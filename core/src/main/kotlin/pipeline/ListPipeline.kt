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

    private fun resolveUniqueSandbox(sessionDir: File, packId: String): File {
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