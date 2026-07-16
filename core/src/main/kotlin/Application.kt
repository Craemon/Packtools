package com.craemon

import com.craemon.pipeline.AtomicPipeline
import com.craemon.pipeline.CentralBuildEngine
import com.craemon.pipeline.ListPipeline
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import java.io.File

val BuildEngineAttributeKey = AttributeKey<CentralBuildEngine>("CentralBuildEngine")

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

    attributes.put(BuildEngineAttributeKey, engine)

    configureRouting(
        repoRoot = workspaceRoot,
        packsDir = packsDir,
        buildsDir = buildsDir,
        artifactsDir = artifactsDir
    )
}