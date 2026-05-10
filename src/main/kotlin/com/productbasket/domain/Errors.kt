package com.productbasket.domain

sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class FileNotFound(path: String) : AppError("Файл не найден: $path")
    class ParseError(detail: String, cause: Throwable? = null) : AppError("Ошибка чтения данных: $detail", cause)
    class UnknownSchemaVersion(version: Int) : AppError("Неизвестная версия формата данных: $version")
    class ValidationError(detail: String) : AppError(detail)
}
