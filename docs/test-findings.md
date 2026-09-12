# App fixes needed to make the unit suite green

The unit tests assert the behaviour the app *should* have. Seven of them fail against the current
code. Each entry below names the failing test, what it expects, what the app does instead, and the
smallest fix. Nothing in `app/src/main` was changed to make a test pass; these are the follow-ups.
Entries are removed as they are fixed.

Run: `./gradlew :app:testDebugUnitTest` (292 tests, 7 failing).

---

## 1. Null price crashes the totals helpers instead of counting as zero

**Tests:** `FinanceUtilsTest.addTotalsToExpensesWithoutToday_nullPriceCountsAsZero`,
`FinanceUtilsTest.addTotalsToIncomes_nullPriceCountsAsZero`
**Where:** `core/utils/FinanceUtils.kt:233` (`total.price!! + expense.price!!`) and `:330`
(`total.price!! + income.price!!`)
`Transaction.price` is nullable and `addTotalsToExpenses` already treats a null as `0.0`
(`expenses[j].price ?: 0.0`). The other two helpers dereference it, so one malformed row from
Firestore throws `NullPointerException` inside the `expenses`/`incomes` flow and takes the screen
down.
**Fix:** `(expense.price ?: 0.0)` / `(income.price ?: 0.0)` in both places, matching the first helper.

## 2. `addTotalsToIncomes` emits two rows with the same id

**Test:** `FinanceUtilsTest.addTotalsToIncomes_everyRowHasADistinctId`
**Where:** `core/utils/FinanceUtils.kt:283` and `:300`
When the first income is from a past year, the placeholder TOTAL row and the JOLLY row for the
current year both get `id = todayDate.year.toString()`. `BudgetScreen` keys its list on `id`, so
Compose sees a duplicate key. The expenses variant avoids this with `total_…` / `jolly_…` prefixes.
**Fix:** use the same scheme — `"total_${year}"` for the TOTAL row and `"jolly_${year}"` for the
JOLLY row — and update any scroll-to-id caller that builds the year id (`HomeViewModel.onTransactionCommitted`
passes `year.toString()` for incomes; it must produce the new TOTAL id).

## 3. `AddViewModel.onAddButtonClick` crashes on an unparseable price

**Tests:** `AddViewModelTest.onAddButtonClick_unparseablePrice_emitsWrongAmountInsteadOfCrashing`,
`AddViewModelTest.onAddButtonClick_emptyPrice_emitsEmptyAmountInsteadOfCrashing`
**Where:** `features/add/AddViewModel.kt:139` (`trimmedPriceString.toDouble()`)
The call sits inside `viewModelScope.launch` with a `finally` but no `catch`, so a bad string
throws `NumberFormatException` out of the coroutine and crashes the process.
Today `AddScreen.kt:171-177` validates the field before calling the ViewModel, so the crash is only
reachable if that guard is bypassed (a second caller, a refactor, a shortcut intent). The
ViewModel is the public API and should be safe on its own; `FinanceCode.EMPTY_AMOUNT` and
`WRONG_AMOUNT` already exist for exactly this.
**Fix:** in the ViewModel, `val price = trimmedPriceString.toDoubleOrNull()`; when the string is
blank emit `AddUiEvent.Error(FinanceResult(FinanceCode.EMPTY_AMOUNT))`, when it is non-null but
unparseable emit `Error(FinanceResult(FinanceCode.WRONG_AMOUNT))`, and return. The screen-level
check can then delegate to the same logic instead of duplicating it.

## 4. `ProfileViewModel.isSwitchDynamicColorChecked` starts as `false` regardless of the stored preference

**Test:** `ProfileViewModelTest.isSwitchDynamicColorChecked_startsFromPreferences`
**Where:** `features/profile/ProfileViewModel.kt:63`
The `stateIn` initial value is the literal `false`, while `userPreferences` (one line above) is
initialised from `userPreferencesRepository.userPreferencesFlow.value`. With dynamic colour on, the
switch renders off for the first frame and flips on once the flow is collected.
**Fix:** `initialValue = userPreferencesRepository.userPreferencesFlow.value.dynamicColor`.

## 5. `UserPreferencesRepositoryImpl.updateUser` turns nulls into `""` and `0`

**Test:** `UserPreferencesRepositoryTest.updateUser_preservesNullFields`
**Where:** `core/data/repository/UserPreferencesRepositoryImpl.kt:182-192` (write) and `:74-89` (read)
A `User` with no photo, no local path or no creation date is written as `""` / `0` and read back
that way. `getCreationDataString()` then renders `00/00/0000`, and `email = null` is written as
`""`, which makes the `if (email != null)` read guard on line 75 always pass.
**Fix:** write a key only when the value is non-null (`remove(key)` otherwise) and read it back as
nullable; keep `email` as the presence marker but store it only when non-null. The test pins the
round-trip: `updateUser(u)` followed by a read must equal `u`.

---

## Not reachable from the JVM suite

- `HomeViewModel`'s `RESOURCE_EXHAUSTED` branch (`FirestoreQuotaExceeded`). `FirebaseFirestoreException.Code`
  fails static initialisation without a Firebase runtime, so neither constructing the exception nor
  the `error.code ==` comparison can run on the JVM. Needs a Robolectric or instrumented test, or a
  thin error type of the app's own between the managers and the ViewModel.

## Observed, but no test asserts either way — decide, then add a test

These came up while writing the suite. Each is real behaviour, but whether it is a bug depends on a
contract the code does not state. No test was kept for them so the suite does not enshrine an
accident; pick the intended behaviour and a test follows directly.

- **`addTotalsToExpenses` groups positionally.** The same date appearing non-contiguously produces
  two TOTAL rows for it (`core/utils/FinanceUtils.kt:142-180`). Fine if the input is always the
  DAO's `ORDER BY` output, wrong if anything else ever calls it.
- **`Expense`/`Income` default `id` renders null parts as the word `null`** — e.g.
  `Expense(name = "A", price = 1.0, category = 2)` gets `id = "A1.0null2[]"`
  (`core/data/model/Expense.kt:37`). It is the Room primary key and the Firestore document id; a
  null `timestamp` therefore produces an id that can collide across days.
- **`AddViewModel.onAddButtonClick` with an unknown `requestCode` does nothing** — no event, no
  error, loading starts and stops (`features/add/AddViewModel.kt:142-222`). Only `1` and `2` are
  handled; anything else is silently swallowed.
- **`ExpensesViewModel.itemMetadata` / `BudgetViewModel.itemMetadata` throw on a row with no date**
  — `getLocalDate()` dereferences `year!!`/`month!!`/`day!!`. A malformed Firestore document takes
  the list screen down with the pipeline. Related to finding 1: the same rows are currently
  accepted by the DAO.
- **`Transaction.getPriceString()` throws on a null price** (`price!!`). Same class of input as
  above; the alternative is rendering an empty string or `0.00`.
- **`UserPreferencesRepositoryImpl` materialises a user for `updateUser(User(email = null))`**
  because `email` is written as `""` — a direct consequence of finding 5; fixing that decides this.
- **Every Firestore listener reports quota errors separately.** `HomeViewModel.updateUserData`
  installs the same `onSyncError` on the root, expenses and incomes listeners, so a
  `RESOURCE_EXHAUSTED` state emits `FirestoreQuotaExceeded` up to three times — three dialogs, if
  the screen shows one per event.
- `AuthViewModel.onGoogleRequest` calls `stopLoading()` without a matching `startLoading()`; it
  relies on the screen having started it. Harmless with the ref-counted `LoadingRepository`, but the
  contract is implicit.
- `ExpensesViewModel.expenses` decides between `addTotalsToExpenses` and the `WithoutToday` variant
  by reading `_searchQuery.value` / `_selectedCategories.value` / … *inside* `map`, not from the
  combined `FilterParams`. A rapid filter change can pair a stale decision with a fresh list.
- `ProfileViewModel.editFullName("   ")` and `ExpensesViewModel.addLabelToExpense(…, "   ")`
  return early from inside `try`, so `startLoading()`/`stopLoading()` still bracket a no-op.

## Test-side adjustments made while writing the suite (not app findings)

Listed so nothing looks like a silent concession. In each case the expectation is unchanged; only
how the test observes it moved.

- **Loading brackets.** `startLoading()`/`stopLoading()` with no suspension in between cannot be
  observed under an unconfined Main: the `StateFlow` conflates the `true` before the collector runs.
  Six tests (`Auth`, `Profile`, `Budget`, `Expenses`) switch to `StandardTestDispatcher` +
  `runCurrent()` via a `useStandardMain()` helper. The app really does start and stop loading.
- **Caller-scope launches.** `LabelsViewModel.undoDeleteLabel(scope, …)` launches on the scope the
  screen passes; in tests that is `backgroundScope`, which needs `advanceUntilIdle()` to run.
- **`HomeViewModel.checkUser` needs a first local count.** `updateUserData` awaits
  `getCount().first()` on both local repositories, so the fakes are seeded with an empty list in
  `setup()` — Room emits a count immediately; the fakes do not until told to.
