package com.productbasket.ui

import com.productbasket.pdf.exportDishesPdf
import com.productbasket.persistence.*
import com.productbasket.ui.common.*
import com.productbasket.ui.dishes.buildDishesView
import com.productbasket.ui.products.buildProductsView
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun buildMainView(stage: Stage, state: AppState): Region {
    val productsTab = Tab("Продукты", buildProductsView(state)).apply { isClosable = false }
    val dishesTab = Tab("Блюда", buildDishesView(state)).apply { isClosable = false }
    val tabPane = TabPane(productsTab, dishesTab)

    val menuBar = buildMenuBar(stage, state)

    return BorderPane().apply {
        top = menuBar
        center = tabPane
    }
}

private fun downloadsDir(): File {
    System.getenv("XDG_DOWNLOAD_DIR")?.let { File(it).takeIf(File::isDirectory) }?.let { return it }
    val home = File(System.getProperty("user.home"))
    return listOf("Downloads", "Загрузки").map { home.resolve(it) }.firstOrNull(File::isDirectory) ?: home
}

private fun buildMenuBar(stage: Stage, state: AppState): MenuBar {
    val jsonFilter = FileChooser.ExtensionFilter("Данные (*.json)", "*.json")
    val pdfFilter = FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")

    fun chooserOpen(vararg filters: FileChooser.ExtensionFilter) = FileChooser().apply {
        extensionFilters.addAll(*filters)
        initialDirectory = state.currentFile?.parent?.toFile() ?: downloadsDir()
    }.showOpenDialog(stage)

    fun chooserSave(ext: String, defaultName: String = "data.$ext", vararg filters: FileChooser.ExtensionFilter) = FileChooser().apply {
        extensionFilters.addAll(*filters)
        initialDirectory = state.currentFile?.parent?.toFile() ?: downloadsDir()
        initialFileName = state.currentFile?.fileName?.toString() ?: defaultName
    }.showSaveDialog(stage)

    val newItem = MenuItem("Новый").apply {
        setOnAction {
            if (state.products.isNotEmpty() || state.dishes.isNotEmpty()) {
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.title = "Новый файл"
                alert.headerText = "Создать новый файл?"
                alert.contentText = "Несохранённые изменения будут потеряны."
                if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return@setOnAction
            }
            state.new()
            stage.title = "Продуктовая корзина"
        }
    }

    val openItem = MenuItem("Открыть…").apply {
        setOnAction {
            val file = chooserOpen(jsonFilter) ?: return@setOnAction
            try {
                state.load(file.toPath())
                stage.title = "Продуктовая корзина — ${file.name}"
            } catch (e: Exception) {
                showError(e.message ?: "Ошибка при открытии файла")
            }
        }
    }

    val saveItem = MenuItem("Сохранить").apply {
        setOnAction {
            val path = state.currentFile ?: run {
                val f = chooserSave("json", "данные.json", jsonFilter) ?: return@setOnAction
                f.toPath()
            }
            try {
                state.save(path)
                stage.title = "Продуктовая корзина — ${path.fileName}"
            } catch (e: Exception) {
                showError(e.message ?: "Ошибка при сохранении")
            }
        }
    }

    val saveAsItem = MenuItem("Сохранить как…").apply {
        setOnAction {
            val file = chooserSave("json", "данные.json", jsonFilter) ?: return@setOnAction
            try {
                state.save(file.toPath())
                stage.title = "Продуктовая корзина — ${file.name}"
            } catch (e: Exception) {
                showError(e.message ?: "Ошибка при сохранении")
            }
        }
    }

    val importItem = MenuItem("Импортировать JSON…").apply {
        setOnAction {
            val file = chooserOpen(jsonFilter) ?: return@setOnAction
            try {
                state.load(file.toPath())
                showInfo("Данные импортированы из ${file.name}")
            } catch (e: Exception) {
                showError(e.message ?: "Ошибка импорта")
            }
        }
    }

    val exportItem = MenuItem("Экспортировать JSON…").apply {
        setOnAction {
            val file = chooserSave("json", "данные.json", jsonFilter) ?: return@setOnAction
            try {
                saveToFile(file.toPath(), state.products.toList(), state.dishes.toList())
                showInfo("Данные экспортированы в ${file.name}")
            } catch (e: Exception) {
                showError(e.message ?: "Ошибка экспорта")
            }
        }
    }

    val pdfItem = MenuItem("Экспортировать PDF…").apply {
        setOnAction {
            if (state.dishes.isEmpty()) {
                showError("Нет блюд для экспорта. Сначала добавьте блюда.")
                return@setOnAction
            }
            val includeIngredients = run {
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.title = "Экспорт PDF"
                alert.headerText = "Включить состав ингредиентов?"
                alert.contentText = "Нажмите OK, чтобы добавить список ингредиентов под каждым блюдом."
                alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK
            }
            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val file = chooserSave("pdf", "меню-$dateStr.pdf", pdfFilter) ?: return@setOnAction
            try {
                FileOutputStream(file).use { out ->
                    exportDishesPdf(out, state.dishes.toList(), state.productsById, includeIngredients)
                }
                showInfo("PDF сохранён: ${file.name}")
            } catch (e: Exception) {
                showError(e.message ?: "Ошибка создания PDF")
            }
        }
    }

    val exitItem = MenuItem("Выход").apply {
        setOnAction { stage.close() }
    }

    val fileMenu = Menu("Файл", null,
        newItem, openItem, saveItem, saveAsItem,
        SeparatorMenuItem(),
        importItem, exportItem,
        SeparatorMenuItem(),
        pdfItem,
        SeparatorMenuItem(),
        exitItem
    )

    return MenuBar(fileMenu)
}
