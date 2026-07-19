package com.craemon.services

import com.craemon.config.GlobalConfigParser
import com.craemon.models.Pack
import com.craemon.pipeline.BuildContext
import com.craemon.pipeline.CentralBuildEngine
import kotlinx.serialization.json.Json
import java.io.File

class PackService(
    private val repoRoot: File,
    private val packsDir: File,
    private val buildsDir: File,
    private val artifactsDir: File,
    private val engine: CentralBuildEngine,
    private val jsonParser: Json
) {
    private val globalConfigFile = File(repoRoot, "global.config")

    fun getAllPacks(): List<Pack> {
        val packFiles = packsDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
        return packFiles.mapNotNull { file ->
            runCatching { jsonParser.decodeFromString<Pack>(file.readText()) }.getOrNull()
        }
    }

    fun getPackById(packId: String): Pack {
        val targetPackFile = File(packsDir, "$packId.json")
        if (!targetPackFile.exists() || !targetPackFile.isFile) {
            throw NoSuchElementException("Pack '$packId' not found.")
        }
        return jsonParser.decodeFromString<Pack>(targetPackFile.readText())
    }

    fun executeBuild(packId: String) {
        val pack = getPackById(packId)
        val globalSettings = GlobalConfigParser.parse(globalConfigFile)

        val context = BuildContext(globalParameters = mutableMapOf("TRIGGERED_BY" to "HTTP_API").apply {
            putAll(globalSettings)
        })

        engine.build(pack, buildsDir, artifactsDir, context)
    }
}