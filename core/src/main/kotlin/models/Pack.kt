package com.craemon.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed interface Pack {
    val id: String
    val name: String
    val specVersion: Int
}

@Serializable
@SerialName("atomic")
data class AtomicPack(
    override val id: String,
    override val name: String,
    override val specVersion: Int,
    val version: String = "1.0.0",
    val preBuild: List<String> = emptyList(),
    val structure: Map<String, JsonElement> = emptyMap(),
    val postBuild: List<String> = emptyList()
) : Pack

@Serializable
@SerialName("list")
data class ListPack(
    override val id: String,
    override val name: String,
    override val specVersion: Int,
    val childPacks: List<Pack>
) : Pack