package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Scheme.Companion.DEFAULT_NAME
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Transient
import com.vdurmont.emoji.EmojiParser


/**
 * The user-configurable collection of schemes applicable to generating strings.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 *
 * @see StringSettingsAction
 * @see StringSettingsConfigurable
 */
@State(name = "StringSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class StringSettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<StringScheme> = DEFAULT_SCHEMES,
    override var currentSchemeName: String = DEFAULT_CURRENT_SCHEME_NAME
) : Settings<StringSettings, StringScheme> {
    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: StringSettings) = XmlSerializerUtil.copyBean(state, this)


    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES: MutableList<StringScheme>
            get() = mutableListOf(StringScheme())

        /**
         * The default value of the [currentSchemeName][currentSchemeName] field.
         */
        const val DEFAULT_CURRENT_SCHEME_NAME = DEFAULT_NAME

        /**
         * The persistent `StringSettings` instance.
         */
        val default: StringSettings
            get() = service()
    }
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
 * @property excludeLookAlikeSymbols Whether the symbols in [SymbolSet.lookAlikeCharacters] should be excluded.
 *
 * @see StringInsertAction
 * @see StringSettings
 */
data class StringScheme(
    override var myName: String = DEFAULT_NAME,
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    @get:Transient
    var symbolSets: Map<String, String> = DEFAULT_SYMBOL_SETS.toMap(),
    @get:Transient
    var activeSymbolSets: Map<String, String> = DEFAULT_ACTIVE_SYMBOL_SETS.toMap(),
    var excludeLookAlikeSymbols: Boolean = DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS
) : Scheme<StringScheme> {
    /**
     * Same as [symbolSets], except that all emoji are serialized.
     */
    @get:MapAnnotation(sortBeforeSave = false)
    @Suppress("unused") // Used by serializer
    var serializedSymbolSets: Map<String, String>
        get() = symbolSets.map { SymbolSet(it.key, EmojiParser.parseToAliases(it.value)) }.toMap()
        set(value) {
            symbolSets = value.map { SymbolSet(it.key, EmojiParser.parseToUnicode(it.value)) }.toMap()
        }

    /**
     * Same as [activeSymbolSets], except that all emoji are serialized.
     */
    @get:MapAnnotation(sortBeforeSave = false)
    @Suppress("unused") // Used by serializer
    var serializedActiveSymbolSets: Map<String, String>
        get() = activeSymbolSets.map { SymbolSet(it.key, EmojiParser.parseToAliases(it.value)) }.toMap()
        set(value) {
            activeSymbolSets = value.map { SymbolSet(it.key, EmojiParser.parseToUnicode(it.value)) }.toMap()
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

        /**
         * The default value of the [excludeLookAlikeSymbols][excludeLookAlikeSymbols] field.
         */
        const val DEFAULT_EXCLUDE_LOOK_ALIKE_SYMBOLS = false
    }
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
