# MyFinance

MyFinance is an Android application that allows to keep track of the personal finances. It shows some plots and statistics including monthly/annual expenses, while giving insights on expense categories.

The app is developed in Kotlin with Android Studio and, so far, four version were built:
- An initial [Java version](https://github.com/francescofiorella/MyFinance-Java).
- The second version uses the Material Design guidelines and the MVVM Design Pattern.
- In the third version, the app design was upgraded to Material You.
- The fourth version tries to improve the user experience by implementing a category system. In addition, incomes and budget management has been implemented, along with more useful plots.

## Testing

Unit tests live in `app/src/test` and run on the JVM, no device or emulator needed:

```
./gradlew :app:testDebugUnitTest
```

The HTML report lands in `app/build/reports/tests/testDebugUnitTest/index.html`. Run the `:app:`
task, not a bare `./gradlew test`: the `:baselineProfile` module has no unit tests and needs a
connected device for everything else.

The suite follows the conventions of [Now in Android](https://github.com/android/nowinandroid):

- **No mocking framework.** Every repository is an interface bound in `core/di/DataModule.kt`; tests
  use hand-written `Test*` fakes from `app/src/test/.../testing/repository/` that implement the same
  interface and add hooks such as `sendExpenses(...)`. ViewModels are constructed directly with those
  fakes, never through Hilt.
- **Real DataStore, in memory.** `UserPreferencesRepositoryTest` runs the production implementation
  over `InMemoryDataStore`, so the `Preferences` key mapping is exercised rather than faked.
- **`stateIn(WhileSubscribed)` flows produce nothing until collected.** A test that reads such a
  flow first starts a collector — `backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.x.collect() }`
  — then pushes data through the fakes and asserts `.value`. One-shot `uiEvents` are unbuffered, so
  the collector must exist before the action that emits. `MainDispatcherRule` provides the
  unconfined Main these rely on.
- **Loading brackets need a standard Main.** When `startLoading()` and `stopLoading()` run with no
  suspension between them, an unconfined Main lets the `StateFlow` conflate the `true` away; those
  tests switch to `StandardTestDispatcher` and call `runCurrent()`.
- **The test JVM is pinned to `en_US`** in `app/build.gradle.kts`. `AuthCode`, `FinanceCode` and
  `FirestoreEnums.NAMES` freeze their messages from `Locale.getDefault()` in static init, so the
  locale must be fixed before the JVM starts; `FormatUtilsTest.currentLanguage_isPinnedToEnglishByTheTestJvm`
  is the canary.
- Assertions use Truth; Turbine is available for ordered-emission checks.

Tests assert the behaviour the app should have. When the app disagrees, the test stays red and the
fix is listed in [docs/test-findings.md](docs/test-findings.md), together with behaviours that were
observed but deliberately not asserted.

### Checking the remote data after the non-null model change

`Expense` and `Income` fields are non-null with defaults, so a Firestore document that lacks a field
loads silently with `""`, `0.0`, `0` or `-1`, and a document with an explicit `null` cannot be
deserialised at all. `DocumentIntegrity` checks every document on its way in and logs under one tag.
On the first launch after updating, watch the sync with:

```
adb logcat -s DataIntegrity
```

One `I` line per collection (`payments: 120 of 120 documents loaded`) means the remote data is
complete. A `W` line names a document that was loaded through defaults and which fields it lacked;
an `E` line names a document that was skipped because a required field is explicitly `null`. Both
include the full document path so it can be fixed in the Firebase console.

Not covered yet: Room DAO queries (instrumented tests), the Firestore sync managers (no injection
seam for `FirebaseFirestore`/`FirebaseAuth`), Compose UI, and screenshot tests.
