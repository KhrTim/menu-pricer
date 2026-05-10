# Menu Pricer

Desktop app for tracking product prices and computing dish costs. Change the price of any ingredient and all dish totals update instantly. Export a printable PDF price table in one click.

Runs on **Windows 7 SP1+**, macOS, and Linux. Developed on Mac.

---

## Features

- **Products** — add/edit/delete with name, category, purchase unit, pack size, and price
- **Dishes** — assemble from products; each dish shows cost per portion and total cost
- **Live price recalculation** — edit a product price → every dish that uses it updates immediately
- **Smart units** — available units depend on product type: liquids get L/мл, eggs get шт, everything else gets кг/г. Recipes can use any unit in the same category (buy карrot per kg, use 200 г in the dish)
- **PDF export** — paginated table of all dishes with optional per-dish ingredient breakdown
- **JSON import/export** — human-readable, hand-editable data format
- **Auto-save** — data is written to disk automatically; atomic writes prevent data corruption on crash

---

## Getting Started (macOS)

### 1. Install JDK 17

```bash
# Option A: Homebrew (needs sudo)
brew install --cask temurin@17

# Option B: no sudo (portable install)
mkdir -p ~/.local/jdk17
curl -L "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_aarch64_mac_hotspot_17.0.10_7.tar.gz" \
  | tar -xz -C ~/.local/jdk17 --strip-components=1
export JAVA_HOME=~/.local/jdk17/Contents/Home
```

### 2. Run

```bash
git clone git@github.com:KhrTim/menu-pricer.git
cd menu-pricer
./gradlew run
```

### 3. Test

```bash
./gradlew test
```

Test report: `build/reports/tests/test/index.html`

---

## Windows Distribution

### Via GitHub Actions (recommended)

Push a version tag and the release workflow builds and attaches a `.msi` installer automatically:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The workflow produces:
- `ProductBasket-1.0.0.msi` — standard Windows installer (includes bundled JRE, no Java install needed by the user)
- `ProductBasket-1.0.0-portable.zip` — zip with a self-contained app image, runs without installing

### Local Windows build (optional)

On a Windows machine with JDK 17 and WiX 3 installed:

```bat
gradlew.bat installDist
jpackage --input build\install\product-basket\lib ^
         --main-jar product-basket-1.0.0.jar ^
         --main-class com.productbasket.MainKt ^
         --name ProductBasket --app-version 1.0.0 ^
         --type msi --win-dir-chooser --win-menu --win-shortcut ^
         --dest dist
```

---

## Data Format

Data is stored as a single JSON file:

- **Windows:** `%APPDATA%\ProductBasket\data.json`
- **macOS:** `~/Library/Application Support/ProductBasket/data.json`
- **Linux:** `~/.config/ProductBasket/data.json`

The file is also what gets exported/imported via **File → Export JSON** and **File → Import JSON**.

```json
{
  "schemaVersion": 1,
  "products": [
    {
      "id": "...",
      "name": "Морковь",
      "category": "Vegetable",
      "purchaseUnit": "Kilogram",
      "packSize": 1.0,
      "pricePerPack": 100.0
    },
    {
      "id": "...",
      "name": "Яйцо",
      "category": "Eggs",
      "purchaseUnit": "Piece",
      "packSize": 30.0,
      "pricePerPack": 270.0
    }
  ],
  "dishes": [
    {
      "id": "...",
      "name": "Морковный салат",
      "category": "Salad",
      "portions": 4,
      "ingredients": [
        { "productId": "...", "quantity": 200.0, "unit": "Gram" }
      ]
    }
  ]
}
```

`schemaVersion` enables forward-compatible migrations as the format evolves.

---

## Architecture

```
src/main/kotlin/com/productbasket/
├── domain/          Pure Kotlin — no JavaFX, no I/O; fully unit-tested
│   ├── MeasureUnit  Unit enum (кг/г/л/мл/шт) with typed conversion
│   ├── Categories   ProductCategory + DishCategory enums
│   ├── Product      Validated data class (rejects mismatched unit/category)
│   ├── Dish         Data class with ingredient list
│   ├── Ingredient   (productId, quantity, unit) triple
│   ├── Pricing      pricePerPortion / dishCost / ingredientCost functions
│   └── Errors       Sealed AppError hierarchy
├── persistence/     Atomic JSON file I/O + migration chain
├── pdf/             OpenPDF dish-cost table exporter
└── ui/              JavaFX — AppState (ObservableList) + TabPane views
    ├── products/    TableView + ProductEditorDialog
    └── dishes/      TableView + DishEditorDialog (inline ingredient editor)
```

**Pricing example:**
- Carrot: 100 ₽ / 1 kg → 100 ₽ per kg
- Recipe uses 200 г → `200 г × (0.001 kg/г) × (100 ₽/kg)` = **20 ₽**
- Dish has 4 portions → **5 ₽ per portion**

---

## Tech Stack

| Component | Choice | Why |
|-----------|--------|-----|
| Language | Kotlin 1.9 | Concise, null-safe, great data classes |
| UI | JavaFX 17 LTS | Ships with JDK, runs on Win 7 SP1+ |
| Serialization | kotlinx.serialization | Compile-time, no reflection |
| PDF | OpenPDF 1.3 (LGPL) | JDK 8+ compatible, actively maintained |
| Tests | JUnit 5 | Standard, well-supported |
| Build | Gradle 8 + Kotlin DSL | Reproducible, wrapper included |
| Packaging | jpackage (JDK 14+) | Native installers, bundled JRE |
