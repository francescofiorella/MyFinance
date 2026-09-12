# App fixes needed to make the unit suite green

The unit tests assert the behaviour the app *should* have. Eight of them fail against the current
code. Each entry below names the failing test, what it expects, what the app does instead, and the
smallest fix. Nothing in `app/src/main` was changed to make a test pass; these are the follow-ups.

Run: `./gradlew :app:testDebugUnitTest` (292 tests, 8 failing).

---

## 1. `AuthCode` has two entries with code 26

**Test:** `AuthCodeTest.codes_areUnique`
**Where:** `core/data/enums/auth/AuthCode.kt:151` and `:159`
`EMPTY_NEW_PASSWORD` and `EMPTY_CONFIRM_NEW_PASSWORD` both declare `26`. Every consumer compares
`AuthResult.code` as an `Int`, so any branch on one of them silently matches the other.
**Fix:** give `EMPTY_CONFIRM_NEW_PASSWORD` its own code (`27` is free) and check nothing persists
the numeric value.

## 2. Null price crashes the totals helpers instead of counting as zero

**Tests:** `FinanceUtilsTest.addTotalsToExpensesWithoutToday_nullPriceCountsAsZero`,
`FinanceUtilsTest.addTotalsToIncomes_nullPriceCountsAsZero`
**Where:** `core/utils/FinanceUtils.kt:233` (`total.price!! + expense.price!!`) and `:330`
(`total.price!! + income.price!!`)
`Transaction.price` is nullable and `addTotalsToExpenses` already treats a null as `0.0`
(`expenses[j].price ?: 0.0`). The other two helpers dereference it, so one malformed row from
Firestore throws `NullPointerException` inside the `expenses`/`incomes` flow and takes the screen
down.
**Fix:** `(expense.price ?: 0.0)` / `(income.price ?: 0.0)` in both places, matching the first helper.

## 3. `addTotalsToIncomes` emits two rows with the same id

**Test:** `FinanceUtilsTest.addTotalsToIncomes_everyRowHasADistinctId`
**Where:** `core/utils/FinanceUtils.kt:283` and `:300`
When the first income is from a past year, the placeholder TOTAL row and the JOLLY row for the
current year both get `id = todayDate.year.toString()`. `BudgetScreen` keys its list on `id`, so
Compose sees a duplicate key. The expenses variant avoids this with `total_…` / `jolly_…` prefixes.
**Fix:** use the same scheme — `"total_${year}"` for the TOTAL row and `"jolly_${year}"` for the
JOLLY row — and update any scroll-to-id caller that builds the year id (`HomeViewModel.onTransactionCommitted`
passes `year.toString()` for incomes; it must produce the new TOTAL id).

## 4. `AddViewModel.onAddButtonClick` crashes on an unparseable price

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

## 5. `ProfileViewModel.isSwitchDynamicColorChecked` starts as `false` regardless of the stored preference

**Test:** `ProfileViewModelTest.isSwitchDynamicColorChecked_startsFromPreferences`
**Where:** `features/profile/ProfileViewModel.kt:63`
The `stateIn` initial value is the literal `false`, while `userPreferences` (one line above) is
initialised from `userPreferencesRepository.userPreferencesFlow.value`. With dynamic colour on, the
switch renders off for the first frame and flips on once the flow is collected.
**Fix:** `initialValue = userPreferencesRepository.userPreferencesFlow.value.dynamicColor`.

## 6. `UserPreferencesRepositoryImpl.updateUser` turns nulls into `""` and `0`

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

## Noticed while writing the tests, not test-backed

- `AuthViewModel.onGoogleRequest` calls `stopLoading()` without a matching `startLoading()`; it
  relies on the screen having started it. Harmless with the ref-counted `LoadingRepository`, but the
  contract is implicit.
- `ExpensesViewModel.expenses` decides between `addTotalsToExpenses` and the `WithoutToday` variant
  by reading `_searchQuery.value` / `_selectedCategories.value` / … *inside* `map`, not from the
  combined `FilterParams`. A rapid filter change can pair a stale decision with a fresh list.
- `ProfileViewModel.editFullName("   ")` and `ExpensesViewModel.addLabelToExpense(…, "   ")`
  return early from inside `try`, so `startLoading()`/`stopLoading()` still bracket a no-op.
