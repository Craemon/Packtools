package com.craemon.util

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PackZipper {
    fun archive (sourceDir: File, targetZipFile: File) {
        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            throw IllegalArgumentException("Source directory does not exist or is not a directory: ${sourceDir.absolutePath}")
        }

        targetZipFile.parentFile?.mkdirs()

        ZipOutputStream(FileOutputStream(targetZipFile)).use { zipOut ->
            val sourcePath = sourceDir.toPath()

            sourceDir.walkTopDown().forEach { file ->
                if (file != sourceDir) {
                    val relativePath = sourcePath.relativize(file.toPath()).toString()

                    // Needs to be done because of some bullshit of a "design decision" in windows
                    val zipEntryName = if (File.separatorChar != '/') {
                        relativePath.replace(File.separatorChar, '/')
                    } else {
                        relativePath
                    }

                    if (file.isDirectory) {
                        zipOut.putNextEntry(ZipEntry("$zipEntryName/"))
                        zipOut.closeEntry()
                    } else {
                        zipOut.putNextEntry(ZipEntry(zipEntryName))
                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }
}