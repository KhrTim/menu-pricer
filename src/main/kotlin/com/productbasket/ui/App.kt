package com.productbasket.ui

import com.productbasket.persistence.defaultDataPath
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class App : Application() {
    override fun start(stage: Stage) {
        val state = AppState()

        val autoPath = defaultDataPath()
        if (autoPath.toFile().exists()) {
            try { state.load(autoPath) } catch (_: Exception) { /* start fresh if corrupt */ }
        }

        val root = buildMainView(stage, state)
        val scene = Scene(root, 1024.0, 720.0)

        stage.title = "Продуктовая корзина"
        stage.minWidth = 800.0
        stage.minHeight = 600.0
        stage.scene = scene
        stage.show()
    }
}
