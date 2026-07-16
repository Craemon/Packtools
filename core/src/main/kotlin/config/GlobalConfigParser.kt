package com.craemon.config

import java.io.File

object GlobalConfigParser {
    fun parse(configFile: File): Map<String, String> {
        val configMap = mutableMapOf<String, String>()

        if (!configFile.exists() || !configFile.isFile) {
            return configMap
        }

        configFile.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()

                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    return@forEach
                }

                val parts = trimmed.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    if (key.isNotEmpty()) {
                        configMap[key] = value
                    }
                }
            }
        }
        return configMap
    }
}