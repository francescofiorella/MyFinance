# Testing

Unit tests live in `app/src/test` and run on the JVM; no device or emulator is needed.

```
./gradlew :app:testDebugUnitTest
```

The HTML report lands in `app/build/reports/tests/testDebugUnitTest/index.html`. Use the `:app:`
task, not a bare `./gradlew test`: the `:baselineProfile` module has no unit tests and needs a
connected device for everything else. From Android Studio, the gutter ▶ next to a test class or
method runs it as an *Android JUnit* configuration; an *Android App* configuration only launches
the app.

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

## What is covered

| Layer | Where |
|---|---|
| ViewModels (all nine) | `features/*/…ViewModelTest`, `app/HomeViewModelTest` |
| Repositories, mapper, integrity check | `core/data/…` |
| Navigation (`Navigator`, `NavigationState`, `MyFinanceAppState`) | `core/navigation/…` |
| Utilities, models, enums | `core/utils/…`, `core/data/model/…`, `core/data/enums/…` |

## Not covered yet

- **Room DAO queries** — the `LIKE` predicate and the `year * 100 + month` range are only
  re-implemented by the fakes. Needs instrumented tests (`Room.inMemoryDatabaseBuilder`, a device or
  a Gradle managed device).
- **Firestore sync managers and `AuthManager`** — they obtain `FirebaseFirestore`/`FirebaseAuth`
  inline, so there is no seam for a fake; a `RemoteDataSource` interface would unlock them.
- **Compose UI** — logic tests (Robolectric + `ui-test-junit4`; the screens already carry
  `Modifier.testTag`) and Roborazzi screenshots.
- **Google sign-in** — `androidx.credentials.Credential` needs an `android.os.Bundle`, which the
  JVM cannot build.
