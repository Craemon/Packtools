package com.craemon.services

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import java.io.File

class BuildHistoryService(
    private val buildsDir: File,
    private val jsonParser: Json
) {

    fun getAllBuildRuns(): List<String> {
        val runs = buildsDir.listFiles { f -> f.isDirectory && f.name.startsWith("run-") } ?: emptyArray()
        return runs
            .map { it.name }
            .sortedDescending()
    }

    fun getBuildManifest(runId: String): JsonArray {
        val cleanRunId = runId.replace(Regex("[^a-zA-Z0-9_\\-]"), "")
        val manifestFile = File(buildsDir, "$cleanRunId/build-manifest.json")

        if (!manifestFile.exists() || !manifestFile.isFile) {
            throw NoSuchElementException("Manifest for run '$runId' not found.")
        }

        return jsonParser.decodeFromString<JsonArray>(manifestFile.readText())
    }

    fun purgeBuildDirectory() {
        buildsDir.listFiles()?.forEach { file ->
            file.deleteRecursively()
        }
        buildsDir.mkdirs()
    }
}