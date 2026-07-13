package com.craemon.pipeline

import com.craemon.models.Pack
import java.io.File

interface PipelineStrategy<T : Pack> {
    fun execute(pack: T, sessionDir: File, artifactsDir: File, context: BuildContext)
}