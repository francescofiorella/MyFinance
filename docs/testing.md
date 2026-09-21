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
- **Real DataStore, in memory.** `UserPreferencesRepositoryTest` runs the production implementation
  over `testing/util/InMemoryDataStore`, so the `Preferences` key mapping is exercised rather than
  faked.
- **Fixtures.** `testing/data/TestTransactions.kt` builds `Expense`, `Income`, `User` and
  `UserPreferencesData` with defaults; `year/month/day` and `timestamp` always derive from one
  `LocalDate`, so a fixture cannot disagree with itself.
- **Assertions** use Truth. Turbine is available for ordered-emission checks but the suite reads
  `StateFlow.value` after collecting, as the clone does.
- **Naming**: camelCase, `subject_condition_expectation` or a short sentence, no `test` prefix;
  `@Before fun setup()`; a repository under test is called `subject`.

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

## Instrumented tests

Instrumented tests live in `app/src/androidTest` and run on a device:

```
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

## What is covered

| Layer | Where |
|---|---|
| ViewModels (all nine) | `features/*/…ViewModelTest`, `app/HomeViewModelTest` |
| Repositories, mapper, integrity check | `core/data/…` |
| Room DAOs and `Converters` (device) | `androidTest/…/core/data/dao/…`, `…/converters/…` |
| App navigation, login, shortcuts (device, Hilt) | `androidTest/…/app/NavigationTest`, `…/LaunchTest` |
| Navigation (`Navigator`, `NavigationState`, `MyFinanceAppState`) | `core/navigation/…` |
| Theme resolution (Robolectric) | `core/theme/ThemeTest` |
| Shared Compose components (Robolectric) | `core/components/…Test` |
| Screens, app shell and components as golden images (Roborazzi) | `**/*ScreenshotTests`, `app/src/test/screenshots` |
| Utilities, models, enums | `core/utils/…`, `core/data/model/…`, `core/data/enums/…` |

## Not covered yet

- **Firestore sync managers and `AuthManager`** — they obtain `FirebaseFirestore`/`FirebaseAuth`
  inline, so there is no seam for a fake; a `RemoteDataSource` interface would unlock them. The
  Hilt tests bypass them entirely through `TestDataModule`.
- **Feature components and screen logic** — the same Robolectric technique as `core/components`
  (the screens already carry `Modifier.testTag`); screens are only covered as images so far.
  `PieChart` arcs and the date-picker dialogs are also uncovered: the arcs have no semantics, the
  dialogs are Material's. Roborazzi's accessibility checks (`roborazzi-accessibility-check`) are
  not enabled: several icons still have `contentDescription = null`.
- **Google sign-in** — `androidx.credentials.Credential` needs an `android.os.Bundle`, which the
  JVM cannot build.
