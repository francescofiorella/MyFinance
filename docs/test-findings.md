# Test findings

The unit tests assert the behaviour the app *should* have. When the app disagrees, the test stays
red and the fix is listed here; entries are removed as they are fixed.

Run: `./gradlew :app:testDebugUnitTest` (292 tests, all passing).

## Open findings

None.

---

## Observed, but no test asserts either way — decide, then add a test

These came up while writing the suite. Each is real behaviour, but whether it is a bug depends on a
contract the code does not state. No test was kept for them so the suite does not enshrine an
accident; pick the intended behaviour and a test follows directly from the example.

### `addTotalsToExpenses` groups by position, not by date

`core/utils/FinanceUtils.kt:142-187`. Consecutive rows with the same date form one group; the same
date appearing again later starts a new group with its own TOTAL row.

```kotlin
// today = 15 Jun
addTotalsToExpenses(listOf(a /* 15 Jun */, b /* 14 Jun */, c /* 15 Jun */), today)
// -> [TOTAL total_15_6_2024 (a only), a, TOTAL total_14_6_2024, b, TOTAL total_15_6_2024 (c only), c]
```

Two rows share the key `total_15_6_2024`, which `ExpensesScreen` uses as the `LazyColumn` key.
Harmless as long as every caller passes the DAO's `ORDER BY year DESC, month DESC, day DESC`
output — which is the case today. Either document the precondition or sort inside the function.

### A transaction built without a `timestamp` gets `null` inside its default id

`core/data/model/Expense.kt:37` and `Income.kt`: `id = "$name$price$timestamp$category$labels"`.

```kotlin
Expense(name = "Coffee", price = 1.5, category = 5).id   // "Coffee1.5null5[]"
```

The id is the Room primary key, so two such rows on different days replace each other on insert.
Today every producer sets `timestamp` (`AddViewModel` derives it from the date; the sync manager
overwrites `id` with the Firestore document id), so the exposure is a future caller that forgets.
Options: make `timestamp` non-null in the constructor, or derive the id from the date fields.

### `AddViewModel.onAddButtonClick` silently ignores an unknown `requestCode`

`features/add/AddViewModel.kt:149-231`. The `when (navKey.requestCode)` handles `1` (add) and `2`
(edit) and has no `else`.

```kotlin
val vm = AddViewModel(…, RootKey.AddEditTransaction(requestCode = 3, expenseCode = 10))
vm.onAddButtonClick("Coffee", "2.5", 5, 2024, 3, 7, emptyList())
// -> no repository call, no uiEvent, isAdding true then false, loading starts and stops
```

The user sees the button do nothing. `RootKey.AddEditTransaction.requestCode` is a plain `Int`, so
nothing prevents a third value. A sealed `RequestType` (or an `else -> error(…)`) closes this.

### A row with no date crashes the list pipelines

`getLocalDate()` dereferences `year!!`, `month!!`, `day!!` and is called from `addTotalsToExpenses`,
`addTotalsToExpensesWithoutToday` and `ExpensesViewModel.itemMetadata`.

```kotlin
// Room accepts this row; nothing validates on insert
Expense(name = "Ghost", price = 1.0, category = 5)   // year/month/day null

// then, in ExpensesViewModel.expenses
addTotalsToExpenses(listOf(ghost))   // NullPointerException inside the flow -> screen crashes
```

`BudgetViewModel.itemMetadata` compares nullable `year`s instead, so it does not throw, but the
grouping for such a row is undefined. The DAO's sums and ordering accept these rows. Decide whether
the model guarantees a date (make `year`/`month`/`day` non-null and reject at sync time) or whether
the pipelines skip such rows.

### `getPriceString()` throws on a null price

`core/data/model/Expense.kt` / `Income.kt`: `doubleToPrice(price!!)`.

```kotlin
Expense(name = "Ghost", price = null, year = 2024, month = 6, day = 15).getPriceString()
// -> NullPointerException
```

Since the totals helpers now treat a null price as `0.0`, rendering is the remaining crash site: a
null-priced row reaches `TransactionItems` and the composable calls `getPriceString()`. Either render
`"€ 0.00"` (matching the totals) or guarantee `price` at the model level.

### A user without an email cannot be stored

`core/data/repository/UserPreferencesRepositoryImpl.kt:75-76`. After the `updateUser` fix, a null
field clears its key; `email` is also the presence marker on read.

```kotlin
updateUser(User(fullName = "Ada", email = null))
userPreferencesFlow.first().user   // null — the name was written but is invisible
```

Firebase always supplies an email for password and Google accounts, so this is theoretical today.
If a provider without email is ever added, make `User.email` non-null (so the compiler enforces the
invariant) or add an explicit `user_present` key.

### `AuthViewModel.onGoogleRequest` relies on the screen for the matching `startLoading()`

`features/auth/AuthScreen.kt:108-121` calls `viewModel.startLoading()`, then `handleGoogleSignIn`
calls either `onGoogleRequest` (which stops loading on completion) or the error callback (which
stops it). Every normal path is balanced, but the pairing is split across two files and one
`launch`:

```
scope.launch {
    viewModel.startLoading()
    handleGoogleSignIn(…)      // suspends inside credentialManager.getCredential(...)
}
```

If that coroutine is cancelled while suspended — the composable leaves composition, or the activity
is recreated — neither callback runs and the app-wide `LoadingRepository` stays at `true` until
something else calls `stopLoading()`. Wrapping the call in `try { … } finally { stopLoading() }`
inside the screen, or moving the whole flow into the ViewModel, makes the pairing local.

### `ExpensesViewModel.expenses` reads the filters from `.value` inside `map`

`features/expenses/ExpensesViewModel.kt:145-153`. The decision between `addTotalsToExpenses` (with
the today block) and `addTotalsToExpensesWithoutToday` uses `_searchQuery.value`,
`_selectedCategories.value`, `_selectedLabels.value` and `_dateRange.value` at the moment the
mapped list arrives, not the values that produced that list.

```
t0  onSearchQueryChanged("a")   -> combine emits FilterParams("a"), DB query starts
t1  onSearchQueryChanged("")    -> combine emits FilterParams(""), second DB query starts
t2  result for "a" arrives      -> map sees _searchQuery.value == "" -> addTotalsToExpenses
                                   (today TOTAL + JOLLY rows prepended to a search result)
t3  result for "" arrives       -> correct
```

The wrong list is visible between t2 and t3. Carrying `FilterParams` through to the `map` (e.g. by
mapping `Pair(params, list)` out of `flatMapLatest`) removes the race.

### No-op writes still flash the loading indicator

`ProfileViewModel.editFullName` and `ExpensesViewModel.addLabelToExpense` validate *after*
`startLoading()`, returning from inside the `try`, so `finally` still runs `stopLoading()`.

```kotlin
viewModel.editFullName("   ")
// isLoading: false -> true -> false, no repository call, no event
```

On the main thread this is a single frame; a `LinearWavyProgressIndicator` bound to `isLoading` can
still blink. Validate before `startLoading()`.
