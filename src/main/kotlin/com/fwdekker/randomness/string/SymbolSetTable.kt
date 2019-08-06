package com.fwdekker.randomness.string

import com.fwdekker.randomness.ui.ActivityTableModelEditor
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.util.ui.CollectionItemEditor


private typealias EditableSymbolSet = EditableDatum<SymbolSet>


/**
 * An editable table for selecting and editing [SymbolSet]s.
 */
class SymbolSetTable : ActivityTableModelEditor<SymbolSet>(
    arrayOf(NAME_COLUMN, SYMBOLS_COLUMN), ITEM_EDITOR, EMPTY_TEXT) {
    companion object {
        /**
         * The column showing the names of the symbol sets.
         */
        private val NAME_COLUMN = object : EditableColumnInfo<EditableSymbolSet, String>("Name") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableSymbolSet) = item.datum.name

            override fun setValue(item: EditableSymbolSet, value: String) {
                item.datum.name = value
            }
        }

        /**
         * The columns showing the symbols of the symbol sets.
         */
        private val SYMBOLS_COLUMN = object : EditableColumnInfo<EditableSymbolSet, String>("Symbols") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableSymbolSet) = item.datum.symbols

            override fun setValue(item: EditableSymbolSet, value: String) {
                item.datum.symbols = value
            }
        }

        /**
         * Describes how table rows are edited.
         */
        private val ITEM_EDITOR = object : CollectionItemEditor<EditableSymbolSet> {
            // TODO Do not instantiate instance of `EditableSymbolSet`
            override fun getItemClass() = EditableSymbolSet(false, SymbolSet("", ""))::class.java

            override fun clone(item: EditableSymbolSet, forInPlaceEditing: Boolean) =
                EditableSymbolSet(item.active, item.datum)
        }

        /**
         * The text that is displayed when the table is empty.
         */
        private const val EMPTY_TEXT = "No symbol sets configured."
    }


    /**
     * Creates a new placeholder [SymbolSet] instance.
     *
     * @return a new placeholder [SymbolSet] instance
     */
    override fun createElement() = EditableSymbolSet(false, SymbolSet("", ""))
}
