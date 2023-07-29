package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.UUIDClock
import com.fasterxml.uuid.UUIDTimer
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.ui.JBColor
import java.awt.Color
import kotlin.random.asJavaRandom


/**
 * Contains settings for generating random UUIDs.
 *
 * @property type The type (or version) of UUIDs to generate.
 * @property isUppercase `true` if and only if all letters are uppercase.
 * @property addDashes `true` if and only if the UUID should have dashes in it.
 * @property affixDecorator The affixation to apply to the generated values.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class UuidScheme(
    var type: Int = DEFAULT_TYPE,
    var isUppercase: Boolean = DEFAULT_IS_UPPERCASE,
    var addDashes: Boolean = DEFAULT_ADD_DASHES,
    var affixDecorator: AffixDecorator = DEFAULT_AFFIX_DECORATOR,
    var arrayDecorator: ArrayDecorator = DEFAULT_ARRAY_DECORATOR,
) : Scheme() {
    override val name = Bundle("uuid.title")
    override val typeIcon = BASE_ICON
    override val decorators get() = listOf(affixDecorator, arrayDecorator)


    /**
     * Returns random type 4 UUIDs.
     *
     * @param count the number of type 4 UUIDs to generate
     * @return random type 4 UUIDs
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val generator = when (type) {
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
            else -> error(Bundle("uuid.error.unknown_type", type))
        }

        return List(count) { generator.generate().toString() }
            .map {
                val capitalization = if (isUppercase) CapitalizationMode.UPPER else CapitalizationMode.LOWER
                capitalization.transform(it, random)
            }
            .map {
                if (addDashes) it
                else it.replace("-", "")
            }
    }


    override fun doValidate() =
        if (type !in listOf(TYPE_1, TYPE_4)) Bundle("uuid.error.unknown_type", type)
        else arrayDecorator.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(
            affixDecorator = affixDecorator.deepCopy(retainUuid),
            arrayDecorator = arrayDecorator.deepCopy(retainUuid),
        ).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for UUIDs.
         */
        val BASE_ICON = TypeIcon(
            Icons.SCHEME,
            "id",
            listOf(JBColor(Color(185, 155, 248, 154), Color(185, 155, 248, 154)))
        )

        /**
         * The default value of the [type] field.
         */
        const val DEFAULT_TYPE = 4

        /**
         * The default value of the [isUppercase] field.
         */
        const val DEFAULT_IS_UPPERCASE = false

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

        /**
         * The preset values for the [affixDecorator] field.
         */
        val PRESET_AFFIX_DECORATOR_DESCRIPTORS = listOf("'", "\"", "`")

        /**
         * The default value of the [affixDecorator] field.
         */
        val DEFAULT_AFFIX_DECORATOR = AffixDecorator(enabled = false, descriptor = "\"")

        /**
         * The default value of the [arrayDecorator] field.
         */
        val DEFAULT_ARRAY_DECORATOR = ArrayDecorator()
    }
}
