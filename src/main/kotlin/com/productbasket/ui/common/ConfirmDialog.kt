package com.productbasket.ui.common

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

fun confirmDelete(itemName: String): Boolean {
    val alert = Alert(Alert.AlertType.CONFIRMATION)
    alert.title = "Удаление"
    alert.headerText = "Удалить «$itemName»?"
    alert.contentText = "Это действие нельзя отменить."
    return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK
}

fun showError(message: String) {
    val alert = Alert(Alert.AlertType.ERROR)
    alert.title = "Ошибка"
    alert.headerText = null
    alert.contentText = message
    alert.showAndWait()
}

fun showInfo(message: String) {
    val alert = Alert(Alert.AlertType.INFORMATION)
    alert.title = "Готово"
    alert.headerText = null
    alert.contentText = message
    alert.showAndWait()
}
