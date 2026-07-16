package com.craemon

import com.craemon.config.GlobalConfigParser
import com.craemon.models.Pack
import com.craemon.pipeline.BuildContext
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.File

fun Application.configureRouting(
    repoRoot: File,
    packsDir: File,
    buildsDir: File,
    artifactsDir: File
) {
    val jsonParser = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }
    val globalConfigFile = File(repoRoot, "global.config")

    routing {
        route("api/v1/packs") {
            get {
                val packFiles = packsDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
                val packsList = packFiles.mapNotNull { file ->
                    runCatching { jsonParser.decodeFromString<Pack>(file.readText()) }.getOrNull()
                }
                call.respond(packsList)
            }

            post("build") {
                val packId = call.request.queryParameters["id"]
                if (packId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing 'id' parameter")
                    return@post
                }

                val targetPackFile = File(packsDir, "$packId.json")
                if (!targetPackFile.exists() || !targetPackFile.isFile) {
                    call.respond(HttpStatusCode.NotFound, "Pack '$packId.json' not found.")
                    return@post
                }

                try {
                    val engine = call.application.attributes[BuildEngineAttributeKey]
                    val pack = jsonParser.decodeFromString<Pack>(targetPackFile.readText())

                    val globalSettings = GlobalConfigParser.parse(globalConfigFile)

                    val context = BuildContext(globalParameters = mutableMapOf("TRIGGERED_BY" to "HTTP_API").apply {
                        putAll(globalSettings)
                    })

                    engine.build(pack, buildsDir, artifactsDir, context)

                    call.respond(HttpStatusCode.Accepted, mapOf("status" to "success", "message" to "Build run initiated for $packId"))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Build step failed: ${e.message}"))
                }
            }
        }
    }
}