package com.fwdekker.randomness.string

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Transient
import com.vdurmont.emoji.EmojiParser


/**
 * The user-configurable persistent collection of all [SymbolSet]s available to the user.
 *
 * @property serializedSymbolSets The symbol sets that are available for generating strings. Emoji have been serialized
 * for compatibility with JetBrains' serializer.
 * @property placeholder A placeholder value used to trick the serializer into working.
 * @see StringScheme
 */
@State(
    name = "com.fwdekker.randomness.string.SymbolSetSettings",
    storages = [Storage("\$APP_CONFIG\$/randomness.xml")]
)
data class SymbolSetSettings(
    @MapAnnotation(sortBeforeSave = false)
    var serializedSymbolSets: Map<String, String> = DEFAULT_SYMBOL_SETS,
    @Suppress("unused") // At least two fields are required for serialization to work
    private val placeholder: String = ""
) : PersistentStateComponent<SymbolSetSettings> {
    /**
     * Same as [symbolSets], except that serialized emoji have been deserialized.
     */
    var symbolSets: Map<String, String>
        @Transient
        get() = serializedSymbolSets.map { SymbolSet(it.key, EmojiParser.parseToUnicode(it.value)) }.toMap()
        set(value) {
            serializedSymbolSets = value.map { SymbolSet(it.key, EmojiParser.parseToAliases(it.value)) }.toMap()
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


    override fun getState() = this

    override fun loadState(state: SymbolSetSettings) = XmlSerializerUtil.copyBean(state, this)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [symbolSets] field.
         */
        val DEFAULT_SYMBOL_SETS: Map<String, String>
            get() = SymbolSet.defaultSymbolSets.toMap()

        /**
         * The persistent `SymbolSetSettings` instance.
         */
        val default: SymbolSetSettings
            get() = service()
    }
}
