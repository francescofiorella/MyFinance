# Test findings

The unit tests assert the behaviour the app *should* have. When the app disagrees, the test stays
red and the fix is listed here; entries are removed as they are fixed.

Run: `./gradlew :app:testDebugUnitTest` (295 tests, all passing).

## Open findings

None.

## Observed, but no test asserts either way

None. The last three (null components in the default id, `getLocalDate()` and `getPriceString()`
throwing on null fields) were closed by making `Expense`/`Income` fields non-null with defaults;
`DocumentIntegrity` reports any remote document that relies on those defaults — see the README's
testing section for how to read its log on first launch.
