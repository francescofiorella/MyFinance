# Testing

Unit tests live in `app/src/test` and run on the JVM; no device or emulator is needed.

```
./gradlew :app:testDebugUnitTest
```

The HTML report lands in `app/build/reports/tests/testDebugUnitTest/index.html`. Use the `:app:`
task, not a bare `./gradlew test`: the `:baselineProfile` module has no unit tests and needs a
connected device for everything else. From Android Studio, the gutter ▶ next to a test class or
method runs it as an *Android JUnit* configuration; an *Android App* configuration only launches
the app. The run also verifies the screenshot goldens — if it goes red right after a UI change,
see [Screenshot tests](#screenshot-tests-roborazzi).

## Conventions

The suite follows [Now in Android](https://github.com/android/nowinandroid): no mocking framework,
hand-written fakes, direct ViewModel construction.

- **Fakes, not mocks.** Every repository is an interface bound in `core/di/DataModule.kt`; tests
  use the `Test*` classes in `app/src/test/.../testing/repository/`, which implement the same
  interface and add hooks such as `sendExpenses(...)` or `setUser(...)`. The local-repository fakes
  replicate the DAO's filtering, ordering and null-on-empty sums so ViewModel tests see the same
  shapes the app does. ViewModels are constructed directly with those fakes, never through Hilt.
- **The remote store is an interface.** `core/data/remote/RemoteDataSource` is everything the sync
  managers need from Firestore in plain types (`RemoteDocument` = id, path, raw fields, decoder;
  `RemoteSnapshot`; `RemoteListener`) — no Firebase class crosses it. `FirestoreRemoteDataSource`
  is the only implementation that names Firebase; `testing/remote/TestRemoteDataSource` stores
  documents in memory, records every call, and drives the listeners from the test
  (`sendChanges`, `sendUserSnapshot`, `sendListenerError`), with `failNext` for a network error.
  The manager tests run under Robolectric with the real DAOs on an in-memory database
  (`testing/util/DatabaseTest`, shared with the device DAO tests) and a real IO dispatcher, so
  Room's main-thread guard stays armed (a manager that dropped `withContext(ioDispatcher)` would
  fail here as on a phone).
- **So is the identity provider.** `core/data/remote/AuthDataSource` is what `AuthManager` needs
  from Firebase Auth; failures arrive as `AuthException(kind, errorCode)`, translated once in
  `FirebaseAuthDataSource`, so every `AuthCode` mapping is plain Kotlin and `AuthManagerTest`
  covers each outcome through `testing/remote/TestAuthDataSource` (`signInFailure = AuthException(…)`
  and friends). The two adapters themselves run against the Firebase emulators on the device
  (see [Firebase emulator tests](#firebase-emulator-tests)).
- **Real DataStore, in memory.** `UserPreferencesRepositoryTest` runs the production implementation
  over `testing/util/InMemoryDataStore`, so the `Preferences` key mapping is exercised rather than
  faked.
  `core/di/DataStoreModuleTest` opens the real file once (DataStore allows one instance per
  file per process). The legacy `SharedPreferences` migration is gone: its keys had been renamed
  away in June 2026, so it copied values nobody read; an old `SHARED_PREFERENCES` file is ignored.
- **Fixtures.** `testing/data/TestTransactions.kt` builds `Expense`, `Income`, `User` and
  `UserPreferencesData` with defaults; `year/month/day` and `timestamp` always derive from one
  `LocalDate`, so a fixture cannot disagree with itself.
- **Assertions** use Truth. Turbine is available for ordered-emission checks but the suite reads
  `StateFlow.value` after collecting, as the clone does.
- **Naming**: camelCase, `subject_condition_expectation` or a short sentence, no `test` prefix;
  `@Before fun setup()`; a repository under test is called `subject`.
- **Content descriptions.** A control whose only label is an icon gets a `contentDescription`
  from a string resource, translated in `values-it` (no literals, no `translatable="false"`); if
  several share an icon, the label names what each acts on (`remove_item` → "Remove Dinner",
  `avatar_position` → "Avatar 3 of 7"). An icon next to visible text, or inside a row that already
  reads its text, stays `null`, or TalkBack reads it twice. A swapped icon that is the only cue for
  a state gets a `stateDescription` (`expanded`/`collapsed`); toggles and selectable items use
  Compose's own toggle/`selected` semantics instead. Tests find controls by these labels
  (`string(R.string.remove_item, "Dinner")`; `string()` takes format arguments), so a wrong label
  fails a test. `app/AccessibilityTest` pins the rules the ATF checks cannot see, and the
  `HardcodedContentDescription` [lint](#lint) rule rejects literals.

## Testing ViewModels

Three things trip up every new ViewModel test.

1. **`stateIn(WhileSubscribed)` flows produce nothing until collected.** `viewModel.x.value` returns
   the initial value forever if nothing subscribes. Start a collector first, then push data through
   the fakes, then assert:

   ```kotlin
   backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.expenses.collect() }
   expensesLocalRepository.sendExpenses(a, b)
   assertThat(viewModel.expenses.value).hasSize(3)
   ```

   Tests that only check the initial value deliberately do not collect. `testing/util/MainDispatcherRule`
   installs the unconfined Main that `viewModelScope` relies on.

2. **One-shot `uiEvents` are unbuffered.** `MutableSharedFlow()` has `replay = 0`, so an emit with
   no subscriber is dropped silently. The collector must exist before the action that emits.
   (`HomeViewModel.mainEvents`/`scrollEvents` and the `scrollToId` flows use `replay = 1` and do
   replay to a late subscriber.)

3. **Loading brackets need a standard Main.** When `startLoading()` and `stopLoading()` run with no
   suspension between them, an unconfined Main lets the `StateFlow` conflate the `true` away before
   the collector runs. Those tests call `Dispatchers.setMain(StandardTestDispatcher(testScheduler))`
   and `runCurrent()` after the action; see `useStandardMain()` in `AuthViewModelTest`.

Two smaller ones: a method that launches on a caller-supplied scope (`LabelsViewModel.undoDeleteLabel`)
needs `advanceUntilIdle()` when given `backgroundScope`; and `HomeViewModel.checkUser` awaits the
first local count of each table, so its tests seed both local fakes with an empty list, as Room
would emit one immediately.

## Test JVM setup

`app/build.gradle.kts` pins the test JVM to `en_US`. `AuthCode`, `FinanceCode` and
`FirestoreEnums.NAMES` freeze their messages from `Locale.getDefault()` in static init, so the
locale has to be fixed before the JVM starts; `Locale.setDefault()` in a `@Before` is too late once
any earlier test class has touched the enum. `FormatUtilsTest.currentLanguage_isPinnedToEnglishByTheTestJvm`
is the canary — if it fails, every enum-message assertion is suspect.

`activeCurrencyCode` in `core/utils/FormatUtils.kt` is a process-wide `mutableStateOf`; tests that
touch prices reset it in both `@Before` and `@After` so test order cannot leak.

## Checking the remote data on first launch

`Expense` and `Income` fields are non-null with defaults, so a Firestore document that lacks a field
loads silently with `""`, `0.0`, `0` or `-1`, and a document with an explicit `null` cannot be
deserialised at all. `core/data/manager/DocumentIntegrity` checks every document on its way in, on
both the full sync and the snapshot listener, and logs under one tag:

```
adb logcat -s DataIntegrity
```

- `I  payments: 120 of 120 documents loaded` — one per collection on the first snapshot; equal
  numbers mean the remote data is complete.
- `W  purchases/<email>/payments/<id>: missing [timestamp] (defaults applied)` — loaded, but that
  field came from the default.
- `E  purchases/<email>/payments/<id>: null [price], missing [] (skipped)` — an explicit `null`;
  the document is skipped rather than poisoning the batch, and is reported again on every launch
  until fixed.

Both carry the full document path for the Firebase console. Tombstones (`isDeleted == true`) are
not checked.

## Compose tests on the JVM

Tests that need a composition run under Robolectric in `app/src/test`, next to the plain JVM tests:
`@RunWith(RobolectricTestRunner::class)` plus `createComposeRule()`. `app/src/test/resources/robolectric.properties`
pins the simulated API level to 35; a method can override it with `@Config(sdk = [30])`, and
`@Config(qualifiers = "night")` switches system dark mode. `ThemeTest` is the model: it sets content
inside `MyFinanceTheme` and asserts on `MaterialTheme.colorScheme` from within the composition.

The setup lives in `app/build.gradle.kts`: `unitTests.isIncludeAndroidResources = true` so Robolectric
sees the merged manifest and resources, and `ui-test-manifest` as a `debugImplementation` — the
unit-test variant reads the debug manifest, so a `testImplementation` would not register the
`ComponentActivity` that `createComposeRule()` launches. The first run downloads one `android-all`
jar per API level used (about 100 MB each) into `~/.m2`; Robolectric classes add roughly a minute
to the suite, plain JVM tests are unaffected.

Tests that decode or write bitmaps (`ProfileImageStorageImplTest`, `UserRepositoryImplTest`) need
`@GraphicsMode(GraphicsMode.Mode.NATIVE)`: in Robolectric's legacy mode `Bitmap.compress` writes a
stub and `BitmapFactory.decodeStream` returns a 1×1 bitmap, so sizes and round trips mean nothing.
The download is driven by `MockWebServer` (OkHttp 4.12.0, the version Coil pulls in) against the real
`OkHttpClient`, and the repository writes through the real `ProfileImageStorageImpl` into
Robolectric's `filesDir`.

`captureAfter` is `captureForDevice` plus a coroutine that runs after composition and before the
capture — how the snackbar goldens show a snackbar. They call `snackbarHostState.showSnackbar(…,
duration = Indefinite)` directly, because `MyFinanceAppState.showSnackBar` hard-codes `Short` and the
snackbar would dismiss itself before the capture. `SnackbarInsetsScreenshotTests` fakes a status bar
and navigation bars with a `DeviceConfigurationOverride.WindowInsets` helper copied from nowinandroid
(Compose has no public inset override); since that helper wraps the content in an `AndroidView`, those
tests capture a tagged node instead of `onRoot()`.

The device passed to a capture is the layout size: `DeviceConfigurationOverride.ForcedSize` only
rescales density, so a golden recorded on a big canvas with a small `ForcedSize` shows the big layout.
Pass `DeviceSpec(width, height)` per case instead.

Component tests live in `core/components/` and use two helpers from `testing/util/ComposeTestHelpers.kt`:
`setThemedContent { … }` wraps the content in `MyFinanceTheme`, and `string(R.string.x)` resolves a
resource the way the composable does, so assertions never hard-code UI text. Passing
`setThemedContent(inline = true)` sets `LocalInspectionMode`, which makes `AdaptiveSheet` render its
content directly instead of inside `ModalBottomSheet` — the same path previews take — so the
sheet-based components (`EditTransactionSheet`, `ConfirmationSheetDialog`) are tested through their
header, items and callbacks; the bottom sheet itself is Material's. Two Robolectric quirks: it
measures text a few pixels wide, so a swipe has to be aimed at the container (`onRoot()` with explicit
coordinates) rather than at a `Text` node; and an `Image` with a null `contentDescription` has no
semantics node, so `EmptyViewTest` reads the illustration's presence from where the message lands.

Screen tests (`features/<feature>/<Screen>Test`) render the stateful screen with a ViewModel built the
way its `*ViewModelTest` builds it — the screens take the ViewModel as a parameter; only the
`*Navigation.kt` entries call `hiltViewModel()`, so no Hilt is involved. `loadingRepository.stopFirstSync()`
plus `sendExpenses(…)` / `sendIncomes(…)` move a screen from its loading branch to empty or
populated. Snackbars and navigation for six screens are wired in the navigation entries, so those
tests collect `viewModel.uiEvents` themselves (`LaunchedEffect(Unit) { viewModel.uiEvents.collect { events += it } }`)
and assert on the fakes' recordings. The rules come from `androidx.compose.ui.test.junit4.v2`, whose
`StandardTestDispatcher` queues that collector instead of running it at once, so call
`composeTestRule.waitForIdle()` before reading `events` (the repository recordings need no wait:
the ViewModel calls them directly). `ChangePasswordScreen` collects its own events, so its
snackbar and `onBackClick` are asserted directly. One quirk: screens emit their sheets *before* their
content, so under inline rendering the full-size content covers the sheet and a tap lands on the
content. Sheet items are therefore activated with `performClickAction()` (the semantics click, as
accessibility services do); `performTextInput` already works through semantics.

## Screenshot tests (Roborazzi)

Every screen and the shared components have golden PNGs under `app/src/test/screenshots/`,
recorded on Robolectric with Roborazzi. **Whenever you change how a screen or component looks**,
run these in order:

```
./gradlew :app:verifyRoborazziDebug     # 1. Did anything look different? Fails and lists the screens that changed.
./gradlew :app:compareRoborazziDebug    # 2. Show me. Writes reference | diff | new images to app/build/outputs/roborazzi/*_compare.png.
./gradlew :app:recordRoborazziDebug     # 3. Yes, that is what I meant. Overwrites the goldens; commit the changed PNGs together with the UI change.
```

Step 1 also happens inside the normal `./gradlew :app:testDebugUnitTest` (`roborazzi.test.verify=true`
in `gradle.properties`), so a red unit-test run after a UI edit means "go to step 2". Only record
after looking at the diff: recording over an unintended change hides a regression. A brand-new
screen needs a test plus a first `recordRoborazziDebug`; `verify` fails while a golden is missing.

### How the tests are written

- Classes are named `*ScreenshotTests` and carry `@RunWith(RobolectricTestRunner::class)`,
  `@GraphicsMode(NATIVE)`, `@LooperMode(PAUSED)` and `createAndroidComposeRule<ComponentActivity>()`.
- Helpers in `app/src/test/…/testing/screenshot/ScreenshotHelpers.kt`:
  - `captureMultiDevice("Name") { … }` — one PNG per `DefaultTestDevices` entry: `phone` (411×891 dp),
    `foldable` (673×841), `tablet` (1280×800), all at 420 dpi → `Name_phone.png` etc.
  - `capturePhoneDark("Name") { … }` → `Name_phone_dark.png`.
  - `captureForDevice(spec, "Name", deviceName = …)` for a single size (empty, loading and error
    states use the phone only).
  - `captureMultiTheme("Component") { description -> … }` — light/dark × dynamic/notDynamic →
    `Component/Component_light_notDynamic.png` and three siblings.
  Each helper sets the Robolectric qualifiers, turns `LocalInspectionMode` on (sheets built on
  `AdaptiveSheet` render inline) and wraps the body in `MyFinanceTheme` plus a `Surface` painted
  with `colorScheme.background` — the tab contents (Dashboard, Expenses, Budget, Profile) are
  transparent and get that colour from the Home scaffold in the app.
- Dark mode goes through `DeviceConfigurationOverride.DarkMode`, not `@Config(qualifiers = "night")`:
  it flips `LocalConfiguration`, which both `MyFinanceTheme`'s default and the components that call
  `isSystemInDarkTheme()` directly (`EmptyView`, `PieChart`, `AnnualBalanceCard`) read.
- Screens are rendered through their stateless content composables (`AuthContent`,
  `HomeScreenContent`, `DashboardContent`, `ExpensesContent`, `BudgetContent`, `ProfileContent`,
  the stateless `AddScreen`/`ChangePasswordScreen` overloads, `LabelsContent`, `CategoriesScreen`),
  with fixtures from `testing/screenshot/ScreenshotFixtures.kt`. Nothing may depend on the real
  clock: `screenshotToday` is fixed, and `ExpensesCard`/`AnnualBalanceCard` take `today` /
  `isNextYearEnabled` from the caller instead of reading `LocalDate.now()`.
- The app shell (`HomeShellScreenshotTests`) renders `HomeScreenContent` under
  `DeviceConfigurationOverride.ForcedSize` with an explicit `WindowAdaptiveInfo`, which is how the
  navigation bar vs rail decision is made in `HomeScreen`.
- Goldens are stored at half scale (`resizeScale = 0.5`) and compared pixel-exactly
  (`changeThreshold = 0f`). Fonts are bundled and the illustrations are vectors, so the output is
  stable on one machine. There is no CI: the goldens are recorded on the developer's machine, and
  a different OS or font stack would render slightly differently and need a re-record.

### Accessibility checks

Every capture made through `captureForDevice`, `captureMultiDevice`, `capturePhoneDark` and
`captureAfter` also runs Google's Accessibility Test Framework (`roborazzi-accessibility-check`,
preset `LATEST`, failing on errors): missing labels on clickable nodes, touch targets under 48 dp,
text and image contrast, duplicate labels, and the rest of the ATF set. `captureMultiTheme` is not
checked, as in nowinandroid — a component on its own has no screen context. The checks run in
`testDebugUnitTest`, `verifyRoborazziDebug` and `recordRoborazziDebug` alike.

A failure reads `AccessibilityViewCheckException: There were N accessibility results`, with each
offending node's bounds and the check that reported it. The golden is still captured first, and
each failing view is written to `app/build/outputs/roborazzi/<screenshot>_<device>_<n>.png`.

Fix the component rather than the test. When a failure is a design limit, suppress it through the
helper's `accessibilitySuppressions` parameter, as narrowly as possible: one check *and* the exact
elements (`Matchers.allOf(matchesCheck(TouchTargetSizeCheck::class.java), matchesElements(withContentDescription(…)))`),
never `Matchers.anything()`, with a one-line reason above it. The only suppression today is in
`DashboardScreenScreenshotTests`: the twelve month bars of the chart are about 27 dp wide on a phone.

## Instrumented tests

Instrumented tests live in `app/src/androidTest` and run on a device. The Firebase adapter tests
are part of the run and need the [Firebase emulators](#firebase-emulator-tests) running on this PC
first; without them those two classes fail and say so:

```
firebase emulators:start --only auth,firestore   # separate terminal, repo root; see below
./gradlew :app:connectedDebugAndroidTest
```

Report: `app/build/reports/androidTests/connected/debug/index.html`. The Room DAO and `Converters`
tests follow nowinandroid's `DatabaseTest` pattern — an abstract base builds a fresh
`Room.inMemoryDatabaseBuilder` database per test and closes it after; no rules, no Hilt, no sign-in.
They never launch `MainActivity`, so a locked phone is fine; the Hilt tests below do launch it.
Method names use the `method_condition_expectation` form the clone's `androidTest` lint expects.

Two things about the install:

- The test build is signed with the debug keystore. If the phone has a release-signed build under
  the same `applicationId`, the install fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`; uninstall it
  first (`adb uninstall com.frafio.myfinance`). That drops the local database, which re-syncs from
  Firestore, and the Firebase session, so log in again on the debug build afterwards.
- `gradle.properties` sets `android.injected.androidTest.leaveApksInstalledAfterRun=true` so the app
  is not uninstalled after every run (the AGP default), which would repeat that login each time.

The search term reaches the DAO already escaped (`ExpensesLocalRepositoryImpl.escapeForLike`) and
the two `LIKE` clauses declare `ESCAPE '\'`, so `%` and `_` in what the user types are literal.
`ExpensesLocalRepositoryImplTest` covers the escaping on the JVM; `ExpenseDaoTest` covers the clause
on the device.

## Firebase emulator tests

`core/data/remote/FirestoreRemoteDataSourceTest` and `FirebaseAuthDataSourceTest` run the two real
Firebase adapters on the phone against the [Firebase Emulator Suite](https://firebase.google.com/docs/emulator-suite)
— local stand-ins for Firestore and Auth running on this PC. They check what the fakes
(`TestRemoteDataSource`, `TestAuthDataSource`) assume: the fields `toFirestoreMap()` writes decode
back through `toObject()`, `arrayUnion`/`arrayRemove` and merges behave, the `updatedAt > since`
listener arrives oldest first, and each Firebase failure becomes the `AuthException` kind and
`ERROR_*` code `AuthManager` switches on. No Android emulator is involved: "emulator" here is the
Firebase one, and the tests run on the phone.

**Setup, once.** The CLI is Google's standalone `firebase-tools-win.exe` (bundles its own Node),
saved as `%USERPROFILE%\bin\firebase.exe`; download it from `https://firebase.tools/bin/win/latest`.
The emulators need Java 21+, and the `java` on PATH may be older, so point the shell at Android
Studio's bundled JDK. The first start downloads the emulator jars to `~/.cache/firebase`.

**Every device run.** In a separate PowerShell, from the repo root, and leave it running:

```
$env:JAVA_HOME = "$env:LOCALAPPDATA\Programs\Android Studio\jbr"; $env:Path = "$env:JAVA_HOME\bin;$env:Path"
firebase emulators:start --only auth,firestore
```

`firebase.json` fixes the ports (Auth 9099, Firestore 8080, no UI) and `.firebaserc` the project id,
which must match `google-services.json`. There is no rules file, so the emulator allows every read
and write: these are adapter tests, not rules tests.

How the pieces connect:

- **`adb reverse`.** `connectedDebugAndroidTest` depends on `firebaseEmulatorReverse`, which runs
  `adb reverse` for both ports, so `127.0.0.1` on the phone is this PC. With more than one device
  attached adb needs `-s`; keep one connected.
- **Cleartext, debug only.** The emulators speak plain HTTP, which the main manifest forbids
  (`usesCleartextTraffic="false"`). `app/src/debug/res/xml/network_security_config.xml` allows it for
  `127.0.0.1` and `localhost` only; release builds are untouched.
- **`testing/firebase/FirebaseEmulator`.** `connect()` probes both ports over HTTP (a bare socket
  connect is not enough: `adb reverse` accepts it even when nothing listens on the PC), then calls
  `useEmulator` on Firestore (memory cache) and Auth. `useEmulator` throws once an instance is in
  use, so a test that forgot to connect fails instead of reaching the real project. `clearFirestore()`
  and `clearAuth()` wipe the emulators in `@BeforeClass`; `oobCodes()` lists the mails Auth would
  have sent.
- **Isolation.** Every test uses its own random email, so documents (`purchases/<email>/…`) and
  accounts never collide between tests, and listener tests wait on a `Channel` with a timeout.
- **Google sign-in** is covered here: the Auth emulator accepts an unsigned JSON id token.

**What the emulator cannot show.** It answers with the *legacy* error codes (`ERROR_WRONG_PASSWORD`,
`ERROR_USER_NOT_FOUND`). A production project with email-enumeration protection (the Firebase
default since September 2023) answers both a wrong password and an unknown user with
`ERROR_INVALID_CREDENTIAL`, which `AuthManager` sends to its generic branch. Whether
`myfinance-fadef` has the protection on is a console setting; try a wrong password on the phone to see
which message comes back.

## Hilt instrumented tests

`app/NavigationTest` and `app/LaunchTest` drive the real `MainActivity` on the device without
Firebase. The pieces, all ported from nowinandroid:

- **Runner.** `testInstrumentationRunner` is `testing/MyFinanceTestRunner` (`androidTest`), which
  swaps the `Application` for `HiltTestApplication`. Tests that do not use Hilt, such as the DAO
  tests, run under it unchanged.
- **Test graph.** Four `@TestInstallIn` modules in `testing/di/` replace their production modules
  for every `@HiltAndroidTest`: `TestDispatchersModule` (both qualifiers → one
  `UnconfinedTestDispatcher`), `TestDataStoreModule` (`InMemoryDataStore`), `TestDatabaseModule`
  (`Room.inMemoryDatabaseBuilder`) and `TestDataModule`. The last keeps the real local repositories
  and `UserPreferencesRepositoryImpl` over that in-memory storage and binds the fakes for
  `UserRepository`, `ExpensesRepository`, `IncomeRepository` and `ProfileImageStorage`, so no code
  path constructs `FirebaseAuth` or `FirebaseFirestore`. `HiltAndroidRule` rebuilds the component
  per test: every test starts with an empty database and empty preferences, and the fakes at their
  defaults (logged in, sync completes at once).
- **Shared sources.** The fakes, fixtures and DI modules live in `app/src/sharedTest`, registered as
  a Kotlin source directory of both `test` and `androidTest` in `app/build.gradle.kts`
  (`kotlin.directories`; a `java` source directory is not picked up by AGP 9's built-in Kotlin). The old
  `androidTest/…/DaoFixtures.kt` copy went away with it.
- **Seeding.** `NavigationTest` uses `createAndroidComposeRule<MainActivity>()` and injects the
  concrete fakes plus `ExpenseDao`/`UserPreferencesRepository` to seed data in `@Before`; the screens
  react live. State that must exist *before* the activity (a logged-out user, a shortcut intent)
  belongs in `LaunchTest`, which uses `createEmptyComposeRule()` and `ActivityScenario.launch` after
  `hiltRule.inject()`.
- **Splash.** `MainActivity` keeps the splash until `checkUser()` completes, so the first thing a
  test does is `waitUntilExactlyOneExists(hasTestTag("tab_dashboard"))` (or `login_button`).
- **Empty states.** Dashboard, Expenses and Labels render `EmptyView` without data, so their content
  tags (`dashboard_scroll`, `expenses_list`, `labels_list`) only exist once something is seeded;
  `LaunchTest.freshStart_withoutData_showsTheEmptyDashboard` pins the empty case.
- **The phone must be unlocked.** The DAO tests pass on a locked phone; anything that launches an
  activity is paused immediately (`wm_pause_activity … sleep`) and fails with "No compose
  hierarchies found". Enable *Stay awake* in Developer options for long runs.

`HiltComponentActivity` (`app/src/debug`, declared in the debug manifest) is the host for future
Robolectric tests that need `hiltViewModel()`: `@Config(application = HiltTestApplication::class)`
plus `createAndroidComposeRule<HiltComponentActivity>()`, with the same test modules
(`hilt-android-testing` is already on the unit-test classpath; the Hilt plugin adds its compiler to
every KSP configuration, so there is no `kspTest`/`kspAndroidTest` line).

## Lint

Android Lint reads the code without running it: the built-in checks plus four of our own. Android
Studio highlights both in the open file; for the whole project run

```
./gradlew :app:lintDebug      # or the "Run Lint" configuration
```

Report: `app/build/reports/lint-results-debug.html`. A new **error** fails the task (and, through
`lintVitalRelease`, a release build); warnings are listed but do not fail. `app/lint-baseline.xml`
holds the warnings accepted so far — unused launcher resources and strings, long vector paths in one
illustration — so only new problems show up. Fix a new issue rather than baselining it; regenerate
the baseline (`./gradlew :app:updateLintBaseline`) only after deciding an issue stays, and review the
diff.

The custom rules live in the `:lint` module (the nowinandroid pattern), wired into the app with
`lintChecks(project(":lint"))`. Each one enforces a convention from this document:

| Issue id | Severity | Rule | Why |
|---|---|---|---|
| `HardcodedContentDescription` | Error | `contentDescription` is never a string literal (argument or `semantics { }`); previews are exempt | a literal is English in every locale and bypasses `values-it` (see Content descriptions) |
| `ClockInComposition` | Error | no `LocalDate.now()`, `System.currentTimeMillis()`, `Calendar.getInstance()` and friends while composing — in a composable body, its default arguments, a `@Composable` content lambda or `remember { }`; effect and callback lambdas and previews are fine | a composable that reads the clock renders differently every day, which breaks the screenshot goldens |
| `FirebaseOutsideAdapter` | Error | `FirebaseFirestore.getInstance()`, `FirebaseAuth.getInstance()`, `Firebase.firestore`, `Firebase.auth` only in `FirestoreRemoteDataSource` and `FirebaseAuthDataSource` | everything else must go through `RemoteDataSource` / `AuthDataSource`, which the tests fake |
| `TestMethodPrefix` | Warning | a `@Test` function does not start with `test` (quick fix removes it) | the naming convention; ported from nowinandroid |

A rule is a `Detector` in `lint/src/main/kotlin/…/lint/`, listed in `MyFinanceIssueRegistry`, with a
test in `lint/src/test` that runs it on small source files (`Stubs` supplies the Compose, JUnit and
Firebase declarations it needs) and pins the exact report. `./gradlew :lint:test` runs those tests;
after a Gradle sync the editor picks up a changed rule.

## What is covered

| Layer | Where |
|---|---|
| ViewModels (all nine) | `features/*/…ViewModelTest`, `app/HomeViewModelTest` |
| Repositories, mapper, integrity check | `core/data/…` |
| Profile picture download and storage (MockWebServer + real file I/O) | `core/data/repository/UserRepositoryImplTest`, `core/data/storage/ProfileImageStorageImplTest` |
| Sync managers over a fake remote and in-memory Room (Robolectric) | `core/data/manager/…SyncManagerTest` |
| `AuthManager` over a fake identity provider | `core/data/manager/AuthManagerTest` |
| Room DAOs and `Converters` (device) | `androidTest/…/core/data/dao/…`, `…/converters/…` |
| App navigation, login, shortcuts (device, Hilt) | `androidTest/…/app/NavigationTest`, `…/LaunchTest` |
| The Firebase adapters against the Firebase emulators (device) | `androidTest/…/core/data/remote/FirestoreRemoteDataSourceTest`, `…/FirebaseAuthDataSourceTest` |
| Navigation (`Navigator`, `NavigationState`, `MyFinanceAppState`) | `core/navigation/…` |
| Theme resolution (Robolectric) | `core/theme/ThemeTest` |
| Shared Compose components (Robolectric) | `core/components/…Test` |
| Feature components: auth form and fields, dashboard cards, filter chips and sheets, profile sheets, budget sheet, labels list | `features/*/components/…Test`, `features/auth/AuthContentTest`, `features/labels/LabelsContentTest` |
| Screens over their ViewModels: loading / empty / populated, validation, sheets, callbacks | `features/*/…ScreenTest` |
| What TalkBack hears: control labels, silent decorative icons, chart bars and arcs, expand state | `app/AccessibilityTest` |
| Snackbar placement at three widths, with and without system insets | `app/Snackbar(Insets)ScreenshotTests` |
| Screens, app shell and components as golden images, with ATF accessibility checks (Roborazzi) | `**/*ScreenshotTests`, `app/src/test/screenshots` |
| Utilities, models, enums | `core/utils/…`, `core/data/model/…`, `core/data/enums/…` |
| Transaction wire format (`toFirestoreMap` vs Firestore's reflective mapper, plain JVM) | `core/data/model/FirestoreMappingTest` |
| The custom lint rules on sample sources | `lint/src/test/…/lint/…DetectorTest` |

## Not covered yet

- **`HomeScreen` and the navigation entries** — the tab entries call `hiltViewModel()` and the
  entries own the snackbar/undo reactions; the shell is covered by `HomeShellScreenshotTests` and
  the device `NavigationTest`. The date-picker dialogs are also uncovered: they are Material's.
- **Google sign-in, before the adapter** — `androidx.credentials.Credential` needs an
  `android.os.Bundle`, which the JVM cannot build, so the Credential Manager step in `AuthViewModel`
  is untested; the adapter's `signInWithGoogle` is covered on the Auth emulator.
- **Production security rules and error codes** — the rules are not in the repo and the emulator
  runs open; the enumeration-protection caveat is under [Firebase emulator tests](#firebase-emulator-tests).
