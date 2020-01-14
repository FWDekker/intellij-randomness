package com.fwdekker.randomness.string

import com.fwdekker.randomness.ui.ActivityTableModelEditor
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.ui.AbstractTableCellEditor
import com.intellij.util.ui.CollectionItemEditor
import javax.swing.JTable


private typealias EditableSymbolSet = EditableDatum<SymbolSet>


/**
 * An editable table for selecting and editing [SymbolSet]s.
 *
 * @see StringSettingsComponent
 */
class SymbolSetTable : ActivityTableModelEditor<SymbolSet>(
    arrayOf(NAME_COLUMN, SYMBOLS_COLUMN), ITEM_EDITOR, EMPTY_TEXT, EMPTY_SUB_TEXT) {
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
         * The column showing the symbols of the symbol sets.
         */
        private val SYMBOLS_COLUMN = object : EditableColumnInfo<EditableSymbolSet, String>("Symbols") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableSymbolSet) = item.datum.symbols

            override fun setValue(item: EditableSymbolSet, value: String) {
                item.datum.symbols = value
            }

            override fun getEditor(item: EditableSymbolSet?) =
                object : AbstractTableCellEditor() {
                    private var component: ExpandableTextField? = null

                    override fun getTableCellEditorComponent(
                        table: JTable?,
                        value: Any?,
                        isSelected: Boolean,
                        row: Int,
                        column: Int
                    ): ExpandableTextField =
                        ExpandableTextField({ it.split("\n") }, { it.joinToString("\n") })
                            .also {
                                it.text = value as String
                                component = it
                            }

                    override fun getCellEditorValue() = component?.text
                }
        }

        /**
         * Describes how table rows are edited.
         */
        private val ITEM_EDITOR = object : CollectionItemEditor<EditableSymbolSet> {
            override fun getItemClass() = createElement()::class.java

            override fun clone(item: EditableSymbolSet, forInPlaceEditing: Boolean) =
                EditableSymbolSet(item.active, SymbolSet(item.datum.name, item.datum.symbols))
        }

        /**
         * The text that is displayed when the table is empty.
         */
        private const val EMPTY_TEXT = "No symbol sets configured."

        /**
         * The instruction that is displayed when the table is empty.
         */
        private const val EMPTY_SUB_TEXT = "Add symbol set"


        /**
         * Creates a new placeholder [SymbolSet] instance.
         *
         * @return a new placeholder [SymbolSet] instance
         */
        private fun createElement() = EditableSymbolSet(DEFAULT_STATE, SymbolSet("", ""))
    }


    /**
     * Creates a new placeholder [SymbolSet] instance.
     *
     * @return a new placeholder [SymbolSet] instance
     */
    override fun createElement() = Companion.createElement()
}
