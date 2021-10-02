package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.UUIDClock
import com.fasterxml.uuid.UUIDTimer
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.util.xmlb.annotations.Transient
import java.awt.Color
import kotlin.random.asJavaRandom


/**
 * Contains settings for generating random UUIDs.
 *
 * @property version The version of UUIDs to generate.
 * @property quotation The string that encloses the generated UUID on both sides.
 * @property customQuotation The grouping separator defined in the custom option.
 * @property capitalization The capitalization mode of the generated UUID.
 * @property addDashes `true` if and only if the UUID should have dashes in it.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class UuidScheme(
    var version: Int = DEFAULT_VERSION,
    var quotation: String = DEFAULT_QUOTATION,
    var customQuotation: String = DEFAULT_CUSTOM_QUOTATION,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var addDashes: Boolean = DEFAULT_ADD_DASHES,
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    @get:Transient
    override val name = Bundle("uuid.title")
    override val typeIcon = BASE_ICON

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)


    /**
     * Returns random type 4 UUIDs.
     *
     * @param count the number of type 4 UUIDs to generate
     * @return random type 4 UUIDs
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val generator = when (version) {
            TYPE_1 ->
                Generators.timeBasedGenerator(
                    EthernetAddress(random.nextLong()),
                    UUIDTimer(
                        random.asJavaRandom(),
                        null,
                        object : UUIDClock() {
                            override fun currentTimeMillis() = random.nextLong()
                        }
                    )
                )
            TYPE_4 -> Generators.randomBasedGenerator(random.asJavaRandom())
            else -> error(Bundle("uuid.error.unknown_version", version))
        }

        return List(count) { generator.generate().toString() }
            .map { capitalization.transform(it, random) }
            .map {
                if (addDashes) it
                else it.replace("-", "")
            }
            .map { inQuotes(it) }
    }

    /**
     * Encapsulates [string] in the quotes defined by [quotation].
     *
     * @param string the string to encapsulate
     * @return [string] encapsulated in the quotes defined by [quotation]
     */
    private fun inQuotes(string: String): String {
        val startQuote = quotation.getOrNull(0) ?: ""
        val endQuote = quotation.getOrNull(1) ?: startQuote

        return "$startQuote$string$endQuote"
    }


    override fun doValidate() =
        when {
            version !in listOf(TYPE_1, TYPE_4) -> Bundle("uuid.error.unknown_version", version)
            quotation.length > 2 -> Bundle("uuid.error.quotation_length")
            else -> arrayDecorator.doValidate()
        }

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid))
            .also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for UUIDs.
         */
        val BASE_ICON = TypeIcon(RandomnessIcons.SCHEME, "id", listOf(Color(185, 155, 248, 154)))

        /**
         * The default value of the [version] field.
         */
        const val DEFAULT_VERSION = 4

        /**
         * The default value of the [quotation] field.
         */
        const val DEFAULT_QUOTATION = "\""

        /**
         * The default value of the [quotation] field.
         */
        const val DEFAULT_CUSTOM_QUOTATION = "<>"

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.LOWER

        /**
         * The default value of the [addDashes] field.
         */
        const val DEFAULT_ADD_DASHES = true

        /**
         * Integer representing a type-1 UUID.
         */
        const val TYPE_1 = 1

        /**
         * Integer representing a type-4 UUID.
         */
        const val TYPE_4 = 4
    }
}
