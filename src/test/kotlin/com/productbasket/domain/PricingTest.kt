package com.productbasket.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PricingTest {

    private fun carrot(pricePerKg: Double = 100.0) = Product(
        name = "Морковь", category = ProductCategory.Vegetable,
        purchaseUnit = MeasureUnit.Kilogram, packSize = 1.0, pricePerPack = pricePerKg
    )

    private fun eggs(pricePerCarton: Double = 270.0) = Product(
        name = "Яйцо", category = ProductCategory.Eggs,
        purchaseUnit = MeasureUnit.Piece, packSize = 30.0, pricePerPack = pricePerCarton
    )

    private fun milk(pricePerLiter: Double = 80.0) = Product(
        name = "Молоко", category = ProductCategory.Liquid,
        purchaseUnit = MeasureUnit.Liter, packSize = 1.0, pricePerPack = pricePerLiter
    )

    private fun dish(vararg ings: Ingredient, portions: Int = 4) = Dish(
        name = "Тест", category = DishCategory.MainCourse,
        portions = portions, ingredients = ings.toList()
    )

    @Test fun `carrot 100 per kg 200g costs 20 rub`() {
        val c = carrot(100.0)
        val ing = Ingredient(c.id, 200.0, MeasureUnit.Gram)
        assertEquals(20.0, ingredientCost(ing, c), 1e-9)
    }

    @Test fun `price per portion divides by portions`() {
        val c = carrot(100.0)
        val d = dish(Ingredient(c.id, 200.0, MeasureUnit.Gram), portions = 4)
        assertEquals(5.0, pricePerPortion(d, mapOf(c.id to c)), 1e-9)
    }

    @Test fun `eggs 30-pack 270 rub 2 eggs cost 18 rub`() {
        val e = eggs(270.0)
        val ing = Ingredient(e.id, 2.0, MeasureUnit.Piece)
        assertEquals(18.0, ingredientCost(ing, e), 1e-9)
    }

    @Test fun `milk 80 per liter 250ml costs 20 rub`() {
        val m = milk(80.0)
        val ing = Ingredient(m.id, 250.0, MeasureUnit.Milliliter)
        assertEquals(20.0, ingredientCost(ing, m), 1e-9)
    }

    @Test fun `empty dish costs zero`() {
        val c = carrot()
        val d = dish(portions = 1)
        assertEquals(0.0, pricePerPortion(d, mapOf(c.id to c)), 1e-9)
    }

    @Test fun `multi-ingredient sum`() {
        val c = carrot(100.0)
        val e = eggs(270.0)
        val d = dish(
            Ingredient(c.id, 200.0, MeasureUnit.Gram),   // 20 rub
            Ingredient(e.id, 3.0, MeasureUnit.Piece),     // 27 rub
            portions = 1
        )
        assertEquals(47.0, pricePerPortion(d, mapOf(c.id to c, e.id to e)), 1e-9)
    }

    @Test fun `missing product returns 0 for that ingredient`() {
        val c = carrot()
        val d = dish(Ingredient(c.id, 1.0, MeasureUnit.Kilogram), portions = 1)
        // empty map — product is deleted
        assertEquals(0.0, pricePerPortion(d, emptyMap()), 1e-9)
    }

    @Test fun `price update reflected in dish cost`() {
        val c = carrot(100.0)
        val c2 = c.copy(pricePerPack = 120.0)
        val ing = Ingredient(c.id, 200.0, MeasureUnit.Gram)
        val d = dish(ing, portions = 1)
        assertEquals(20.0, dishCost(d, mapOf(c.id to c)), 1e-9)
        assertEquals(24.0, dishCost(d, mapOf(c.id to c2)), 1e-9)
    }
}
