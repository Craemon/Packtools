package com.craemon.pipeline

import com.craemon.models.ListPack
import java.io.File

class ListPipeline(private val engine: CentralBuildEngine) : PipelineStrategy<ListPack> {
    override fun execute(pack: ListPack, sessionDir: File, artifactsDir: File, context: BuildContext) {
        val listSandBoxDir = resolveUniqueSandbox(sessionDir, pack.id)
        ensureSafePath(sessionDir, listSandBoxDir)

        pack.childPacks.forEach { childPack ->
            engine.runPipeline(childPack, listSandBoxDir, artifactsDir, context)
        }
    }
}