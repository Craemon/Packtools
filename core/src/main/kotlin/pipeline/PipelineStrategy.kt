package com.craemon.pipeline

import com.craemon.models.Pack
import java.io.File

interface PipelineStrategy<T : Pack> {
    fun execute(pack: T, runRoot: File, sessionDir: File, artifactsDir: File, context: BuildContext)

    fun ensureSafePath(sandboxDir: File, targetFile: File): File {
        val canonicalSandbox = sandboxDir.canonicalPath
        val canonicalTarget = targetFile.canonicalPath

        if (!canonicalTarget.startsWith(canonicalSandbox)) {
            throw SecurityException("Sandbox Escape Blocked! Path '$canonicalTarget' attempted to modify outside of '$canonicalSandbox'")
        }
        return targetFile
    }

    fun resolveUniqueSandbox(sessionDir: File, packId: String): File {
        var candidateDir = File(sessionDir, packId)
        if (candidateDir.exists()) {
            var counter = 1
            while (candidateDir.exists()) {
                candidateDir = File(sessionDir, "$packId-$counter")
                counter++
            }
        }
        candidateDir.mkdirs()
        return candidateDir
    }
}