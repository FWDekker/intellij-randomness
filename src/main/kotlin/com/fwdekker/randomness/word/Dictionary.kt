package com.fwdekker.randomness.word

import com.intellij.openapi.ui.ValidationInfo
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.HashSet
import java.util.Objects
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


/**
 * A dictionary of English words.
 *
 * @param uid   the unique identifier of the dictionary
 * @param name  the human-readable name of the dictionary
 * @param words the words in the dictionary
 */
// TODO Make this whole thing more Kotlin-like
// TODO Fix ugly exception catching and throwing\
abstract class Dictionary(
    val uid: String = UUID.randomUUID().toString(),
    val name: String = uid,
    val words: MutableSet<String> = HashSet() // TODO prevent outsider from changing set directly?
) {
    /**
     * The shortest word in this `Dictionary`.
     */
    val shortestWord: String
        get() = words.minBy { it.length }
            ?: throw IllegalStateException("Dictionary should not be empty.")

    /**
     * The longest word in this `Dictionary`.
     */
    val longestWord: String
        get() = words.maxBy { it.length }
            ?: throw IllegalStateException("Dictionary should not be empty.")


    /**
     * Returns a list of all words with a length in the given range.
     *
     * @param minLength the minimum word length (inclusive)
     * @param maxLength the maximum word length (inclusive)
     * @return a list of all words with a length in the given range
     */
    fun getWordsWithLengthInRange(minLength: Int, maxLength: Int) =
        words.filter { word -> word.length in minLength..maxLength }

    /**
     * Detects whether this dictionary is still valid.
     *
     * Depending on the underlying model, the dictionary may become invalid or even disappear. This method detects such
     * problems.
     *
     * @return `null` if this dictionary is valid, or a `ValidationInfo` explaining why it is invalid
     */
    abstract fun validate(): ValidationInfo?


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }

        val that = other as Dictionary?
        return this.uid == that!!.uid
    }

    override fun hashCode(): Int {
        return Objects.hash(uid)
    }


    /**
     * A `Dictionary` provided by the plugin.
     *
     * Bundled dictionaries are found in the application's resources.
     *
     * @param path the path to the dictionary resource
     */
    // TODO Private constructor?
    class BundledDictionary(path: String) : Dictionary(path, File(path).name, fromInputStream(path).toMutableSet()) {
        override fun validate() = validate(uid)

        override fun toString() = "[bundled] $name"


        companion object {
            /**
             * A cache of previously created `BundledDictionary(s)`.
             */
            private val CACHE = ConcurrentHashMap<String, BundledDictionary>()

            /**
             * Constructs a new `BundledDictionary` for the given dictionary resource, or returns the previously
             * created instance for this resource if there is one.
             *
             * @param path     the path to the dictionary resource
             * @param useCache `true` if a cached version of the dictionary should be returned if it exists,
             * `false` if the cache should always be updated
             * @return a new `BundledDictionary` for the given dictionary resource, or the previously created instance
             * of this dictionary if there is one
             */
            fun get(path: String, useCache: Boolean = true): BundledDictionary =
                if (useCache) {
                    CACHE.getOrPut(path) { BundledDictionary(path) }
                } else {
                    BundledDictionary(path).also { CACHE[path] = it }
                }

            /**
             * Clears the cache of stored dictionaries.
             */
            fun clearCache() = CACHE.clear()


            /**
             * Detects whether the dictionary at the given resource would be valid.
             *
             * @param path the path to the dictionary resource
             * @return `null` if the dictionary would be valid, or a `ValidationInfo` explaining why it would be
             * invalid
             */
            fun validate(path: String): ValidationInfo? {
                val name = File(path).name

                try {
                    getInputStream(path)?.use { iStream ->
                        if (iStream.read() < 0) {
                            return ValidationInfo("The dictionary resource for $name is empty.")
                        }
                    } ?: return ValidationInfo("The dictionary resource for $name no longer exists.")
                } catch (e: IOException) {
                    return ValidationInfo("The dictionary resource for $name exists, but could not be read.")
                }

                return null
            }

            /**
             * Returns an [InputStream] to the given dictionary resource.
             *
             * @param dictionary the location of the dictionary resource
             * @return an [InputStream] to the given dictionary resource
             */
            private fun getInputStream(dictionary: String) =
                try {
                    Dictionary::class.java.classLoader.getResourceAsStream(dictionary)
                } catch (e: IOException) {
                    throw IllegalArgumentException("Failed to read dictionary into memory.", e)
                }

            /**
             * Reads the given dictionary resource into a list of lines.
             *
             * @param dictionary the location of the dictionary resource
             * @return the lines in the dictionary resource at [dictionary]
             */
            private fun fromInputStream(dictionary: String): List<String> {
                val inputStream = getInputStream(dictionary)
                    ?: throw IllegalArgumentException("Failed to read dictionary into memory.")

                val words = inputStream.bufferedReader().use { it.readText() }.lines().filter { it.isNotBlank() }
                if (words.isEmpty())
                    throw IllegalArgumentException("Dictionary must be non-empty.")

                return words
            }
        }
    }

    /**
     * A `Dictionary` added by the user.
     */
    // TODO Private constructor?
    class UserDictionary(path: String) : Dictionary(path, File(path).name, readLines(path).toMutableSet()) {
        override fun validate() = validate(uid)

        override fun toString() = "[custom] $name"


        companion object {
            /**
             * A cache of previously created `BundledDictionary(s)`.
             */
            private val CACHE = ConcurrentHashMap<String, UserDictionary>()

            /**
             * Constructs a new `UserDictionary` for the given dictionary path, or returns the previously created
             * instance for the file if there is one.
             *
             * @param path     the absolute path to the dictionary file
             * @param useCache `true` if a cached version of the dictionary should be returned if it exists,
             * `false` if the cache should always be updated
             * @return a new `UserDictionary` for the given dictionary path, or the previously created instance of
             * this dictionary if there is one
             */
            fun get(path: String, useCache: Boolean = true): UserDictionary =
                if (useCache) {
                    CACHE.getOrPut(path) { UserDictionary(path) }
                } else {
                    UserDictionary(path).also { CACHE[path] = it }
                }

            /**
             * Clears the cache of stored dictionaries.
             */
            fun clearCache() = CACHE.clear()

            /**
             * Detects whether the dictionary at the given path would be valid.
             *
             * @param path the absolute path to the dictionary file
             * @return `null` if the dictionary would be valid, or a `ValidationInfo` explaining why it would be
             * invalid
             */
            fun validate(path: String): ValidationInfo? {
                val file = File(path)
                val name = file.name

                if (!file.exists()) {
                    return ValidationInfo("The dictionary file for $name no longer exists.")
                }
                if (!file.canRead()) {
                    return ValidationInfo("The dictionary file for $name exists, but could not be read.")
                }
                try {
                    Files.newInputStream(file.toPath()).use { iStream ->
                        if (iStream.read() < 0) {
                            return ValidationInfo("The dictionary file for $name is empty.")
                        }
                    }
                } catch (e: IOException) {
                    return ValidationInfo("The dictionary file for $name exists, but could not be read.")
                }

                return null
            }


            private fun readLines(path: String) =
                try {
                    File(path).readLines().filter { it.isNotBlank() }
                        .also { if (it.isEmpty()) throw IllegalArgumentException("Dictionary must be non-empty.") }
                } catch (e: IOException) {
                    throw IllegalArgumentException("Failed to read dictionary into memory.", e)
                }
        }
    }

    /**
     * An (initially) empty `Dictionary` without a source file.
     */
    // TODO Use internal constructor?
    private class SimpleDictionary : Dictionary() {
        override fun validate(): ValidationInfo? = null
    }


    companion object {
        /**
         * The name of the default dictionary file.
         */
        const val DEFAULT_DICTIONARY_FILE = "words_alpha.dic"


        /**
         * Combines the given `Dictionary Dictionary(s)` into a single `Dictionary`.
         *
         * @param dictionaries the `Dictionary Dictionary(s)` to combine
         * @return a `Dictionary` containing all words in the given `Dictionary Dictionary(s)`
         */
        fun combine(dictionaries: Collection<Dictionary>): Dictionary =
            dictionaries.fold(SimpleDictionary()) { acc, dictionary -> acc.also { it.words.addAll(dictionary.words) } }
    }
}
