package com.craemon

import com.craemon.pipeline.AtomicPipeline
import com.craemon.pipeline.CentralBuildEngine
import com.craemon.pipeline.ListPipeline
import com.craemon.services.ArtifactService
import com.craemon.services.BuildHistoryService
import com.craemon.services.PackService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import java.io.File

fun Application.module() {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    install(ContentNegotiation) {
        json(jsonConfig)
    }

    val userConfigFile = File(System.getProperty("user.home"), ".config/packtools/config.yaml")

    val repoRootPath = parseRepoRoot(userConfigFile) ?: "."

    val workspaceRoot = File(repoRootPath).canonicalFile

    if (workspaceRoot == File(System.getProperty("user.home")).canonicalFile || workspaceRoot.parent == null) {
        throw IllegalStateException("Refusing to run Packtools directly on user home or root directory: ${workspaceRoot.absolutePath}")
    }

    log.info("Packtools Core operating on repository root: ${workspaceRoot.absolutePath}")

    val packsDir = File(workspaceRoot, "packs")
    val libDir = File(workspaceRoot, "lib")
    val buildsDir = File(workspaceRoot, "builds").apply { mkdirs() }
    val artifactsDir = File(workspaceRoot, "artifacts").apply { mkdirs() }

    val engine = CentralBuildEngine().apply {
        registerStrategy(AtomicPipeline(libDir = libDir))
        registerStrategy(ListPipeline(engine = this, packsDir = packsDir, json = jsonConfig))
    }

    val packService = PackService(
        repoRoot = workspaceRoot,
        packsDir = packsDir,
        buildsDir = buildsDir,
        artifactsDir = artifactsDir,
        engine = engine,
        jsonParser = jsonConfig
    )

    val buildHistoryService = BuildHistoryService(
        buildsDir = buildsDir,
        jsonParser = jsonConfig
    )

    val artifactService = ArtifactService(artifactsDir = artifactsDir)

    configureRouting(packService, buildHistoryService, artifactService)
}

private fun parseRepoRoot(file: File): String? {
    if (!file.exists()) return null
    return file.useLines { lines ->
        lines.firstOrNull { it.trim().startsWith("repoRoot:") }
            ?.substringAfter("repoRoot:")
            ?.substringBefore("#") // Strip inline comments
            ?.trim()
            ?.removeSurrounding("\"")
            ?.removeSurrounding("'")
            ?.takeIf { it.isNotBlank() } // Return null if value was empty
    }
}