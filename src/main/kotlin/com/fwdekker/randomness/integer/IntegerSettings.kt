package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import org.ini4j.Registry.Key.DEFAULT_NAME


/**
 * The user-configurable collection of schemes applicable to generating integers.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 */
@State(name = "IntegerSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class IntegerSettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<IntegerScheme> = DEFAULT_SCHEMES,
    override var currentSchemeName: String = DEFAULT_CURRENT_SCHEME_NAME
) : Settings<IntegerSettings, IntegerScheme> {
    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES: MutableList<IntegerScheme>
            get() = mutableListOf(IntegerScheme())
        /**
         * The default value of the [currentSchemeName][currentSchemeName] field.
         */
        const val DEFAULT_CURRENT_SCHEME_NAME = DEFAULT_NAME

        /**
         * The persistent `IntegerSettings` instance.
         */
        val default: IntegerSettings
            get() = ServiceManager.getService(IntegerSettings::class.java)
    }


    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: IntegerSettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * Contains settings for generating random integers.
 *
 * @property myName The name of the scheme.
 * @property minValue The minimum value to be generated, inclusive.
 * @property maxValue The maximum value to be generated, inclusive.
 * @property base The base the generated value should be displayed in.
 * @property groupingSeparator The character that should separate groups.
 *
 * @see IntegerInsertAction
 * @see IntegerSettingsAction
 * @see IntegerSettingsComponent
 */
// TODO Turn the separator property into a char property once supported by the settings serializer
data class IntegerScheme(
    override var myName: String = DEFAULT_NAME,
    var minValue: Long = DEFAULT_MIN_VALUE,
    var maxValue: Long = DEFAULT_MAX_VALUE,
    var base: Int = DEFAULT_BASE,
    var groupingSeparator: String = DEFAULT_GROUPING_SEPARATOR
) : Scheme<IntegerScheme> {
    companion object {
        /**
         * The minimum value of the [base][base] field.
         */
        const val MIN_BASE = 2
        /**
         * The maximum value of the [base][base] field.
         */
        const val MAX_BASE = 36
        /**
         * The definition of decimal base.
         */
        const val DECIMAL_BASE = 10

        /**
         * The default value of the [minValue][minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0L
        /**
         * The default value of the [maxValue][maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1000L
        /**
         * The default value of the [base][base] field.
         */
        const val DEFAULT_BASE = DECIMAL_BASE
        /**
         * The default value of the [groupingSeparator][groupingSeparator] field.
         */
        const val DEFAULT_GROUPING_SEPARATOR = ""
    }


    override fun copyFrom(other: IntegerScheme) = XmlSerializerUtil.copyBean(other, this)

    override fun copyAs(name: String) = this.copy(myName = name)


    /**
     * Sets the grouping separator safely to ensure that exactly one character is set.
     *
     * @param groupingSeparator the possibly-unsafe grouping separator string
     */
    fun safeSetGroupingSeparator(groupingSeparator: String?) {
        if (groupingSeparator == null || groupingSeparator.isEmpty())
            this.groupingSeparator = DEFAULT_GROUPING_SEPARATOR
        else
            this.groupingSeparator = groupingSeparator.substring(0, 1)
    }
}


/**
 * The configurable for integer settings.
 *
 * @see IntegerSettingsAction
 */
class IntegerSettingsConfigurable(
    override val component: IntegerSettingsComponent = IntegerSettingsComponent()
) : SettingsConfigurable<IntegerSettings, IntegerScheme>() {
    override fun getDisplayName() = "Integers"
}
