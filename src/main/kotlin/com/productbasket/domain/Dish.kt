package com.productbasket.domain

import java.util.UUID

data class Dish(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val category: DishCategory,
    val portions: Int,
    val ingredients: List<Ingredient>
) {
    init {
        require(name.isNotBlank()) { "Название блюда не может быть пустым" }
        require(portions >= 1) { "Количество порций должно быть не менее 1" }
    }
}
