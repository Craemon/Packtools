package com.craemon.pipeline

import com.craemon.models.Pack
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

class CentralBuildEngine {
    @PublishedApi
    internal val strategies = mutableMapOf<KClass<out Pack>, PipelineStrategy<*>>()

    inline fun <reified T : Pack> registerStrategy(strategy: PipelineStrategy<T>) {
        strategies[T::class] = strategy
    }

    fun compile(pack:Pack, buildsDir: File, artifactsDir: File, context: BuildContext) {
        val topLevelSessionDir = generateUniquesBuildDir(buildsDir, pack.id).apply { mkdirs() }
        runPipeline(pack, topLevelSessionDir, artifactsDir, context)
    }

    fun runPipeline(pack: Pack, sessionDir: File, artifactsDir: File, context: BuildContext) {
        val strategy = getStrategyFor(pack)
        strategy.execute(pack, sessionDir, artifactsDir, context)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getStrategyFor(pack: Pack): PipelineStrategy<Pack> {
        return strategies[pack::class] as? PipelineStrategy<Pack>
            ?: throw IllegalArgumentException("No build strategy registered for pack type: ${pack::class.simpleName}")
    }

    private fun generateUniquesBuildDir(buildsDir: File, packId: String): File {
        val timeStamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now())
        var candidateDir = File("${buildsDir.absolutePath}/$packId/run-$timeStamp")

        if (candidateDir.exists()) {
            var counter = 1
            while (candidateDir.exists()) {
                candidateDir = File("${buildsDir.absolutePath}/$packId/run-$timeStamp-$counter")
                counter++
            }
        }
        return candidateDir
    }
}