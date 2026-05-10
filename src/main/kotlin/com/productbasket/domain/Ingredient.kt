package com.productbasket.domain

import java.util.UUID

data class Ingredient(
    val productId: UUID,
    val quantity: Double,
    val unit: MeasureUnit
) {
    init {
        require(quantity > 0) { "Количество должно быть больше 0" }
    }
}
