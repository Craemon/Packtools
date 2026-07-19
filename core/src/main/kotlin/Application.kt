package com.craemon

import com.craemon.pipeline.AtomicPipeline
import com.craemon.pipeline.CentralBuildEngine
import com.craemon.pipeline.ListPipeline
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

    val repoRootPath = environment.config.propertyOrNull("ktor.deployment.repoRoot")?.getString() ?: "."
    val workspaceRoot = File(repoRootPath)

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
    
    configureRouting(packService)
}