# Menu Pricer — Agent Guide

## Project in one sentence

Kotlin + JavaFX desktop app that tracks ingredient prices and recalculates dish costs automatically. Runs on Windows 7 SP1+ via JDK 17.

## Build commands

```bash
export JAVA_HOME=~/.local/jdk17/Contents/Home   # if JDK installed without sudo
./gradlew run          # launch the app
./gradlew test         # run domain + persistence tests (33 tests, ~2s)
./gradlew compileKotlin   # compile-only check (fast)
./gradlew installDist  # build distributable layout under build/install/
```

Never use `gradle` directly — always use `./gradlew` (wrapper guarantees the right version).

## Source layout

```
src/main/kotlin/com/productbasket/
├── domain/          ← edit here for business logic changes
│   ├── MeasureUnit.kt     unit enum + convertTo()
│   ├── Categories.kt      ProductCategory, DishCategory
│   ├── Product.kt         validated data class
│   ├── Dish.kt            validated data class
│   ├── Ingredient.kt      (productId, quantity, unit) triple
│   ├── Pricing.kt         pure pricing functions (no state)
│   └── Errors.kt          sealed AppError hierarchy
├── persistence/     ← edit here for storage/format changes
│   ├── Schema.kt          @Serializable DTOs + toDto/toDomain converters
│   ├── DataFile.kt        load/save/parseJson/defaultDataPath
│   └── Migrations.kt      migrate(JsonObject) — add v→v+1 steps here
├── pdf/
│   └── DishesPdfExporter.kt
├── ui/
│   ├── AppState.kt        ObservableList<Product/Dish> + debounced auto-save
│   ├── MainView.kt        MenuBar + TabPane wiring
│   ├── App.kt             JavaFX Application entry
│   ├── products/          ProductsView + ProductEditorDialog
│   ├── dishes/            DishesView + DishEditorDialog
│   └── common/            Formatting, ConfirmDialog, showError/showInfo
└── Main.kt

src/test/kotlin/com/productbasket/
├── domain/          MeasureUnitConversionTest, PricingTest, DomainInvariantsTest
└── persistence/     DataFileRoundTripTest
```

## Key design rules

**Domain layer has zero JavaFX / IO dependency.** If you're adding code to `domain/` and importing `javafx.*` or `java.nio.*`, that's wrong — move it to `persistence/` or `ui/`.

**Unit category enforced at construction.** `Product.init {}` asserts `purchaseUnit.category == category.unitCategory`. Don't bypass this by using `copy()` with a mismatched unit — tests will catch it.

**Pricing is stateless.** `Pricing.kt` functions take `(Dish, Map<UUID, Product>)` and return a `Double`. No side effects, no caching. Easy to test, easy to reason about.

**Auto-recalc is in the UI layer only.** `DishesView` adds a `ListChangeListener` on `state.products` and calls `table.refresh()`. The dish price column cell factory calls `pricePerPortion(dish, state.productsById)` on every render. This is intentional — no observer pattern in the domain.

**Atomic writes.** `saveToFile` writes to `data.json.tmp` then renames. If you add a new save path, follow the same pattern. Never call `Files.writeString(path, text)` directly on the real file.

**schemaVersion.** Every save writes `"schemaVersion": 1`. When you change the data model:
1. Bump `CURRENT_VERSION` in `Migrations.kt`
2. Add a `version == N → N+1` migration step before the throw
3. Update `AppData` / DTOs in `Schema.kt`

## Adding a new product category

1. Add entry to `ProductCategory` enum in `Categories.kt` with the right `unitCategory`
2. That's it — the UI dropdowns, validation, and PDF all pick it up automatically

## Adding a new unit

1. Add entry to `MeasureUnit` enum in `MeasureUnit.kt` with the right `category` and `toBase` factor
2. Update `forCategory()` if needed (it uses `values().filter` so no change needed)
3. Add a `schemaVersion` migration if existing saved files might have the old enum name serialized

## Testing approach

- Domain tests only — no UI tests (TestFX is fragile and slow)
- The carrot-100₽/kg + 200g = 20₽ case is the canonical pricing sanity check; it appears in `PricingTest`
- `DataFileRoundTripTest` covers the full save→load cycle including error paths
- Run `./gradlew test` before every commit

## Windows packaging (CI)

On tag push, `.github/workflows/release-windows.yml` runs on `windows-2022` and produces:
- `ProductBasket-{version}.msi` — installer with bundled JRE (WiX 3, pre-installed on GitHub runners)
- `ProductBasket-{version}-portable.zip` — app-image zip, no install needed

The workflow uses `./gradlew installDist` (not fat JAR) so JavaFX native DLLs land in the right place for jpackage.

## What NOT to do

- Don't add `@JvmField` or other Java interop annotations to domain classes — they're Kotlin-only
- Don't make `AppState` a singleton — it's passed explicitly to UI functions
- Don't catch `Exception` broadly in domain code — let `AppError` subtypes propagate to the UI layer
- Don't add i18n framework — all strings are in `ui/common/Formatting.kt` and inline in each file; if needed later, extract to a `Strings.kt` object
