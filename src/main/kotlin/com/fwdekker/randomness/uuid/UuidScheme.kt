package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.UUIDClock
import com.fasterxml.uuid.UUIDTimer
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.util.xmlb.annotations.Transient
import icons.RandomnessIcons
import kotlin.random.asJavaRandom


/**
 * Contains settings for generating random UUIDs.
 *
 * @property version The version of UUIDs to generate.
 * @property enclosure The string that encloses the generated UUID on both sides.
 * @property capitalization The capitalization mode of the generated UUID.
 * @property addDashes True if and only if the UUID should have dashes in it.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class UuidScheme(
    var version: Int = DEFAULT_VERSION,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var addDashes: Boolean = DEFAULT_ADD_DASHES,
    override var decorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme() {
    @Transient
    override val name = "UUID"
    override val icons = RandomnessIcons.Uuid


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
            else -> error("Unknown UUID version '$version'.")
        }

        return List(count) { generator.generate().toString() }
            .map { capitalization.transform(it, random) }
            .map {
                if (addDashes) it
                else it.replace("-", "")
            }
            .map { enclosure + it + enclosure }
    }


    override fun doValidate() =
        if (version !in listOf(TYPE_1, TYPE_4)) "Unknown UUID version '$version'."
        else decorator.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(decorator = decorator.deepCopy(retainUuid))
            .also { if (retainUuid) it.uuid = this.uuid }


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
