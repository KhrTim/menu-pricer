package com.productbasket.ui.dishes

import com.productbasket.domain.*
import com.productbasket.ui.AppState
import com.productbasket.ui.common.*
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import java.util.UUID

fun showDishEditor(state: AppState, existing: Dish? = null): Dish? {
    val dialog = Dialog<Dish>()
    dialog.title = if (existing == null) "Новое блюдо" else "Изменить блюдо"
    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
    dialog.dialogPane.prefWidth = 600.0

    val nameField = TextField(existing?.name ?: "")
    nameField.promptText = "Название блюда"

    val categoryBox = ComboBox<DishCategory>().apply {
        items.addAll(DishCategory.values().toList())
        buttonCell = labelCell { it.display }
        setCellFactory { labelListCell { it.display } }
        value = existing?.category ?: DishCategory.MainCourse
    }

    val portionsField = TextField(existing?.portions?.toString() ?: "4")
    portionsField.promptText = "Количество порций"
    portionsField.prefWidth = 80.0

    val ingredients = FXCollections.observableArrayList(existing?.ingredients?.toMutableList() ?: mutableListOf())

    // Ingredient table
    val ingTable = TableView(ingredients).apply {
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        prefHeight = 200.0
        placeholder = Label("Нет ингредиентов. Нажмите «+ Добавить ингредиент».")
    }

    val productCol = TableColumn<Ingredient, String>("Продукт").apply {
        setCellValueFactory {
            val p = state.productsById[it.value.productId]
            SimpleStringProperty(p?.name ?: "(удалён)")
        }
        minWidth = 180.0
    }
    val qtyCol = TableColumn<Ingredient, String>("Кол-во").apply {
        setCellValueFactory { SimpleStringProperty(formatQty(it.value.quantity)) }
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    val unitCol = TableColumn<Ingredient, String>("Ед.").apply {
        setCellValueFactory { SimpleStringProperty(it.value.unit.display) }
    }
    val costCol = TableColumn<Ingredient, String>("Стоимость").apply {
        setCellValueFactory {
            val ing = it.value
            val p = state.productsById[ing.productId]
            val cost = if (p != null) ingredientCost(ing, p) else 0.0
            SimpleStringProperty(formatMoney(cost))
        }
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    ingTable.columns.addAll(productCol, qtyCol, unitCol, costCol)

    val ingSelection = ingTable.selectionModel.selectedItemProperty()

    val addIngBtn = Button("+ Добавить ингредиент").apply {
        setOnAction {
            showIngredientEditor(state, null, ingredients.map { it.productId }.toSet())?.let {
                ingredients.add(it)
            }
        }
    }
    val editIngBtn = Button("Изменить").apply {
        disableProperty().bind(javafx.beans.binding.Bindings.isNull(ingSelection))
        setOnAction {
            val sel = ingTable.selectionModel.selectedItem ?: return@setOnAction
            val idx = ingredients.indexOf(sel)
            showIngredientEditor(state, sel, ingredients.map { it.productId }.toSet() - sel.productId)?.let {
                ingredients[idx] = it
            }
        }
    }
    val removeIngBtn = Button("Удалить").apply {
        disableProperty().bind(javafx.beans.binding.Bindings.isNull(ingSelection))
        setOnAction {
            ingTable.selectionModel.selectedItem?.let { ingredients.remove(it) }
        }
    }

    val ingToolbar = HBox(8.0, addIngBtn, editIngBtn, removeIngBtn)
    ingToolbar.alignment = Pos.CENTER_LEFT

    val topGrid = GridPane().apply {
        hgap = 10.0; vgap = 8.0; padding = Insets(0.0, 0.0, 8.0, 0.0)
        add(Label("Название:"), 0, 0); add(nameField, 1, 0)
        add(Label("Категория:"), 0, 1); add(categoryBox, 1, 1)
        add(Label("Порций:"), 0, 2); add(portionsField, 1, 2)
        columnConstraints.addAll(
            ColumnConstraints(140.0),
            ColumnConstraints().also { it.hgrow = Priority.ALWAYS }
        )
    }

    val content = VBox(10.0,
        topGrid,
        Label("Ингредиенты:"),
        ingTable,
        ingToolbar
    ).apply { padding = Insets(16.0) }

    dialog.dialogPane.content = content

    val okBtn = dialog.dialogPane.lookupButton(ButtonType.OK)
    okBtn.isDisable = true
    val validate = {
        okBtn.isDisable = nameField.text.isBlank() || parsePositiveInt(portionsField.text) == null
    }
    nameField.textProperty().addListener { _, _, _ -> validate() }
    portionsField.textProperty().addListener { _, _, _ -> validate() }
    validate()

    dialog.setResultConverter { btn ->
        if (btn != ButtonType.OK) return@setResultConverter null
        val portions = parsePositiveInt(portionsField.text) ?: return@setResultConverter null
        try {
            Dish(
                id = existing?.id ?: UUID.randomUUID(),
                name = nameField.text.trim(),
                category = categoryBox.value,
                portions = portions,
                ingredients = ingredients.toList()
            )
        } catch (e: IllegalArgumentException) {
            showError(e.message ?: "Ошибка"); null
        }
    }
    return dialog.showAndWait().orElse(null)
}

private fun showIngredientEditor(state: AppState, existing: Ingredient?, excludedProductIds: Set<UUID>): Ingredient? {
    val available = state.products.filter { it.id !in excludedProductIds }
    if (available.isEmpty()) {
        showError("Нет доступных продуктов. Сначала добавьте продукты на вкладке «Продукты».")
        return null
    }

    val dialog = Dialog<Ingredient>()
    dialog.title = if (existing == null) "Добавить ингредиент" else "Изменить ингредиент"
    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

    val productBox = ComboBox<Product>().apply {
        items.addAll(available)
        buttonCell = labelCell { it.name }
        setCellFactory { labelListCell { it.name } }
        value = if (existing != null) state.productsById[existing.productId] ?: available.first()
                else available.first()
    }

    val unitBox = ComboBox<MeasureUnit>().apply {
        buttonCell = labelCell { it.display }
        setCellFactory { labelListCell { it.display } }
    }

    fun refreshUnits() {
        val p = productBox.value ?: return
        val units = MeasureUnit.forCategory(p.purchaseUnit.category)
        unitBox.items.setAll(units)
        unitBox.value = if (existing != null && existing.productId == p.id) existing.unit else p.purchaseUnit
    }
    productBox.setOnAction { refreshUnits() }
    refreshUnits()

    val qtyField = TextField(existing?.quantity?.let { formatQty(it) } ?: "")
    qtyField.promptText = "Количество"

    val grid = GridPane().apply {
        hgap = 10.0; vgap = 8.0; padding = Insets(16.0)
        add(Label("Продукт:"), 0, 0); add(productBox, 1, 0)
        add(Label("Количество:"), 0, 1); add(qtyField, 1, 1)
        add(Label("Единица измерения:"), 0, 2); add(unitBox, 1, 2)
    }
    dialog.dialogPane.content = grid
    dialog.dialogPane.prefWidth = 400.0

    val okBtn = dialog.dialogPane.lookupButton(ButtonType.OK)
    okBtn.isDisable = true
    val validate = { okBtn.isDisable = parsePositiveDouble(qtyField.text) == null }
    qtyField.textProperty().addListener { _, _, _ -> validate() }
    validate()

    dialog.setResultConverter { btn ->
        if (btn != ButtonType.OK) return@setResultConverter null
        val qty = parsePositiveDouble(qtyField.text) ?: return@setResultConverter null
        try {
            Ingredient(productId = productBox.value.id, quantity = qty, unit = unitBox.value)
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
