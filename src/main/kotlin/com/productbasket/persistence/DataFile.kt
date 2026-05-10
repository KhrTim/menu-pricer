package com.productbasket.persistence

import com.productbasket.domain.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

data class LoadedData(val products: List<Product>, val dishes: List<Dish>)

fun loadFromFile(path: Path): LoadedData {
    if (!Files.exists(path)) throw AppError.FileNotFound(path.toString())
    val text = try {
        Files.readString(path)
    } catch (e: Exception) {
        throw AppError.ParseError("Не удалось прочитать файл", e)
    }
    return parseJson(text)
}

fun parseJson(text: String): LoadedData {
    val root: JsonObject = try {
        Json.parseToJsonElement(text).jsonObject
    } catch (e: Exception) {
        throw AppError.ParseError("Неверный формат JSON", e)
    }
    val migrated = migrate(root)
    val data: AppData = try {
        json.decodeFromJsonElement(AppData.serializer(), migrated)
    } catch (e: Exception) {
        throw AppError.ParseError("Ошибка разбора данных: ${e.message}", e)
    }
    return LoadedData(
        products = data.products.map { it.toDomain() },
        dishes = data.dishes.map { it.toDomain() }
    )
}

fun saveToFile(path: Path, products: List<Product>, dishes: List<Dish>) {
    val data = AppData(
        products = products.map { it.toDto() },
        dishes = dishes.map { it.toDto() }
    )
    val text = json.encodeToString(AppData.serializer(), data)
    // Atomic write: write to .tmp, then rename over the real file
    val tmp = path.resolveSibling(path.fileName.toString() + ".tmp")
    Files.createDirectories(path.parent)
    Files.writeString(tmp, text)
    Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
}

fun defaultDataPath(): Path {
    val os = System.getProperty("os.name", "").lowercase()
    val base = if (os.contains("win")) {
        Path.of(System.getenv("APPDATA") ?: System.getProperty("user.home"), "ProductBasket")
    } else if (os.contains("mac")) {
        Path.of(System.getProperty("user.home"), "Library", "Application Support", "ProductBasket")
    } else {
        Path.of(System.getProperty("user.home"), ".config", "ProductBasket")
    }
    return base.resolve("data.json")
}
