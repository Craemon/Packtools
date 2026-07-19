package com.craemon

import com.craemon.services.BuildHistoryService
import com.craemon.services.PackService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    packService: PackService,
    buildHistoryService: BuildHistoryService
) {
    routing {
        route("api/v1") {
            route("/packs") {
                get {
                    call.respond(packService.getAllPacks())
                }

                get("/{id}") {
                    val packId = call.parameters["id"]
                    if (packId.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Missing pack ID parameter")
                        return@get
                    }

                    runCatching { packService.getPackById(packId) }
                        .onSuccess { pack -> call.respond(pack) }
                        .onFailure { call.respond(HttpStatusCode.NotFound, "Pack '$packId' not found.") }
                }

                post("/{id}/build") {
                    val packId = call.parameters["id"]
                    if (packId.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Missing pack ID parameter")
                        return@post
                    }

                    try {
                        packService.executeBuild(packId)
                        call.respond(HttpStatusCode.Accepted, mapOf(
                            "status" to "success",
                            "message" to "Build run initiated for $packId"
                        ))
                    } catch (e: NoSuchElementException) {
                        call.respond(HttpStatusCode.NotFound, e.message ?: "Pack not found")
                    } catch (e: SecurityException) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Build execution failed: ${e.message}"))
                    }
                }
            }

            route("/builds") {
                get {
                    call.respond(buildHistoryService.getAllBuildRuns())
                }

                get("/{runId}/manifest") {
                    val runId = call.parameters["runId"]
                    if (runId.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Missing runId parameter")
                        return@get
                    }

                    runCatching { buildHistoryService.getBuildManifest(runId) }
                        .onSuccess { manifest -> call.respond(manifest) }
                        .onFailure { call.respond(HttpStatusCode.NotFound, "Manifest for run '$runId' not found.") }
                }

                delete {
                    try {
                        buildHistoryService.purgeBuildDirectory()
                        call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Build history directory wiped out clean."))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to clean builds folder: ${e.message}"))
                    }
                }
            }
        }
    }
}