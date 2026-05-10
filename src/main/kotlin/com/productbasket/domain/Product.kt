package com.productbasket.domain

import java.util.UUID

data class Product(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val category: ProductCategory,
    val purchaseUnit: MeasureUnit,
    val packSize: Double,
    val pricePerPack: Double
) {
    init {
        require(name.isNotBlank()) { "Название продукта не может быть пустым" }
        require(packSize > 0) { "Размер упаковки должен быть больше 0" }
        require(pricePerPack >= 0) { "Цена не может быть отрицательной" }
    }
}
