# Test findings

The unit tests assert the behaviour the app *should* have. When the app disagrees, the test stays
red and the fix is listed here; entries are removed as they are fixed.

Run: `./gradlew :app:testDebugUnitTest` (294 tests, all passing).

## Open findings

None.

---

## Observed, but no test asserts either way — one decision covers all three

The three remaining items are the same question asked three ways: **should `Expense` / `Income`
guarantee their fields at the type level?** Today `name`, `price`, `year`, `month`, `day`,
`timestamp` and `category` are all nullable (`labels` is a non-null `List`). Nothing in the app
ever writes a null for any of them — `AddScreen` and `AddViewModel` reject empty input, and the
ViewModel derives `timestamp` from the date — so every case below is reachable only through data
the app did not produce (a document written by an older version or edited in the Firebase console)
or through a future Kotlin caller that omits a field.

### Any null component renders as the word `null` inside the default id

`core/data/model/Expense.kt:37` and `Income.kt`: `id = "$name$price$timestamp$category$labels"`.
This is not specific to `timestamp`; every nullable component behaves the same way.

```kotlin
Expense(name = "Coffee", price = 1.5, category = 5).id              // "Coffee1.5null5[]"
Expense(name = "Coffee", timestamp = 1L, category = 5).id           // "Coffeenull15[]"
```

The id is the Room primary key and the Firestore document id, so two rows that differ only in the
missing field replace each other on insert. `timestamp` is the one worth watching because it is the
only component the caller does not type in — it is computed — so it is the one a new call site could
plausibly forget.

### `getLocalDate()` throws when the date fields are null

`Expense.getLocalDate()` is `LocalDate.of(year!!, month!!, day!!)` — it does not return null, it
throws `NullPointerException`. The fields are `Int?` only because Firestore's `toObject()` fills a
class from the document and leaves any absent field at its default, which is `null` here. A
document that lacks `year` therefore yields a row with `year == null`, Room stores it without
complaint, and the first pipeline to call `getLocalDate()` — `addTotalsToExpenses` in
`ExpensesViewModel.expenses` — takes the screen down.

```kotlin
val ghost = Expense(name = "Ghost", price = 1.0, category = 5)   // year/month/day null
addTotalsToExpenses(listOf(ghost))                               // NullPointerException
```

### `getPriceString()` throws on a null price

`doubleToPrice(price!!)`. The totals helpers now treat a null price as `0.0`, so this is the last
crash site for a null-priced row: `TransactionItems` calls `getPriceString()` while rendering it.

```kotlin
Expense(name = "Ghost", price = null, year = 2024, month = 6, day = 15).getPriceString()
// -> NullPointerException
```

### What making the fields non-null would mean

Firestore is not the obstacle. Its mapper needs a no-arg constructor (satisfied when every
parameter has a default) and then writes fields reflectively, where Kotlin nullability is not
checked. So an *absent* field keeps its default (`price: Double = 0.0`), which is exactly the
tolerant behaviour wanted; a field that is *explicitly* `null` in a document would be written into a
non-null field and fail on first read instead of when rendered — same class of crash, moved
earlier, and only for documents this app never wrote.

The real cost is Room. `price REAL` becoming `price REAL NOT NULL` is a schema change, so the
database version must bump, and `MyFinanceDatabase` uses `fallbackToDestructiveMigration` with
`exportSchema = false`: every device drops its local cache on first launch after the update and
re-downloads from Firestore (the `onDestructiveMigration` callback resets the sync timestamps, so
the re-sync is automatic).

Options:

1. **Non-null fields with defaults** — `name: String = ""`, `price: Double = 0.0`,
   `year/month/day: Int = 0`, `timestamp: Long = 0L`, `category: Int = -1`. Closes all three items
   at the type level; the `!!` calls disappear; one destructive migration.
2. **Keep the model nullable, make the readers tolerant** — `getPriceString()` renders `0.00` for a
   null price, the list pipelines skip rows without a date. No migration; the invariant stays
   informal and the id scheme is unchanged.
3. **Leave as is** — accept that malformed remote data crashes the list screens.
