package com.productbasket.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DomainInvariantsTest {

    @Test fun `product accepts any unit in any category`() {
        assertDoesNotThrow {
            Product(name = "Масло", category = ProductCategory.Oil,
                purchaseUnit = MeasureUnit.Gram, packSize = 1.0, pricePerPack = 50.0)
            Product(name = "Яйцо", category = ProductCategory.Eggs,
                purchaseUnit = MeasureUnit.Kilogram, packSize = 1.0, pricePerPack = 50.0)
            Product(name = "Молоко", category = ProductCategory.Dairy,
                purchaseUnit = MeasureUnit.Liter, packSize = 1.0, pricePerPack = 80.0)
        }
    }

    @Test fun `product rejects blank name`() {
        assertThrows(IllegalArgumentException::class.java) {
            Product(name = "  ", category = ProductCategory.Vegetable,
                purchaseUnit = MeasureUnit.Kilogram, packSize = 1.0, pricePerPack = 50.0)
        }
    }

    @Test fun `product rejects zero pack size`() {
        assertThrows(IllegalArgumentException::class.java) {
            Product(name = "Яблоко", category = ProductCategory.Fruit,
                purchaseUnit = MeasureUnit.Kilogram, packSize = 0.0, pricePerPack = 80.0)
        }
    }

    @Test fun `product rejects negative price`() {
        assertThrows(IllegalArgumentException::class.java) {
            Product(name = "Яблоко", category = ProductCategory.Fruit,
                purchaseUnit = MeasureUnit.Kilogram, packSize = 1.0, pricePerPack = -10.0)
        }
    }

    @Test fun `product allows zero price`() {
        assertDoesNotThrow {
            Product(name = "Вода", category = ProductCategory.Liquid,
                purchaseUnit = MeasureUnit.Liter, packSize = 1.0, pricePerPack = 0.0)
        }
    }

    @Test fun `dish rejects zero portions`() {
        assertThrows(IllegalArgumentException::class.java) {
            Dish(name = "Суп", category = DishCategory.Soup, portions = 0, ingredients = emptyList())
        }
    }

    @Test fun `dish rejects blank name`() {
        assertThrows(IllegalArgumentException::class.java) {
            Dish(name = "", category = DishCategory.Soup, portions = 1, ingredients = emptyList())
        }
    }

    @Test fun `ingredient rejects zero quantity`() {
        val p = Product(name = "Морковь", category = ProductCategory.Vegetable,
            purchaseUnit = MeasureUnit.Kilogram, packSize = 1.0, pricePerPack = 100.0)
        assertThrows(IllegalArgumentException::class.java) {
            Ingredient(productId = p.id, quantity = 0.0, unit = MeasureUnit.Gram)
        }
    }
}
