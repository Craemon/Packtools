package com.craemon

import com.craemon.models.Pack
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.File

fun Application.configureRouting(packsDir: File, buildsDir: File, artifactsDir: File) {
    val jsonParser = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }

    routing {
        route("api/v1/packs") {
            get {
                val packFiles = packsDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
                val packsList = packFiles.mapNotNull { file ->
                    runCatching { jsonParser.decodeFromString<Pack>(file.readText()) }.getOrNull()
                }
                call.respond(packsList)
            }
        }
    }
}