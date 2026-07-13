package com.craemon

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Pack(
    val id: String,
    val name: String,
    val version: String,
    val status: String
)

val availablePacks = listOf(
    Pack("p1", "Base Utilities", "1.0.0", "ready"),
    Pack("p2", "Network Tools", "2.1.4", "archived"),
    Pack("p3", "Crypto Extension", "0.9.2", "beta")
)

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        route("api/v1") {
            route("/packs") {
                get {
                    call.respond(availablePacks)
                }
            }
        }
    }
}