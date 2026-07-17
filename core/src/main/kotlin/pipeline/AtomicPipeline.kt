package com.craemon.pipeline

import com.craemon.models.AtomicPack
import com.craemon.util.PackZipper
import kotlinx.serialization.json.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AtomicPipeline(private val libDir: File) : PipelineStrategy<AtomicPack> {

    override fun execute(pack: AtomicPack, runRoot: File, sessionDir: File, artifactsDir: File, context: BuildContext) {
        val stagingDir = resolveUniqueSandbox(sessionDir, pack.id)
        ensureSafePath(sessionDir, stagingDir)

        val executionEnvironment = mutableMapOf<String, String>().apply {
            putAll(context.globalParameters)
            put("PACK_ID", pack.id)
            put("PACK_NAME", pack.name)
            put("PACK_AUTHOR", pack.author)
            put("PACK_VERSION", pack.version)
            put("SANDBOX_NAME", stagingDir.name)
            put("BUILD_SESSION_NAME", sessionDir.name)
            put("LIB_DIR", libDir.absolutePath)
        }

        ScriptExecutor.executeCluster(pack.preBuild, stagingDir, executionEnvironment)
        unpackTree(pack.structure, stagingDir, stagingDir, libDir)
        ScriptExecutor.executeCluster(pack.postBuild, stagingDir, executionEnvironment)

        val zipFileName = resolveArtifactName(pack, executionEnvironment) + ".zip"
        val sessionZipFile = File(stagingDir.parentFile, zipFileName)
        val permanentArtifactFile = File(artifactsDir, zipFileName)

        PackZipper.archive(sourceDir = stagingDir, targetZipFile = sessionZipFile)

        artifactsDir.mkdirs()
        Files.copy(sessionZipFile.toPath(), permanentArtifactFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        BuildManifestManager.update(runRoot, pack, sessionZipFile)
    }

    private fun resolveArtifactName(pack: AtomicPack, environment: Map<String, String>): String {
        val pattern = pack.artifactNamePattern ?: return pack.id
        var resolvedName = pattern.removeSuffix(".zip")

        for ((key, value) in environment) {
            resolvedName = resolvedName.replace("{$key}", value)
        }

        return resolvedName
            .replace(Regex("[\\s]+"), "-")
            .replace(Regex("[^a-zA-Z0-9_\\-.]"), "")
    }

    private fun unpackTree(currentTree: Map<String, JsonElement>, currentStagingLocation: File, masterStagingDir: File, libraryDir: File) {
        for ((key, element) in currentTree) {
            when (element) {
                is JsonObject -> {
                    val cleanKey = key.trimEnd('/')
                    val nextSubfolder = ensureSafePath(masterStagingDir, File(currentStagingLocation, cleanKey).normalize())
                    nextSubfolder.mkdirs()
                    unpackTree(element, nextSubfolder, masterStagingDir, libraryDir)
                }
                is JsonPrimitive -> if (element.isString) {
                    val sourceFile = File(libraryDir, element.content)
                    val targetFile = ensureSafePath(masterStagingDir, File(currentStagingLocation, key).normalize())

                    if (sourceFile.exists()) {
                        targetFile.parentFile.mkdirs()
                        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    } else {
                        println("Source file asset missing from library: ${sourceFile.absolutePath}")
                    }
                }
                else -> println("Skipping unhandled layout structure key: $key")
            }
        }
    }
}