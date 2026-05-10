package com.productbasket.ui.products

import com.productbasket.domain.*
import com.productbasket.ui.common.*
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import java.util.UUID

fun showProductEditor(existing: Product? = null): Product? {
    val dialog = Dialog<Product>()
    dialog.title = if (existing == null) "Новый продукт" else "Изменить продукт"
    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

    val nameField = TextField(existing?.name ?: "")
    nameField.promptText = "Например: Морковь"

    val categoryBox = ComboBox<ProductCategory>().apply {
        items.addAll(ProductCategory.values().toList())
        buttonCell = labelCell { it.display }
        setCellFactory { labelListCell { it.display } }
        value = existing?.category ?: ProductCategory.Vegetable
    }

    val unitBox = ComboBox<MeasureUnit>().apply {
        buttonCell = labelCell { it.display }
        setCellFactory { labelListCell { it.display } }
    }

    fun refreshUnits() {
        val cat = categoryBox.value ?: return
        val units = MeasureUnit.forCategory(cat.unitCategory)
        unitBox.items.setAll(units)
        unitBox.value = if (existing?.category == cat) existing.purchaseUnit else units.first()
    }
    categoryBox.setOnAction { refreshUnits() }
    refreshUnits()

    val packSizeField = TextField(existing?.packSize?.let { formatQty(it) } ?: "1")
    packSizeField.promptText = "1, 30, 0.5 ..."

    val priceField = TextField(existing?.pricePerPack?.let { formatQty(it) } ?: "")
    priceField.promptText = "Цена за упаковку, ₽"

    val grid = GridPane().apply {
        hgap = 10.0; vgap = 8.0; padding = Insets(16.0)
        add(Label("Название:"), 0, 0); add(nameField, 1, 0)
        add(Label("Категория:"), 0, 1); add(categoryBox, 1, 1)
        add(Label("Единица покупки:"), 0, 2); add(unitBox, 1, 2)
        add(Label("Размер упаковки:"), 0, 3); add(packSizeField, 1, 3)
        add(Label("Цена за упаковку (₽):"), 0, 4); add(priceField, 1, 4)
    }
    dialog.dialogPane.content = grid
    dialog.dialogPane.prefWidth = 420.0

    val okBtn = dialog.dialogPane.lookupButton(ButtonType.OK)
    okBtn.isDisable = true
    val validate = {
        okBtn.isDisable = nameField.text.isBlank() ||
            parsePositiveDouble(packSizeField.text) == null ||
            parsePositiveDouble(priceField.text) == null
    }
    nameField.textProperty().addListener { _, _, _ -> validate() }
    packSizeField.textProperty().addListener { _, _, _ -> validate() }
    priceField.textProperty().addListener { _, _, _ -> validate() }
    validate()

    dialog.setResultConverter { btn ->
        if (btn != ButtonType.OK) return@setResultConverter null
        val packSize = parsePositiveDouble(packSizeField.text) ?: return@setResultConverter null
        val price = parsePositiveDouble(priceField.text) ?: return@setResultConverter null
        try {
            Product(
                id = existing?.id ?: UUID.randomUUID(),
                name = nameField.text.trim(),
                category = categoryBox.value,
                purchaseUnit = unitBox.value,
                packSize = packSize,
                pricePerPack = price
            )
        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Ошибка"); null
        }
    }
    return dialog.showAndWait().orElse(null)
}

private fun <T> labelCell(label: (T) -> String): ListCell<T> = object : ListCell<T>() {
    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (empty || item == null) "" else label(item)
    }
}

private fun <T> labelListCell(label: (T) -> String): ListCell<T> = labelCell(label)
