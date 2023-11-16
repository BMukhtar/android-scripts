package com.example.transitiverscript

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private const val PATH =
    "C:\\Users\\15010\\AndroidStudioProjects\\jysan-android\\feature\\card\\src\\main\\java\\kz\\jysan\\business\\card\\presentation\\feature\\subsciptions"

val start = "Condition"
val end = "BundleProduct"

fun main(args: Array<String>) {
    val directory = File(PATH)

    if (directory.exists() && directory.isDirectory) {
        directory.walk().forEach { file ->
            if (file.isFile) {
                // Process the file content
                val content = file.readText()
                val newContent = content.replace(start, end).replace(start.lowercase(), end.lowercase())
                file.writeText(newContent)

                // Process the file name
                val oldFileName = file.name
                if (start in oldFileName || start.lowercase() in oldFileName) {
                    val newFileName =
                        oldFileName.replace(start, end).replace(start.lowercase(), end.lowercase())
                    val newFilePath = Paths.get(file.parent, newFileName)
                    Files.move(file.toPath(), newFilePath)
                }
            }
        }
    } else {
        println("Input is not a valid directory!")
    }
}
