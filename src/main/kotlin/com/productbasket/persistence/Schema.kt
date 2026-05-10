package com.productbasket.persistence

import com.productbasket.domain.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AppData(
    val schemaVersion: Int = 1,
    val products: List<ProductDto> = emptyList(),
    val dishes: List<DishDto> = emptyList()
)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val category: String,
    val purchaseUnit: String,
    val packSize: Double,
    val pricePerPack: Double
)

@Serializable
data class DishDto(
    val id: String,
    val name: String,
    val category: String,
    val portions: Int,
    val ingredients: List<IngredientDto>
)

@Serializable
data class IngredientDto(
    val productId: String,
    val quantity: Double,
    val unit: String
)

fun Product.toDto() = ProductDto(
    id = id.toString(),
    name = name,
    category = category.name,
    purchaseUnit = purchaseUnit.name,
    packSize = packSize,
    pricePerPack = pricePerPack
)

fun ProductDto.toDomain() = Product(
    id = UUID.fromString(id),
    name = name,
    category = ProductCategory.valueOf(category),
    purchaseUnit = MeasureUnit.valueOf(purchaseUnit),
    packSize = packSize,
    pricePerPack = pricePerPack
)

fun Ingredient.toDto() = IngredientDto(
    productId = productId.toString(),
    quantity = quantity,
    unit = unit.name
)

fun IngredientDto.toDomain() = Ingredient(
    productId = UUID.fromString(productId),
    quantity = quantity,
    unit = MeasureUnit.valueOf(unit)
)

fun Dish.toDto() = DishDto(
    id = id.toString(),
    name = name,
    category = category.name,
    portions = portions,
    ingredients = ingredients.map { it.toDto() }
)

fun DishDto.toDomain() = Dish(
    id = UUID.fromString(id),
    name = name,
    category = DishCategory.valueOf(category),
    portions = portions,
    ingredients = ingredients.map { it.toDomain() }
)
