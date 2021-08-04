package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.UUIDClock
import com.fasterxml.uuid.UUIDTimer
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlin.random.Random
import kotlin.random.asJavaRandom


/**
 * Contains settings for generating random UUIDs.
 *
 * @property version The version of UUIDs to generate.
 * @property enclosure The string that encloses the generated UUID on both sides.
 * @property capitalization The capitalization mode of the generated UUID.
 * @property addDashes True if and only if the UUID should have dashes in it.
 */
data class UuidScheme(
    var version: Int = DEFAULT_VERSION,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var addDashes: Boolean = DEFAULT_ADD_DASHES
) : Scheme<UuidScheme> {
    private val random: Random = Random.Default


    /**
     * Returns random type 4 UUIDs.
     *
     * @param count the number of type 4 UUIDs to generate
     * @return random type 4 UUIDs
     */
    override fun generateStrings(count: Int): List<String> {
        @Suppress("MagicNumber") // UUID version is not magic
        val generator = when (version) {
            1 ->
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
            4 -> Generators.randomBasedGenerator(random.asJavaRandom())
            else -> throw DataGenerationException("Unknown UUID version `${version}`.")
        }

        return List(count) { generator.generate().toString() }
            .map { capitalization.transform(it) }
            .map {
                if (addDashes) it
                else it.replace("-", "")
            }
            .map { enclosure + it + enclosure }
    }


    override fun copyFrom(other: UuidScheme) = XmlSerializerUtil.copyBean(other, this)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [version][version] field.
         */
        const val DEFAULT_VERSION = 4

        /**
         * The default value of the [enclosure][enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""

        /**
         * The default value of the [capitalization][capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.LOWER

        /**
         * The default value of the [addDashes][addDashes] field.
         */
        const val DEFAULT_ADD_DASHES = true
    }
}
