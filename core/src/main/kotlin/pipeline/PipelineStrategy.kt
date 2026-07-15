package com.craemon.pipeline

import com.craemon.models.Pack
import java.io.File

interface PipelineStrategy<T : Pack> {
    fun execute(pack: T, sessionDir: File, artifactsDir: File, context: BuildContext)

    fun ensureSafePath(sandboxDir: File, targetFile: File): File {
        val canonicalSandbox = sandboxDir.canonicalPath
        val canonicalTarget = targetFile.canonicalPath

        if (!canonicalTarget.startsWith(canonicalSandbox)) {
            throw SecurityException("Sandbox Escape Blocked! Path '$canonicalTarget' attempted to modify outside of '$canonicalSandbox'")
        }
        return targetFile
    }
}