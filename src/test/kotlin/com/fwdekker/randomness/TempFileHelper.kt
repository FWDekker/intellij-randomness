package com.fwdekker.randomness

import org.junit.jupiter.api.fail
import java.io.File
import java.io.IOException


/**
 * Helper class for managing temporary files.
 *
 * Simply call [createFile] to create a new temporary file with the given contents, and call [cleanUp] to delete all
 * temporary files that have been created that way.
 */
internal class TempFileHelper {
    /**
     * The files that have been created by this helper.
     */
    private val files: MutableList<File> = mutableListOf()


    /**
     * Creates a temporary dictionary file with the given contents.
     *
     * @param contents the contents to write to the dictionary file
     * @param extension the extension of the file, including a dot
     */
    fun createFile(contents: String = "", extension: String? = null): File =
        try {
            val dictionaryFile = File.createTempFile("intellij-randomness", extension)
            files.add(dictionaryFile)
            dictionaryFile.writeText(contents)

            dictionaryFile
        } catch (e: IOException) {
            fail("Could not set up dictionary file.", e)
        }

    /**
     * Deletes all temporary files that were created.
     */
    fun cleanUp() {
        files.forEach { it.delete() }
        files.clear()
    }
}
