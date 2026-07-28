package com.craemon.pipeline

import java.io.File

object ScriptExecutor {
    fun executeCluster(
        commands: List<String>,
        workingDir: File,
        libraryDir: File,
        environmentVariables: Map<String, String>
    ) {
        commands.filter { it.isNotBlank() }.forEach { rawCommand ->
            try {
                val parts = Regex("""[^\s"]+|"[^"]*"?""")
                    .findAll(rawCommand)
                    .map { it.value.removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
                    .toList()

                if (parts.isEmpty()) return@forEach

                val scriptPath = parts[0]

                val executableFile = if (scriptPath.startsWith("/")) {
                    File(libraryDir, scriptPath.removePrefix("/")).normalize()
                } else {
                    File(scriptPath)
                }

                println("Running build hook: ${executableFile.absolutePath} inside staging folder: ${workingDir.absolutePath}")

                val finalCommand = listOf(executableFile.absolutePath) + parts.drop(1)

                val process = ProcessBuilder(finalCommand)
                    .directory(workingDir)
                    .apply { environment().putAll(environmentVariables) }
                    .start()

                if (process.waitFor() != 0) {
                    throw RuntimeException("Command execution exited with non-zero status code.")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to run build hook script: '$rawCommand'. Error: ${e.message}")
            }
        }
    }
}