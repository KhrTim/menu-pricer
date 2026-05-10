package com.productbasket.domain

import java.util.UUID

fun pricePerPurchaseUnit(p: Product): Double = p.pricePerPack / p.packSize

fun ingredientCost(ing: Ingredient, p: Product): Double {
    val qtyInPurchaseUnit = ing.unit.convertTo(ing.quantity, p.purchaseUnit)
    return pricePerPurchaseUnit(p) * qtyInPurchaseUnit
}

fun dishCost(d: Dish, byId: Map<UUID, Product>): Double =
    d.ingredients.sumOf { ing ->
        val p = byId[ing.productId] ?: return@sumOf 0.0
        ingredientCost(ing, p)
    }

fun pricePerPortion(d: Dish, byId: Map<UUID, Product>): Double =
    if (d.ingredients.isEmpty()) 0.0 else dishCost(d, byId) / d.portions
