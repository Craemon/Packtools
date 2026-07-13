package com.craemon.pipeline

import com.craemon.models.AtomicPack
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AtomicPipeline(private val libDir: File) : PipelineStrategy<AtomicPack> {
    override fun execute(pack: AtomicPack, sessionDir: File, artifactsDir: File, context: BuildContext) {
        val stagingDir = resolveUniqueSandbox(sessionDir, pack.id)

        val executionEnvironment = mutableMapOf<String, String>().apply {
            putAll(context.globalParameters)
            put("PACK_ID", pack.id)
            put("SANDBOX_NAME", stagingDir.name)
            put("BUILD_SESSION_NAME", sessionDir.name)
            put("LIB_DIR", libDir.absolutePath)
        }

        executeScriptCluster(pack.preBuild, stagingDir, executionEnvironment)
        unpackTree(pack.structure, stagingDir)
        executeScriptCluster(pack.postBuild, stagingDir, executionEnvironment)
    }

    private fun resolveUniqueSandbox(sessionDir: File, packId: String): File {
        var candidateDir = File(sessionDir, packId)
        if (candidateDir.exists()) {
            var counter = 1
            while (candidateDir.exists()) {
                candidateDir = File(sessionDir, "$packId-$counter")
                counter++
            }
        }
        candidateDir.mkdirs()
        return candidateDir
    }

    private fun unpackTree(currentTree: Map<String, JsonElement>, currentStagingLocation: File) {
        for ((key, element) in currentTree) {
            when (element) {
                is JsonObject -> {
                    val nextSubfolder = File(currentStagingLocation, key).apply { mkdirs() }
                    unpackTree(element, nextSubfolder)
                }
                is JsonPrimitive -> {
                    if (element.isString) {
                        val sourceFile = File(libDir, element.content)
                        val targetFile = File(currentStagingLocation, key)

                        if (sourceFile.exists()) {
                            targetFile.parentFile.mkdirs()
                            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        } else {
                            println("Source file asset missing from library: ${sourceFile.absolutePath}")
                        }
                    }
                }
                else -> println("Skipping unhandled layout structure key: $key")
            }
        }
    }

    private fun executeScriptCluster(commands: List<String>, workingDir: File, environmentVariables: Map<String, String>) {
        commands.forEach { command ->
            if (command.isBlank()) return@forEach
            try {
                println("Running build hook: $command")
                val processBuilder = ProcessBuilder(command.split(" ")).directory(workingDir)
                processBuilder.environment().putAll(environmentVariables)

                val process = processBuilder.start()
                if (process.waitFor() != 0) {
                    throw RuntimeException("Command execution exited with non-zero status.")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to run build hook script: '$command'. Error: ${e.message}")
            }
        }
    }
}