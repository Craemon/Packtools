package com.craemon.pipeline

import com.craemon.models.AtomicPack
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File

object BuildManifestManager {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Synchronized
    fun update(runRoot: File, pack: AtomicPack, zipFile: File) {
        val targetManifest = File(runRoot, "build-manifest.json")

        val currentArray = if (targetManifest.exists()) {
            runCatching {
                json.decodeFromString<JsonArray>(targetManifest.readText())
            }.getOrElse { JsonArray(emptyList()) }
        } else {
            JsonArray(emptyList())
        }

        val relativeZipPath = "/" + runRoot.toPath()
            .relativize(zipFile.toPath())
            .toString()
            .replace(File.separatorChar, '/')

        val entry = JsonObject(mapOf(
            "id" to JsonPrimitive(pack.id),
            "version" to JsonPrimitive(pack.version),
            "path" to JsonPrimitive(relativeZipPath),
            "characteristics" to JsonArray(pack.characteristics.map { JsonPrimitive(it) })
        ))

        val updatedList = currentArray.toMutableList().apply { add(entry) }
        targetManifest.writeText(json.encodeToString(JsonArray(updatedList)))
    }
}