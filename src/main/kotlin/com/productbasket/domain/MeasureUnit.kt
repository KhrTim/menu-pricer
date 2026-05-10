package com.productbasket.domain

enum class UnitCategory { Weight, Volume, Countable }

enum class MeasureUnit(val display: String, val category: UnitCategory, val toBase: Double) {
    Kilogram("кг",  UnitCategory.Weight,    1.0),
    Gram    ("г",   UnitCategory.Weight,    0.001),
    Liter   ("л",   UnitCategory.Volume,    1.0),
    Milliliter("мл",UnitCategory.Volume,    0.001),
    Piece   ("шт",  UnitCategory.Countable, 1.0);

    fun convertTo(qty: Double, target: MeasureUnit): Double {
        require(category == target.category) {
            "Несовместимые единицы: $display → ${target.display}"
        }
        return qty * toBase / target.toBase
    }

    companion object {
        fun forCategory(category: UnitCategory): List<MeasureUnit> =
            values().filter { it.category == category }
    }
}
