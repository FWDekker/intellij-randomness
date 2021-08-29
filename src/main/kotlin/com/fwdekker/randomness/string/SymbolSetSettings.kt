package com.fwdekker.randomness.string

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
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
    var serializedSymbolSets: Map<String, String> = DEFAULT_SYMBOL_SETS.toMutableMap(),
    @Suppress("unused") // At least two fields are required for serialization to work
    private val placeholder: String = ""
) : PersistentStateComponent<SymbolSetSettings>, Settings() {
    /**
     * Same as [symbolSets], except that serialized emoji have been deserialized.
     */
    @get:Transient
    var symbolSets: Map<String, String>
        get() = serializedSymbolSets.map { SymbolSet(it.key, EmojiParser.parseToUnicode(it.value)) }.toMap()
        set(value) {
            serializedSymbolSets = value.map { SymbolSet(it.key, EmojiParser.parseToAliases(it.value)) }.toMap()
        }

    /**
     * A list view of the `SymbolSet` objects described by [symbolSets].
     */
    @get:Transient
    var symbolSetList: Collection<SymbolSet>
        get() = symbolSets.toSymbolSets()
        set(value) {
            symbolSets = value.toMap()
        }


    /**
     * Returns this instance.
     *
     * @return this instance
     */
    override fun getState() = this

    /**
     * Invokes [copyFrom].
     *
     * @param state the state to invoke [copyFrom] on
     */
    override fun loadState(state: SymbolSetSettings) = copyFrom(state)


    override fun doValidate(): String? {
        val empty = symbolSetList.firstOrNull { it.symbols.isEmpty() }?.name

        return when {
            symbolSetList.isEmpty() -> "Add at least one symbol set."
            symbolSetList.any { it.name.isEmpty() } -> "All symbol sets should have a name."
            empty != null -> "Symbol set `$empty` should contain at least one symbol."
            else -> null
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(serializedSymbolSets = serializedSymbolSets.toMap())
            .also { if (retainUuid) it.uuid = uuid }


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
