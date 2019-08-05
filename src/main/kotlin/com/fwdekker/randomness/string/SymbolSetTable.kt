package com.fwdekker.randomness.string

import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.table.TableModelEditor
import javax.swing.JPanel


/**
 * An editable table for selecting and editing [SymbolSet]s.
 */
class SymbolSetTable : TableModelEditor<SymbolSetTable.EditableSymbolSet>(
    arrayOf(ACTIVITY_COLUMN, NAME_COLUMN, SYMBOLS_COLUMN), ITEM_EDITOR, EMPTY_TEXT) {
    companion object {
        /**
         * The column with checkboxes for (de)activating symbol sets.
         */
        private val ACTIVITY_COLUMN = object : EditableColumnInfo<EditableSymbolSet, Boolean>() {
            override fun getColumnClass() = Boolean::class.java

            override fun valueOf(item: EditableSymbolSet) = item.active

            override fun setValue(item: EditableSymbolSet, value: Boolean) {
                item.active = value
            }
        }

        /**
         * The column showing the names of the symbol sets.
         */
        private val NAME_COLUMN = object : EditableColumnInfo<EditableSymbolSet, String>("Name") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableSymbolSet) = item.symbolSet.name

            override fun setValue(item: EditableSymbolSet, value: String) {
                item.symbolSet.name = value
            }
        }

        /**
         * The columns showing the symbols of the symbol sets.
         */
        private val SYMBOLS_COLUMN = object : EditableColumnInfo<EditableSymbolSet, String>("Symbols") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableSymbolSet) = item.symbolSet.symbols

            override fun setValue(item: EditableSymbolSet, value: String) {
                item.symbolSet.symbols = value
            }
        }

        /**
         * Describes how table rows are edited.
         */
        private val ITEM_EDITOR = object : CollectionItemEditor<EditableSymbolSet> {
            override fun getItemClass() = EditableSymbolSet::class.java

            override fun clone(item: EditableSymbolSet, forInPlaceEditing: Boolean) =
                EditableSymbolSet(item.active, item.symbolSet)
        }

        /**
         * The text that is displayed when the table is empty.
         */
        private const val EMPTY_TEXT = "No symbol sets configured."

        /**
         * Whether newly added symbol sets are active by default.
         */
        private const val DEFAULT_STATE = false
    }


    /**
     * All symbol sets currently in the table.
     */
    var symbolSets: Collection<SymbolSet>
        get() = model.items.map { it.symbolSet }
        set(value) {
            model.items = value.map { EditableSymbolSet(DEFAULT_STATE, it) }
        }

    /**
     * All symbol sets of which the checkbox is currently checked.
     */
    var activeSymbolSets: Collection<SymbolSet>
        get() = model.items.filter { it.active }.map { it.symbolSet }
        set(value) {
            model.items.forEachIndexed { i, item -> model.setValueAt(item.symbolSet in value, i, 0) }
        }


    /**
     * Creates a new `JPanel` with the table and the corresponding buttons.
     *
     * @return a new `JPanel` with the table and the corresponding buttons
     */
    override fun createComponent() = super.createComponent() as JPanel

    /**
     * Creates a new placeholder [SymbolSet] instance.
     *
     * @return a new placeholder [SymbolSet] instance
     */
    override fun createElement() = EditableSymbolSet(false, SymbolSet("", ""))


    /**
     * An entry in the table.
     *
     * Contains a reference to a symbol set that can be swapped with another reference, effectively allowing one to
     * edit the symbol set.
     *
     * @property active whether the symbol set is active
     * @property symbolSet a reference to a symbol set
     */
    data class EditableSymbolSet(var active: Boolean, var symbolSet: SymbolSet)
}
