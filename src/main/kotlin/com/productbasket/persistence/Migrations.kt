package com.productbasket.persistence

import com.productbasket.domain.AppError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

private const val CURRENT_VERSION = 1

fun migrate(json: JsonObject): JsonObject {
    val version = json["schemaVersion"]?.jsonPrimitive?.int ?: 0
    if (version == CURRENT_VERSION) return json
    if (version > CURRENT_VERSION) throw AppError.UnknownSchemaVersion(version)
    // Placeholder for future migrations — chain v0→v1→v2 here as needed.
    throw AppError.UnknownSchemaVersion(version)
}
