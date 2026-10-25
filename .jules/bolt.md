## 2026-03-31 - CurrenciesCatalog size and Jetpack Compose state tick recompositions
**Learning:** `CurrenciesCatalog.allCurrencies` contains over 1000 items with pre-uppercased currency codes. Re-calling `.uppercase()` or doing multi-pass collection transformations (`map` + `partition` + `+`) on every search/selection emission allocates thousands of unnecessary objects per second. Additionally, active countdown timer ticks trigger `ConversionScreen` recomposition every second, so unremembered item callbacks in `LazyColumn` break `ConversionCard` skip-recomposition, re-triggering composition for all visible cards every second.
**Action:** Always remember callback lambdas in `ConversionScreen` for list items, and use single-pass `ArrayList` iterations for `CurrenciesCatalog` queries to keep allocations to a minimum.

## 2026-03-31 - Decoupling modal search query state from screen ConversionUiState
**Learning:** Including ephemeral search query state in a multi-flow `combine(...)` that builds `ConversionUiState` causes every character typed in the search field to recompute rate conversions and re-format string displays for all active conversion cards. Additionally, collecting sheet-specific state at top-level screen scope triggers unnecessary screen recompositions when sheet state changes.
**Action:** Expose modal search state via a dedicated `StateFlow` separate from `ConversionUiState`, and defer flow collection into the `if (showSheet)` block in Jetpack Compose.

## 2026-04-11 - Verification of pre-existing optimizations before proposing PRs
**Learning:** Adding comments explaining an optimization without code changes when the code already contains the optimization is an anti-pattern and gets rejected.
**Action:** Always verify if an intended optimization is already implemented in the source code before creating a PR.

## 2026-05-18 - Avoiding low-impact collection micro-optimizations
**Learning:** Micro-optimizations replacing standard Kotlin collection transformations (e.g. `filter`, `sortedBy`, `mapNotNull`) with index-based `for` loops in ViewModels do not yield sufficient user-perceivable performance improvements to justify code churn or risks of regression.
**Action:** Focus optimization efforts on high-impact architectural bottlenecks or actual UI/database hot paths rather than minor collection allocation micro-optimizations.
