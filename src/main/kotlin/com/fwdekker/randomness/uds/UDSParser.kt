package com.fwdekker.randomness.uds

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor


/**
 * Parses a UDS descriptor into a UDS generator.
 */
object UDSParser {
    /**
     * Parses the given UDS descriptor into a UDS generator.
     *
     * @param descriptor a UDS descriptor
     * @return a UDS generator
     * @throws UDSParseException if the given string is not a valid UDS descriptor
     */
    @Throws(UDSParseException::class)
    fun parse(descriptor: String): UDSGenerator {
        val generators = mutableListOf<(Int, Random) -> List<String>>()

        val iterator = StringPointer(descriptor)
        while ((+iterator).isNotEmpty()) {
            iterator.advanceWhile { it != '%' }.also { prefix -> generators.add { count, _ -> List(count) { prefix } } }
            if ((+iterator).isEmpty())
                continue

            iterator += 1 // Drop '%'
            if ((+iterator).isEmpty())
                throw UDSParseException("Descriptor must not end in '%'.")

            if ((+iterator).first() == '%') {
                generators.add { count, _ -> List(count) { "%" } }
                iterator += 1 // Drop the second '%'
                continue
            }

            generators += createTypeGenerator(iterator.advanceWhile { it != '[' }.trim(), parseArgs(iterator))
        }

        return UDSGenerator(generators)
    }


    /**
     * Parses a list of key-value pairs at the start of a string.
     *
     * A set of key-value pairs is written as `[pair*]`, where `pair` is defined as in [parseKeyValue].
     *
     * @param iterator the string that starts with the list of key-value pairs; after this method, the string points to
     * the first character after the `]`
     * @return the list of key-value pairs, converted to a map
     * @throws UDSParseException if the string could not be parsed into a list of key-value pairs
     */
    @Throws(UDSParseException::class)
    private fun parseArgs(iterator: StringPointer): Map<String, String> {
        val typeArgs = mutableMapOf<String, String>()

        if ((+iterator).firstOrNull() != '[')
            throw UDSParseException(
                "Type argument list must begin with '[', but begins with '${(+iterator).firstOrNull()}'."
            )
        iterator += 1 // Drop '['

        while ((+iterator).firstOrNull() != ']') {
            parseKeyValue(iterator)?.let { (k, v) -> typeArgs[k] = v }
        }

        if ((+iterator).firstOrNull() != ']')
            throw UDSParseException(
                "Type argument list must end with ']', but ends with '${(+iterator).firstOrNull()}'."
            )
        iterator += 1 // Drop ']'

        return typeArgs
    }

    /**
     * Parses a key-value pair found at the start of a string.
     *
     * The given string should adhere to the following regex: `[^=]*=[^,\]]*[,\]?.*]`. That is, the key and value are
     * separated by an `=`, and the value is finalised by a `,`, a `]`, or by ending the string. The key and the value
     * may be empty.
     *
     * @param iterator the string that starts with a key-value pair; after this method, the iterator points to the first
     * character after the value
     * @return the key-value pair, with both the key and value trimmed for whitespace
     * @throws UDSParseException if the string could not be parsed into a key-value pair
     */
    @Throws(UDSParseException::class)
    private fun parseKeyValue(iterator: StringPointer): Pair<String, String>? {
        iterator.advanceWhile { it == ',' || it == ' ' }
        if ((+iterator).firstOrNull() == ']')
            return null

        val argKey = (+iterator).takeWhile { it != '=' }
        iterator += argKey.length + 1 // Also drop '='
        if ((+iterator).isEmpty())
            throw UDSParseException("Type argument '$argKey' must also have a value.")

        val argVal = (+iterator).takeWhile { it != ',' && it != ']' }
        iterator += argVal.length

        return argKey.trim() to argVal.trim().removeSurrounding("\"")
    }

    /**
     * Creates a function that returns a given amount of string as specified by the type and arguments.
     *
     * @param type the data type to generate values with
     * @param args the arguments to give to the type's scheme
     * @return a function that returns a given amount of string as specified by the type and arguments
     * @throws UDSParseException if an unknown type is given
     */
    @Throws(UDSParseException::class)
    private fun createTypeGenerator(
        type: String,
        args: Map<String, String>
    ): (Int, Random) -> List<String> {
        val schemeConstructor = when (type) {
            "Int" -> IntegerScheme::class.primaryConstructor!!
            "Dec" -> DecimalScheme::class.primaryConstructor!!
            "Str" -> StringScheme::class.primaryConstructor!!
            "Word" -> WordScheme::class.primaryConstructor!!
            "UUID" -> UuidScheme::class.primaryConstructor!!
            else -> throw UDSParseException("Unknown type '$type'.")
        }

        val typeScheme = schemeConstructor.callBy(convertSchemeArgs(args, schemeConstructor))
        return { count, _ -> typeScheme.generateStrings(count) }
    }

    /**
     * Converts the arguments into the proper types for the given function.
     *
     * If an argument cannot be found in the given function, then it is truncated.
     *
     * @param args the named arguments to convert into the proper type
     * @param function the function to read the types from
     * @return the type-converted arguments
     * @throws UDSParseException if an unknown type is given
     */
    @Throws(UDSParseException::class)
    @Suppress("ComplexMethod") // Method cannot be separated or simplified
    private fun convertSchemeArgs(args: Map<String, String>, function: KFunction<Scheme<*>>): Map<KParameter, *> {
        return args
            .mapNotNull { (k, v) ->
                function.findParameterByName(k)?.let {
                    val stringProjection = KTypeProjection(KVariance.INVARIANT, String::class.createType())

                    it to when {
                        it.type.classifier == Boolean::class -> v.toBoolean()
                        it.type.classifier == Double::class -> v.toDoubleOrNull() ?: 0.0
                        it.type.classifier == Int::class -> v.toIntOrNull() ?: 0
                        it.type.classifier == Long::class -> v.toLongOrNull() ?: 0L
                        it.type.classifier == String::class -> v
                        it.type.classifier == CapitalizationMode::class -> CapitalizationMode.getMode(v)
                        it.type.classifier == List::class
                            && it.type.arguments == listOf(stringProjection) -> parseListDescriptor(v)
                        it.type.classifier == Set::class
                            && it.type.arguments == listOf(stringProjection) -> parseSetDescriptor(v)
                        it.type.classifier == Map::class
                            && it.type.arguments == listOf(stringProjection, stringProjection) -> parseMapDescriptor(v)
                        else -> throw UDSParseException("Unknown type '${it.type}'.")
                    }
                } ?: throw UDSParseException("Unknown parameter '$k'.")
            }
            .toMap()
    }

    /**
     * Parses a list of strings from the given descriptor.
     *
     * List descriptors should adhere to the following rules:
     * * A list consists of entries.
     * * Entries are separated by at least one whitespace.
     * * A whitespace can be added to an entry by escaping it with a backslash.
     * * A backslash can be added to an entry by escaping it with a backslash.
     * * No other characters may be escaped by a backspace.
     * * The descriptor may not end with an escaping backslash.
     * * Empty entries are ignored.
     * * Lists may have zero entries.
     *
     * @param listDescriptor a string describing a list of strings
     * @param evaluateEscapeCharacters true if escape characters should be validated and evaluated. For example, if set
     * to true, the string `\\\\` will be replaced with `\\`. Set this to false if the returned list will be used by
     * another parser which may allow for more escape characters.
     * @return a list of strings parsed from the given descriptor
     * @throws UDSParseException if the given string is not a valid string list descriptor
     */
    @Throws(UDSParseException::class)
    @Suppress(
        "NestedBlockDepth",
        "StringLiteralDuplication"
    ) // TODO: Create a generic parser that works with escape symbols
    private fun parseListDescriptor(listDescriptor: String, evaluateEscapeCharacters: Boolean = true): List<String> {
        val elements = mutableListOf<String>()

        val iterator = StringPointer(listDescriptor)
        while ((+iterator).isNotEmpty()) {
            var element = ""

            while (true) {
                element += iterator.advanceWhile { it !in " \\" }

                when ((+iterator).firstOrNull()) {
                    null -> break
                    ' ' -> {
                        iterator += 1
                        break
                    }
                    '\\' -> {
                        iterator += 1
                        if ((+iterator).isEmpty())
                            throw UDSParseException("Trailing backslash does not escape anything.")

                        if (!evaluateEscapeCharacters) {
                            element += "\\" + (+iterator).first()
                        } else {
                            if ((+iterator).first() in "\\ ")
                                element += (+iterator).first()
                            else
                                throw UDSParseException("Backslash must escape either whitespace or another backslash.")
                        }

                        iterator += 1
                    }
                }
            }

            if (element.isNotEmpty())
                elements += element
        }

        return elements
    }

    /**
     * Parses a set of strings from the given descriptor.
     *
     * Uses the same format as [parseListDescriptor], but duplicate entries are removed.
     *
     * @param setDescriptor a string describing a set of strings
     * @return a set of strings parsed from the given descriptor
     * @throws UDSParseException if the given string is not a valid string set descriptor
     */
    @Throws(UDSParseException::class)
    private fun parseSetDescriptor(setDescriptor: String) =
        parseListDescriptor(setDescriptor).toSet()

    /**
     * Parses a string-to-string map from the given descriptor.
     *
     * Map descriptors should adhere to the following rules:
     * * A map consists of key-value pairs.
     * * Key-value pairs are separated by at least one whitespace.
     * * A key-value pair consists of a key and a value, separated by a colon.
     * * A whitespace can be added to a key or a value by escaping it with a backslash.
     * * A colon can be added to a key or a value by escaping it with a backslash.
     * * A backslash can be added to a key or a value by escaping it with a backslash.
     * * No other characters may be escaped by a backslash.
     * * The descriptor may not end with an escaping backslash.
     * * A key-value pair descriptor must contain exactly one unescaped colon.
     * * Keys may not be empty.
     * * Values may be empty.
     * * If multiple key-value pairs have the same key, only the last key-value pair is returned.
     * * Maps may have zero key-value pairs.
     *
     * @param mapDescriptor a string describing a string-to-string map
     * @return a string-to-string map parsed from the given descriptor
     * @throws UDSParseException if the given string is not a valid string-to-string map descriptor
     */
    @Throws(UDSParseException::class)
    private fun parseMapDescriptor(mapDescriptor: String) =
        parseListDescriptor(mapDescriptor, evaluateEscapeCharacters = false)
            .associate { pairDescriptor ->
                StringPointer(pairDescriptor).let { parseMapKey(it) to parseMapValue(it) }
            }

    /**
     * Parses a key for a string-to-string map at the start of the string.
     *
     * @param iterator the string that starts with the map's key; after this method, the string points to the first
     * character after the `:`
     * @return a key for a string-to-string map
     * @throws UDSParseException if the iterator does not point to a key for a string-to-string map
     * @see UDSParser.parseMapDescriptor
     * @see UDSParser.parseMapValue
     */
    @Throws(UDSParseException::class)
    @Suppress("ThrowsCount") // TODO: Create a generic parser that works with escape symbols
    private fun parseMapKey(iterator: StringPointer): String {
        var key = ""

        while ((+iterator).isNotEmpty()) {
            key += iterator.advanceWhile { it !in ":\\" }

            when ((+iterator).firstOrNull()) {
                null -> throw UDSParseException("Key '$key' does not have a value.")
                ':' -> {
                    if (key.isEmpty())
                        throw UDSParseException("Key must not be empty.")

                    iterator += 1
                    break
                }
                '\\' -> {
                    iterator += 1
                    when ((+iterator).firstOrNull()) {
                        '\\', ' ', ':' ->
                            key += (+iterator).first()
                        null ->
                            throw UDSParseException("Trailing backslash does not escape anything.")
                        else ->
                            throw UDSParseException(
                                "Backslash must escape either whitespace, colon, or another backslash."
                            )
                    }

                    iterator += 1
                }
            }
        }

        return key
    }

    /**
     * Parses a value for a string-to-string map at the start of the string.
     *
     * @param iterator the string that starts with the map's value; after this method, the string points to the first
     * character after the `:`
     * @return a key for a string-to-string map
     * @throws UDSParseException if the iterator does not point to a value for a string-to-string map
     * @see UDSParser.parseMapDescriptor
     * @see UDSParser.parseMapKey
     */
    @Throws(UDSParseException::class)
    @Suppress("ThrowsCount") // TODO: Create a generic parser that works with escape symbols
    private fun parseMapValue(iterator: StringPointer): String {
        var value = ""

        while ((+iterator).isNotEmpty()) {
            value += iterator.advanceWhile { it !in ":\\" }

            when ((+iterator).firstOrNull()) {
                null -> break
                ':' -> throw UDSParseException("Value must not contain ':'.")
                '\\' -> {
                    iterator += 1
                    when ((+iterator).firstOrNull()) {
                        '\\', ' ', ':' ->
                            value += (+iterator).first()
                        null ->
                            throw UDSParseException("Trailing backslash does not escape anything.")
                        else ->
                            throw UDSParseException(
                                "Backslash must escape either whitespace, colon, or another backslash."
                            )
                    }

                    iterator += 1
                }
            }
        }

        return value
    }
}


/**
 * Generates strings by concatenating the outputs of the generators.
 *
 * @property generators the generators that generate parts of the string
 */
class UDSGenerator(private val generators: List<(Int, Random) -> List<String>>) {
    /**
     * The random number generator used for all the generators.
     */
    var random: Random = Random.Default


    /**
     * Generates the given number of strings.
     *
     * @param count the number of strings to generate
     * @return a list of strings
     */
    fun generateStrings(count: Int): List<String> {
        val generated = generators.map { it(count, random) }

        return List(count) { i -> generated.joinToString(separator = "") { it[i] } }
    }
}


/**
 * Behaves a bit like a pointer, containing a reference to a "pointee".
 *
 * @property pointee the element being pointed to
 */
class StringPointer(private var pointee: String) {
    /**
     * "Dereferences" the pointee.
     *
     * @return the pointee
     */
    operator fun unaryPlus() = pointee

    /**
     * Advances the string pointer by the given number of chars.
     *
     * @param advanceBy the number of chars to advance the pointer by
     */
    operator fun plusAssign(advanceBy: Int) {
        this.pointee = pointee.drop(advanceBy)
    }

    /**
     * Reads chars from the start of the pointee as long as the predicate matches the chars being read, advancing the
     * pointer while doing so, and returning the read characters as a string.
     *
     * @param predicate the predicate to check read characters against
     * @return the chars that matched the predicate
     */
    fun advanceWhile(predicate: (Char) -> Boolean) = pointee.takeWhile(predicate).also { this += it.length }
}


/**
 * Thrown if a UDS string could not be parsed.
 *
 * @param message the detail message
 * @param cause the cause
 */
class UDSParseException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
