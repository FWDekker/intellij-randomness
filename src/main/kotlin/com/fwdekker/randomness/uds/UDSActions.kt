package com.fwdekker.randomness.uds

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataInsertRepeatArrayAction
import com.fwdekker.randomness.DataQuickSwitchSchemeAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettings
import com.fwdekker.randomness.array.ArraySettingsAction
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
import icons.RandomnessIcons
import kotlin.reflect.full.createType
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor


/**
 * All actions related to inserting UDS-based strings.
 */
class UDSGroupAction : DataGroupAction(RandomnessIcons.Data.Base) {
    override val insertAction = UDSInsertAction()
    override val insertArrayAction = UDSInsertAction.ArrayAction()
    override val insertRepeatAction = UDSInsertAction.RepeatAction()
    override val insertRepeatArrayAction = UDSInsertAction.RepeatArrayAction()
    override val settingsAction = UDSSettingsAction()
    override val quickSwitchSchemeAction = UDSSettingsAction.UDSQuickSwitchSchemeAction()
    override val quickSwitchArraySchemeAction = ArraySettingsAction.ArrayQuickSwitchSchemeAction()
}


/**
 * Inserts random arbitrary strings based on the UDS descriptor.
 *
 * @param scheme the scheme to use for generating UDS-based strings
 */
class UDSInsertAction(private val scheme: UDSScheme = UDSSettings.default.currentScheme) :
    DataInsertAction(RandomnessIcons.Data.Base) {
    override val name = "Random Decimal"


    /**
     * Returns random UDS-based strings based on the descriptor.
     *
     * @param count the number of strings to generate
     * @return random UDS-based strings based on the descriptor
     */
    @Suppress("LongMethod", "ComplexMethod", "LoopWithTooManyJumpStatements")
    override fun generateStrings(count: Int) = List(count) {
        var out = ""

        var iterator = scheme.descriptor
        while (iterator.isNotEmpty()) {
            val prefix = iterator.takeWhile { it != '%' }
            out += prefix
            iterator = iterator.drop(prefix.length + 1) // Also drop '%'
            if (iterator.isEmpty()) break

            if (iterator.first() == '%') {
                // Add escaped '%'
                out += '%'
                iterator = iterator.drop(1)
                continue
            }

            val type = iterator.takeWhile { it != '[' }
            iterator = iterator.drop(type.length + 1) // Also drop '['
            if (iterator.isEmpty()) throw DataGenerationException("UDS type '$type' cannot end with '['.")

            val typeArgs = mutableMapOf<String, String>()
            while (iterator.first() != ']') {
                if (iterator.first() == ',')
                    iterator = iterator.drop(1)

                val argKey = iterator.takeWhile { it != '=' }
                iterator = iterator.drop(argKey.length + 1) // Also drop '='
                if (iterator.isEmpty())
                    throw DataGenerationException("UDS type argument '$argKey' must also have a value.")

                val argVal = iterator.takeWhile { it != ',' && it != ']' }
                iterator = iterator.drop(argVal.length)

                typeArgs[argKey.trim()] = argVal.trim()
            }
            iterator = iterator.drop(1) // Drop ']'

            val (typeConstructor, schemeConstructor) = when (type) {
                "Int" -> Pair(IntegerInsertAction::class, IntegerScheme::class.primaryConstructor!!)
                "Dec" -> Pair(DecimalInsertAction::class, DecimalScheme::class.primaryConstructor!!)
                "Str" -> Pair(StringInsertAction::class, StringScheme::class.primaryConstructor!!)
                "Word" -> Pair(WordInsertAction::class, WordScheme::class.primaryConstructor!!)
                "UUID" -> Pair(UuidInsertAction::class, UuidScheme::class.primaryConstructor!!)
                else -> throw DataGenerationException("Unknown UDS type '$type'.")
            }

            val typeScheme = schemeConstructor.callBy(
                typeArgs
                    .mapNotNull { (k, v) ->
                        schemeConstructor.findParameterByName(k)?.let {
                            Pair(
                                it,
                                when (it.type) {
                                    Boolean::class.createType() -> v.toBoolean()
                                    Double::class.createType() -> v.toDouble()
                                    Int::class.createType() -> v.toInt()
                                    Long::class.createType() -> v.toLong()
                                    String::class.createType() -> v
                                    else -> throw DataGenerationException("Unknown type '${it.type}'.")
                                }
                            )
                        }
                    }
                    .toMap()
            )
            out += typeConstructor.primaryConstructor!!.call(typeScheme).generateString()
        }

        out
    }

    /**
     * Inserts an array-like string of UDS-based strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class ArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: UDSScheme = UDSSettings.default.currentScheme
    ) : DataInsertArrayAction(arrayScheme, UDSInsertAction(scheme), RandomnessIcons.Data.Array) {
        override val name = "Random UDS Array"
    }

    /**
     * Inserts repeated random UDS-based strings.
     *
     * @param scheme the settings to use for generating strings
     */
    class RepeatAction(scheme: UDSScheme = UDSSettings.default.currentScheme) :
        DataInsertRepeatAction(UDSInsertAction(scheme), RandomnessIcons.Data.Repeat) {
        override val name = "Random Repeated UDS"
    }

    /**
     * Inserts repeated array-like strings of UDS-based strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class RepeatArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: UDSScheme = UDSSettings.default.currentScheme
    ) : DataInsertRepeatArrayAction(ArrayAction(arrayScheme, scheme), RandomnessIcons.Data.RepeatArray) {
        override val name = "Random Repeated UDS Array"
    }
}


/**
 * Controller for random string generation settings.
 *
 * @see UDSSettings
 * @see UDSSettingsComponent
 */
class UDSSettingsAction : DataSettingsAction(RandomnessIcons.Data.Settings) {
    override val name = "UDS Settings"

    override val configurableClass = UDSSettingsConfigurable::class.java


    /**
     * Opens a popup to allow the user to quickly switch to the selected scheme.
     *
     * @param settings the settings containing the schemes that can be switched between
     */
    class UDSQuickSwitchSchemeAction(settings: UDSSettings = UDSSettings.default) :
        DataQuickSwitchSchemeAction<UDSScheme>(settings, RandomnessIcons.Data.QuickSwitchScheme) {
        override val name = "Quick Switch UDS Scheme"
    }
}
