package com.productbasket.ui

import com.productbasket.domain.*
import com.productbasket.persistence.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class AppState {
    val products: ObservableList<Product> = FXCollections.observableArrayList()
    val dishes: ObservableList<Dish> = FXCollections.observableArrayList()

    var currentFile: Path? = null
        private set

    val productsById: Map<UUID, Product>
        get() = products.associateBy { it.id }

    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "autosave").also { it.isDaemon = true }
    }
    private var pendingSave: ScheduledFuture<*>? = null

    fun load(path: Path) {
        val data = loadFromFile(path)
        products.setAll(data.products)
        dishes.setAll(data.dishes)
        currentFile = path
    }

    fun save(path: Path = currentFile ?: defaultDataPath()) {
        pendingSave?.cancel(false)
        saveToFile(path, products.toList(), dishes.toList())
        currentFile = path
    }

    fun new() {
        products.clear()
        dishes.clear()
        currentFile = null
    }

    fun addProduct(p: Product) { products.add(p); scheduleAutosave() }
    fun updateProduct(p: Product) {
        val idx = products.indexOfFirst { it.id == p.id }
        if (idx >= 0) products[idx] = p
        // Update references in dishes — since Ingredient only stores productId, price recalc is automatic.
        scheduleAutosave()
    }
    fun deleteProduct(id: UUID) {
        products.removeIf { it.id == id }
        // Keep ingredients referencing deleted products — they'll show "(удалён)" in the UI.
        scheduleAutosave()
    }

    fun addDish(d: Dish) { dishes.add(d); scheduleAutosave() }
    fun updateDish(d: Dish) {
        val idx = dishes.indexOfFirst { it.id == d.id }
        if (idx >= 0) dishes[idx] = d
        scheduleAutosave()
    }
    fun deleteDish(id: UUID) { dishes.removeIf { it.id == id }; scheduleAutosave() }

    private fun scheduleAutosave() {
        val path = currentFile ?: return
        pendingSave?.cancel(false)
        pendingSave = scheduler.schedule({ saveToFile(path, products.toList(), dishes.toList()) }, 500, TimeUnit.MILLISECONDS)
    }
}
