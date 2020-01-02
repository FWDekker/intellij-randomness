package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Transient


/**
 * The user-configurable collection of schemes applicable to generating strings.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 */
@State(name = "StringSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class StringSettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<StringScheme> = DEFAULT_SCHEMES.toMutableList(),
    override var currentSchemeName: String = Scheme.DEFAULT_NAME
) : Settings<StringSettings, StringScheme> {
    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES
            get() = listOf(StringScheme())

        /**
         * The persistent `StringSettings` instance.
         */
        val default: StringSettings
            get() = ServiceManager.getService(StringSettings::class.java)
    }


    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: StringSettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * Contains settings for generating random strings.
 *
 * @property myName The name of the scheme.
 * @property minLength The minimum length of the generated string, inclusive.
 * @property maxLength The maximum length of the generated string, inclusive.
 * @property enclosure The string that encloses the generated string on both sides.
 * @property capitalization The capitalization mode of the generated string.
 * @property symbolSets The symbol sets that are available for generating strings.
 * @property activeSymbolSets The symbol sets that are actually used for generating strings; a subset of [symbolSets].
 *
 * @see StringInsertAction
 * @see StringSettingsAction
 * @see StringSettingsComponent
 */
data class StringScheme(
    override var myName: String = Scheme.DEFAULT_NAME,
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    @MapAnnotation(sortBeforeSave = false)
    var symbolSets: Map<String, String> = DEFAULT_SYMBOL_SETS.toMap(),
    @MapAnnotation(sortBeforeSave = false)
    var activeSymbolSets: Map<String, String> = DEFAULT_ACTIVE_SYMBOL_SETS.toMap()
) : Scheme<StringScheme> {
    companion object {
        /**
         * The default value of the [minLength][minLength] field.
         */
        const val DEFAULT_MIN_LENGTH = 3
        /**
         * The default value of the [maxLength][maxLength] field.
         */
        const val DEFAULT_MAX_LENGTH = 8
        /**
         * The default value of the [enclosure][enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""
        /**
         * The default value of the [capitalization][capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RANDOM
        /**
         * The default value of the [symbolSets][symbolSets] field.
         */
        val DEFAULT_SYMBOL_SETS = SymbolSet.defaultSymbolSets.toMap()
        /**
         * The default value of the [activeSymbolSets][activeSymbolSets] field.
         */
        val DEFAULT_ACTIVE_SYMBOL_SETS = listOf(SymbolSet.ALPHABET, SymbolSet.DIGITS).toMap()
    }


    /**
     * A list view of the `SymbolSet` objects described by [symbolSets].
     */
    var symbolSetList: Collection<SymbolSet>
        @Transient
        get() = symbolSets.toSymbolSets()
        set(value) {
            symbolSets = value.toMap()
        }
    /**
     * A list view of the `SymbolSet` objects described by [activeSymbolSets].
     */
    var activeSymbolSetList: Collection<SymbolSet>
        @Transient
        get() = activeSymbolSets.toSymbolSets()
        set(value) {
            activeSymbolSets = value.toMap()
        }


    override fun copyFrom(other: StringScheme) = XmlSerializerUtil.copyBean(other, this)

    override fun copyAs(name: String) = this.copy(myName = name)
}


/**
 * The configurable for string settings.
 *
 * @see StringSettingsAction
 */
class StringSettingsConfigurable(
    override val component: StringSettingsComponent = StringSettingsComponent()
) : SettingsConfigurable<StringSettings, StringScheme>() {
    override fun getDisplayName() = "Strings"
}
