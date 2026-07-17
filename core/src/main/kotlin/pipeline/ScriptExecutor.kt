package com.craemon.pipeline

import java.io.File

object ScriptExecutor {
    fun executeCluster(commands: List<String>, workingDir: File, environmentVariables: Map<String, String>) {
        commands.filter { it.isNotBlank() }.forEach { command ->
            try {
                println("Running build hook: $command")
                val process = ProcessBuilder(command.split(" "))
                    .directory(workingDir)
                    .apply { environment().putAll(environmentVariables) }
                    .start()

                if (process.waitFor() != 0) {
                    throw RuntimeException("Command execution exited with non-zero status.")
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to run build hook script: '$command'. Error: ${e.message}")
            }
        }
    }
}