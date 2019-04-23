package com.fwdekker.randomness.word

import org.assertj.core.api.Assertions.fail
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.ArrayList
import java.util.logging.Logger


/**
 * Helper class for file manipulation for tests of [Dictionaries][Dictionary].
 */
internal class DictionaryFileHelper {
    /**
     * The files that have been created by this helper.
     */
    private val files: MutableList<File>


    /**
     * Constructs a new `DictionaryFileHelper`.
     */
    init {
        files = ArrayList()
    }


    /**
     * Writes the given contents to the given file.
     *
     * @param target   the file to write to
     * @param contents the contents to write to the file
     */
    fun writeToFile(target: File, contents: String) {
        try {
            Files.write(target.toPath(), contents.toByteArray(StandardCharsets.UTF_8))
        } catch (e: IOException) {
            fail("Could not write to dictionary file.")
        }
    }

    /**
     * Creates a temporary dictionary file with the given contents.
     *
     *
     * Because the created file is a temporary file, it does not have to be cleaned up afterwards.
     *
     * @param contents the contents to write to the dictionary file
     * @return the created temporary dictionary file
     */
    fun createDictionaryFile(contents: String) =
        try {
            val dictionaryFile = File.createTempFile("dictionary", ".dic")
            writeToFile(dictionaryFile, contents)
            files.add(dictionaryFile)

            dictionaryFile
        } catch (e: IOException) {
            fail("Could not set up dictionary file.")
            File("")
        }

    /**
     * Cleans up the created dictionary files.
     */
    fun cleanUpDictionaries() {
        files
            .filter { it.exists() && !it.delete() }
            .forEach { _ -> Logger.getLogger(this.javaClass.name).warning("Failed to clean up dictionary file.") }

        files.clear()
    }
}
