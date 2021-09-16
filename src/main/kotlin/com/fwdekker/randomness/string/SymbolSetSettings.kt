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
    var serializedSymbolSets: List<SymbolSet> = DEFAULT_SYMBOL_SETS.toMutableList(),
    @Suppress("unused") // At least two fields are required for serialization to work
    private val placeholder: String = ""
) : PersistentStateComponent<SymbolSetSettings>, Settings() {
    /**
     * A list view of the deserialized `SymbolSet` objects described by [serializedSymbolSets].
     */
    @get:Transient
    var symbolSets: List<SymbolSet>
        get() = serializedSymbolSets.map {
            SymbolSet(it.name, EmojiParser.parseToUnicode(it.symbols).replace("\\\\", "\\").replace("\\:", ":"))
        }
        set(value) {
            serializedSymbolSets = value.map {
                SymbolSet(it.name, EmojiParser.parseToAliases(it.symbols.replace("\\", "\\\\").replace(":", "\\:")))
            }
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
        val symbolSetNames = symbolSets.map { it.name }
        val duplicate = symbolSetNames.firstOrNull { symbolSet -> symbolSetNames.count { it == symbolSet } > 1 }
        val empty = symbolSets.firstOrNull { it.symbols.isEmpty() }?.name

        return when {
            symbolSets.isEmpty() -> "Add at least one symbol set."
            symbolSets.any { it.name.isEmpty() } -> "All symbol sets should have a name."
            duplicate != null -> "Multiple symbol sets with name '$duplicate'."
            empty != null -> "Symbol set `$empty` should contain at least one symbol."
            else -> null
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(serializedSymbolSets = serializedSymbolSets.map { it.copy() })
            .also { if (retainUuid) it.uuid = uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [symbolSets] field.
         */
        val DEFAULT_SYMBOL_SETS: List<SymbolSet>
            get() = SymbolSet.defaultSymbolSets

        /**
         * The persistent `SymbolSetSettings` instance.
         */
        val default: SymbolSetSettings
            get() = service()
    }
}
