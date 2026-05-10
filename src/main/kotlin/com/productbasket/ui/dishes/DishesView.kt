package com.productbasket.ui.dishes

import com.productbasket.domain.*
import com.productbasket.ui.AppState
import com.productbasket.ui.common.*
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.transformation.FilteredList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*

fun buildDishesView(state: AppState): Region {
    val filtered = FilteredList(state.dishes)

    val categoryFilter = ComboBox<DishCategory?>().apply {
        items.add(null)
        items.addAll(DishCategory.values().toList())
        buttonCell = object : ListCell<DishCategory?>() {
            override fun updateItem(item: DishCategory?, empty: Boolean) {
                super.updateItem(item, empty)
                text = if (empty || item == null) "Все категории" else item.display
            }
        }
        setCellFactory {
            object : ListCell<DishCategory?>() {
                override fun updateItem(item: DishCategory?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) "Все категории" else item.display
                }
            }
        }
        value = null
        setOnAction {
            val cat = value
            filtered.setPredicate { d -> cat == null || d.category == cat }
        }
    }

    val table = TableView(filtered).apply {
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        placeholder = Label("Нет блюд. Нажмите «Добавить» для создания первого.")
        setOnMouseClicked { e ->
            if (e.clickCount == 2) selectionModel.selectedItem?.let { sel ->
                showDishEditor(state, sel)?.let { state.updateDish(it) }
            }
        }
    }

    // Force refresh when products change (price recalc)
    state.products.addListener(ListChangeListener { table.refresh() })

    val nameCol = TableColumn<Dish, String>("Блюдо").apply {
        setCellValueFactory { SimpleStringProperty(it.value.name) }
        minWidth = 180.0
    }
    val catCol = TableColumn<Dish, String>("Категория").apply {
        setCellValueFactory { SimpleStringProperty(it.value.category.display) }
    }
    val portionsCol = TableColumn<Dish, String>("Порций").apply {
        setCellValueFactory { SimpleStringProperty(it.value.portions.toString()) }
        style = "-fx-alignment: CENTER;"
    }
    val perPortionCol = TableColumn<Dish, String>("Цена/порция").apply {
        setCellValueFactory {
            val price = pricePerPortion(it.value, state.productsById)
            SimpleStringProperty(formatMoney(price))
        }
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    val totalCol = TableColumn<Dish, String>("Итого").apply {
        setCellValueFactory {
            val cost = dishCost(it.value, state.productsById)
            SimpleStringProperty(formatMoney(cost))
        }
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    table.columns.addAll(nameCol, catCol, portionsCol, perPortionCol, totalCol)

    val selection = table.selectionModel.selectedItemProperty()
    val nothingSelected = Bindings.isNull(selection)

    val addBtn = Button("Добавить").apply {
        setOnAction {
            showDishEditor(state)?.let { state.addDish(it) }
        }
    }
    val editBtn = Button("Изменить").apply {
        disableProperty().bind(nothingSelected)
        setOnAction {
            val sel = table.selectionModel.selectedItem ?: return@setOnAction
            showDishEditor(state, sel)?.let { state.updateDish(it) }
        }
    }
    val deleteBtn = Button("Удалить").apply {
        disableProperty().bind(nothingSelected)
        setOnAction {
            val sel = table.selectionModel.selectedItem ?: return@setOnAction
            if (confirmDelete(sel.name)) state.deleteDish(sel.id)
        }
    }

    val toolbar = HBox(8.0, categoryFilter, Region().also { HBox.setHgrow(it, Priority.ALWAYS) }, addBtn, editBtn, deleteBtn).apply {
        alignment = Pos.CENTER_LEFT
        padding = Insets(8.0)
    }

    return BorderPane().apply {
        top = toolbar
        center = table
    }
}
