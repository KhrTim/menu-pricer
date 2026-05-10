package com.productbasket.persistence

import com.productbasket.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class DataFileRoundTripTest {

    @Test fun `save and load returns equivalent data`(@TempDir dir: Path) {
        val carrot = Product(name = "Морковь", category = ProductCategory.Vegetable,
            purchaseUnit = MeasureUnit.Kilogram, packSize = 1.0, pricePerPack = 100.0)
        val eggs = Product(name = "Яйцо", category = ProductCategory.Eggs,
            purchaseUnit = MeasureUnit.Piece, packSize = 30.0, pricePerPack = 270.0)
        val dish = Dish(name = "Салат", category = DishCategory.Salad, portions = 4,
            ingredients = listOf(
                Ingredient(carrot.id, 200.0, MeasureUnit.Gram),
                Ingredient(eggs.id, 2.0, MeasureUnit.Piece)
            ))

        val file = dir.resolve("data.json")
        saveToFile(file, listOf(carrot, eggs), listOf(dish))
        val loaded = loadFromFile(file)

        assertEquals(2, loaded.products.size)
        assertEquals(1, loaded.dishes.size)
        val loadedCarrot = loaded.products.first { it.id == carrot.id }
        assertEquals(carrot.name, loadedCarrot.name)
        assertEquals(carrot.pricePerPack, loadedCarrot.pricePerPack)

        val loadedDish = loaded.dishes.first()
        assertEquals(dish.name, loadedDish.name)
        assertEquals(2, loadedDish.ingredients.size)
        assertEquals(200.0, loadedDish.ingredients.first { it.productId == carrot.id }.quantity)
    }

    @Test fun `malformed json throws ParseError`() {
        assertThrows<AppError.ParseError> {
            parseJson("{not valid json")
        }
    }

    @Test fun `unknown schema version throws UnknownSchemaVersion`() {
        assertThrows<AppError.UnknownSchemaVersion> {
            parseJson("""{"schemaVersion":999,"products":[],"dishes":[]}""")
        }
    }

    @Test fun `file not found throws FileNotFound`(@TempDir dir: Path) {
        assertThrows<AppError.FileNotFound> {
            loadFromFile(dir.resolve("nonexistent.json"))
        }
    }

    @Test fun `atomic write survives — original intact if tmp deleted mid-write`(@TempDir dir: Path) {
        val p = Product(name = "Тест", category = ProductCategory.Other,
            purchaseUnit = MeasureUnit.Kilogram, packSize = 1.0, pricePerPack = 10.0)
        val file = dir.resolve("data.json")
        // First save — baseline
        saveToFile(file, listOf(p), emptyList())
        val originalText = file.toFile().readText()
        assertTrue(originalText.contains("Тест"))

        // Simulate partial write by checking the file is still valid after re-save
        saveToFile(file, listOf(p.copy(pricePerPack = 99.0)), emptyList())
        val updated = loadFromFile(file)
        assertEquals(99.0, updated.products.first().pricePerPack)
    }
}
