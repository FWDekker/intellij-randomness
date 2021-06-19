package com.fwdekker.randomness.uds

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.decimal.DecimalInsertAction
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerInsertAction
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringInsertAction
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidInsertAction
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordInsertAction
import com.fwdekker.randomness.word.WordScheme
import java.util.function.Function
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
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
     */
    fun parse(descriptor: String): UDSGenerator {
        val generators = mutableListOf<Function<Int, List<String>>>()

        val iterator = StringPointer(descriptor)
        while ((+iterator).isNotEmpty()) {
            iterator.advanceWhile { it != '%' }.also { prefix -> generators.add { count -> List(count) { prefix } } }
            if ((+iterator).isEmpty())
                continue

            iterator += 1 // Drop '%'
            if ((+iterator).isEmpty())
                throw UDSParseException("Descriptor must not end in '%'.")

            if ((+iterator).first() == '%') {
                generators.add { count -> List(count) { "%" } }
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

        return argKey.trim() to argVal.trim()
    }

    /**
     * Creates a function that returns a given amount of string as specified by the type and arguments.
     *
     * @param type the data type to generate values with
     * @param args the arguments to give to the type's scheme
     * @return a function that returns a given amount of string as specified by the type and arguments
     */
    private fun createTypeGenerator(
        type: String,
        args: Map<String, String>
    ): Function<Int, List<String>> {
        val (typeConstructor, schemeConstructor) = when (type) {
            "Int" -> Pair(IntegerInsertAction::class, IntegerScheme::class.primaryConstructor!!)
            "Dec" -> Pair(DecimalInsertAction::class, DecimalScheme::class.primaryConstructor!!)
            "Str" -> Pair(StringInsertAction::class, StringScheme::class.primaryConstructor!!)
            "Word" -> Pair(WordInsertAction::class, WordScheme::class.primaryConstructor!!)
            "UUID" -> Pair(UuidInsertAction::class, UuidScheme::class.primaryConstructor!!)
            else -> throw UDSParseException("Unknown type '$type'.")
        }

        val typeScheme = schemeConstructor.callBy(convertSchemeArgs(args, schemeConstructor))

        return Function { typeConstructor.primaryConstructor!!.call(typeScheme).generateStrings(it) }
    }

    /**
     * Converts the arguments into the proper types for the given function.
     *
     * If an argument cannot be found in the given function, then it is truncated.
     *
     * @param args the named arguments to convert into the proper type
     * @param function the function to read the types from
     * @return the type-converted arguments
     */
    private fun convertSchemeArgs(args: Map<String, String>, function: KFunction<Scheme<*>>): Map<KParameter, *> {
        return args
            .mapNotNull { (k, v) ->
                function.findParameterByName(k)?.let {
                    it to when (it.type) {
                        Boolean::class.createType() -> v.toBoolean()
                        Double::class.createType() -> v.toDouble()
                        Int::class.createType() -> v.toInt()
                        Long::class.createType() -> v.toLong()
                        String::class.createType() -> v
                        else -> throw UDSParseException("Unknown type '${it.type}'.")
                    }
                }
            }
            .toMap()
    }
}


/**
 * Generates strings by concatenating the outputs of the generators.
 *
 * @property generators the generators that generate parts of the string
 */
class UDSGenerator(private val generators: List<Function<Int, List<String>>>) {
    /**
     * Generates the given number of strings.
     *
     * @param count the number of strings to generate
     * @return a list of strings
     */
    fun generateStrings(count: Int): List<String> {
        val generated = generators.map { it.apply(count) }

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
