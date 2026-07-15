package com.craemon.pipeline

import com.craemon.models.ListPack
import com.craemon.models.Pack
import kotlinx.serialization.json.Json
import java.io.File

class ListPipeline(
    private val engine: CentralBuildEngine,
    private val packsDir: File,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : PipelineStrategy<ListPack> {
    override fun execute(pack: ListPack, sessionDir: File, artifactsDir: File, context: BuildContext) {
        val listSandBoxDir = resolveUniqueSandbox(sessionDir, pack.id)
        ensureSafePath(sessionDir, listSandBoxDir)

        pack.childPacks.forEach { relativePath ->
            val childPackFile = ensureSafePath(packsDir, File(packsDir, relativePath).normalize())

            if (childPackFile.exists() && childPackFile.isFile) {
                try {
                    val childJsonContent = childPackFile.readText()
                    val childPack = json.decodeFromString<Pack>(childJsonContent)

                    engine.runPipeline(childPack, listSandBoxDir, artifactsDir, context)
                } catch (e: Exception) {
                    println("Failed to parse child pack configuration at '$relativePath': ${e.message}")
                }
            } else {
                println("Warning: Referenced child pack file does not exist: ${childPackFile.absolutePath}")
            }
        }
    }
}