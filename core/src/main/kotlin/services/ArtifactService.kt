package com.craemon.services

import java.io.File

class ArtifactService(private val artifactsDir: File) {

    fun getAvailableArtifacts(): List<String> {
        return artifactsDir.listFiles { file -> file.isFile }
            ?.map { it.name }
            ?.sorted() ?: emptyList()
    }

    fun purgeArtifactsDirectory() {
        artifactsDir.listFiles()?.forEach { file ->
            file.deleteRecursively()
        }
        artifactsDir.mkdirs()
    }
}