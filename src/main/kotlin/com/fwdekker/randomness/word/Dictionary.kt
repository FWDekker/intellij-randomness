package com.fwdekker.randomness.word

import com.fwdekker.randomness.Cache
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


/**
 * Thrown when a [Dictionary] is found to be invalid and cannot be used in the intended way.
 *
 * @param message the detail message
 * @param cause the cause
 */
class InvalidDictionaryException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)


/**
 * A collection of words that may become inaccessible at any moment in time.
 */
interface Dictionary {
    /**
     * The words in the dictionary.
     */
    @get:Throws(InvalidDictionaryException::class)
    val words: Set<String>


    /**
     * Throws an [InvalidDictionaryException] iff this dictionary is currently invalid.
     *
     * @throws InvalidDictionaryException if this dictionary is currently invalid
     */
    @Throws(InvalidDictionaryException::class)
    fun validate()

    /**
     * Returns `true` iff [validate] does not throw an exception.
     *
     * @return `true` iff [validate] does not throw an exception
     */
    @Suppress("SwallowedException") // That's exactly how this function should work
    fun isValid(): Boolean =
        try {
            validate()
            true
        } catch (e: InvalidDictionaryException) {
            false
        }
}


/**
 * A `Dictionary` of which the underlying file is a resource in the JAR.
 *
 * @property filename the path to the resource file
 */
class BundledDictionary private constructor(val filename: String) : Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String> by lazy {
        validate()

        try {
            getStream().bufferedReader()
                .readLines()
                .filterNot { it.isBlank() }
                .filterNot { it.startsWith('#') }
                .toSet()
        } catch (e: IOException) {
            throw InvalidDictionaryException(e.message, e)
        }
    }


    @Throws(InvalidDictionaryException::class)
    override fun validate() {
        try {
            getStream()
        } catch (e: IOException) {
            throw InvalidDictionaryException(e.message, e)
        }
    }

    /**
     * Returns `true` iff this dictionary's filename equals [other]'s filename.
     *
     * @param other an object
     * @return `true` iff this dictionary's filename equals [other]'s filename
     */
    override fun equals(other: Any?) = other is BundledDictionary && this.filename == other.filename

    /**
     * Returns the hash code of the filename.
     *
     * @return the hash code of the filename
     */
    override fun hashCode() = filename.hashCode()

    /**
     * Returns a human-readable string of the dictionary's filename.
     *
     * @return a human-readable string of the dictionary's filename
     */
    override fun toString() = filename


    /**
     * Returns a stream to the resource file.
     *
     * @return a stream to the resource file
     * @throws IOException if the resource file could not be opened
     */
    @Throws(IOException::class)
    private fun getStream() =
        BundledDictionary::class.java.classLoader.getResource(filename)?.openStream()
            ?: throw FileNotFoundException("File not found.")


    companion object {
        /**
         * The location of a simple English dictionary.
         */
        const val SIMPLE_DICTIONARY = "english.dic"

        /**
         * The cache of bundled dictionaries, used to improve word generation times.
         */
        val cache = Cache<String, BundledDictionary> { BundledDictionary(it) }
    }
}


/**
 * A `Dictionary` of which the underlying file is a regular file.
 *
 * @property filename the path to the file
 */
class UserDictionary private constructor(val filename: String) : Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String> by lazy {
        validate()
        File(filename).readLines()
            .filterNot { it.isBlank() }
            .filterNot { it.startsWith('#') }
            .toSet()
    }


    @Throws(InvalidDictionaryException::class)
    override fun validate() {
        val file = File(filename)
        if (!file.exists()) throw InvalidDictionaryException("File not found.")
        if (!file.canRead()) throw InvalidDictionaryException("File unreadable.")

        try {
            file.inputStream()
        } catch (e: IOException) {
            throw InvalidDictionaryException(e.message, e)
        }
    }

    /**
     * Returns a human-readable string of the dictionary's filename.
     *
     * @return a human-readable string of the dictionary's filename
     */
    override fun toString() = filename

    /**
     * Returns `true` iff this dictionary's filename equals [other]'s filename.
     *
     * @param other an object
     * @return `true` iff this dictionary's filename equals [other]'s filename
     */
    override fun equals(other: Any?) = other is UserDictionary && this.filename == other.filename

    /**
     * Returns the hash code of the filename.
     *
     * @return the hash code of the filename
     */
    override fun hashCode() = filename.hashCode()


    companion object {
        /**
         * The cache of bundled dictionaries, used to improve word generation times.
         */
        val cache = Cache<String, UserDictionary> { UserDictionary(it) }
    }
}


/**
 * References a dictionary by its properties.
 *
 * Using a reference, each access goes through the cache first, so that outdated instances of a dictionary (i.e. those
 * flushed when clearing the cache) are not used anymore. This ensures that only the latest instance of that dictionary
 * is used, which is important when a dictionary has to be used both before and after clearing a cache.
 *
 * @property isBundled True if this dictionary refers to a [BundledDictionary].
 * @property filename The filename of the referred-to dictionary.
 */
data class DictionaryReference(val isBundled: Boolean, var filename: String) : Dictionary {
    /**
     * The dictionary that is referred to by this reference, as fetched from the cache.
     */
    val referent: Dictionary
        get() =
            if (isBundled) BundledDictionary.cache.get(filename)
            else UserDictionary.cache.get(filename)


    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String>
        get() = referent.words

    @Throws(InvalidDictionaryException::class)
    override fun validate() = referent.validate()


    /**
     * Returns the string representation of the [referent].
     *
     * @return the string representation of the [referent]
     */
    override fun toString() = referent.toString()


    companion object {
        /**
         * The error message that is displayed if an unknown dictionary implementation is used.
         */
        const val DICTIONARY_CAST_EXCEPTION = "Unexpected dictionary implementation."


        /**
         * Returns a reference to the given dictionary.
         *
         * @param dictionary the dictionary to return a reference to
         */
        @Suppress("FunctionMinLength") // Function name is clear enough because of class name
        fun to(dictionary: Dictionary) =
            when (dictionary) {
                is BundledDictionary -> DictionaryReference(true, dictionary.filename)
                is UserDictionary -> DictionaryReference(false, dictionary.filename)
                else -> error(DICTIONARY_CAST_EXCEPTION)
            }
    }
}
