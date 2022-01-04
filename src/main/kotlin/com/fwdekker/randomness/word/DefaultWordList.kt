package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


/**
 * A list of words read from a bundled file.
 *
 * @property name The name of the file as presented and understood by the end user.
 * @property filename The location of the resource file containing the words.
 */
data class DefaultWordList(val name: String, val filename: String) {
    /**
     * The words in this list.
     *
     * @throws IOException if the resource file could not be found
     */
    @get:Throws(IOException::class)
    val words: List<String> by lazy {
        cache.getOrPut(filename) {
            (javaClass.classLoader.getResource(filename) ?: throw IOException(Bundle("word_list.error.file_not_found")))
                .openStream()
                .bufferedReader()
                .readLines()
                .filterNot { it.isBlank() }
        }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Retains word lists that have previously been looked up.
         */
        @get:Synchronized
        private val cache = ConcurrentHashMap<String, List<String>>()

        /**
         * The list of all available word lists.
         */
        val wordLists = listOf(
            DefaultWordList("Names", "word-lists/names.txt"),
            DefaultWordList("Places", "word-lists/places.txt")
        )

        /**
         * The available [wordLists] as indexed by [name].
         */
        val wordListMap = wordLists.associateBy { it.name }
    }
}
