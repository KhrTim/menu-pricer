package com.productbasket.ui.products

import com.productbasket.domain.*
import com.productbasket.ui.AppState
import com.productbasket.ui.common.*
import javafx.beans.binding.Bindings
import javafx.collections.transformation.FilteredList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*

fun buildProductsView(state: AppState): Region {
    val filtered = FilteredList(state.products)

    val categoryFilter = ComboBox<ProductCategory?>().apply {
        items.add(null)
        items.addAll(ProductCategory.values().toList())
        buttonCell = object : ListCell<ProductCategory?>() {
            override fun updateItem(item: ProductCategory?, empty: Boolean) {
                super.updateItem(item, empty)
                text = if (empty || item == null) "Все категории" else item.display
            }
        }
        setCellFactory {
            object : ListCell<ProductCategory?>() {
                override fun updateItem(item: ProductCategory?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) "Все категории" else item.display
                }
            }
        }
        value = null
        setOnAction {
            val cat = value
            filtered.setPredicate { p -> cat == null || p.category == cat }
        }
    }

    val table = TableView(filtered).apply {
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        placeholder = Label("Нет продуктов. Нажмите «Добавить» для создания первого.")
    }

    val nameCol = TableColumn<Product, String>("Название").apply {
        setCellValueFactory { it.value.name.let { n -> javafx.beans.property.SimpleStringProperty(n) } }
        minWidth = 180.0
    }
    val catCol = TableColumn<Product, String>("Категория").apply {
        setCellValueFactory { javafx.beans.property.SimpleStringProperty(it.value.category.display) }
    }
    val unitCol = TableColumn<Product, String>("Единица").apply {
        setCellValueFactory { javafx.beans.property.SimpleStringProperty(it.value.purchaseUnit.display) }
    }
    val packCol = TableColumn<Product, String>("Упаковка").apply {
        setCellValueFactory { javafx.beans.property.SimpleStringProperty(formatQty(it.value.packSize)) }
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    val priceCol = TableColumn<Product, String>("Цена за уп.").apply {
        setCellValueFactory { javafx.beans.property.SimpleStringProperty(formatMoney(it.value.pricePerPack)) }
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    val perUnitCol = TableColumn<Product, String>("Цена за ед.").apply {
        setCellValueFactory {
            val p = it.value
            val v = p.pricePerPack / p.packSize
            javafx.beans.property.SimpleStringProperty("${formatMoney(v)} / ${p.purchaseUnit.display}")
        }
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    table.columns.addAll(nameCol, catCol, unitCol, packCol, priceCol, perUnitCol)

    val selection = table.selectionModel.selectedItemProperty()
    val nothingSelected = Bindings.isNull(selection)

    val addBtn = Button("Добавить").apply {
        setOnAction {
            showProductEditor()?.let { state.addProduct(it) }
        }
    }
    val editBtn = Button("Изменить").apply {
        disableProperty().bind(nothingSelected)
        setOnAction {
            val sel = table.selectionModel.selectedItem ?: return@setOnAction
            showProductEditor(sel)?.let { state.updateProduct(it) }
        }
    }
    val deleteBtn = Button("Удалить").apply {
        disableProperty().bind(nothingSelected)
        setOnAction {
            val sel = table.selectionModel.selectedItem ?: return@setOnAction
            if (confirmDelete(sel.name)) state.deleteProduct(sel.id)
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
