package com.fwdekker.randomness.string

import com.fwdekker.randomness.ValidationInfo
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
 * @see StringSchemeEditor
 */
class SymbolSetTable : ActivityTableModelEditor<SymbolSet>(
    arrayOf(NAME_COLUMN, SYMBOLS_COLUMN),
    ITEM_EDITOR,
    EMPTY_TEXT, EMPTY_SUB_TEXT
) {
    /**
     * Creates a new placeholder [SymbolSet] instance.
     *
     * @return a new placeholder [SymbolSet] instance
     */
    override fun createElement() = Companion.createElement()

    /**
     * Validates the symbol sets entered into this table.
     *
     * @param excludeLookAlikeSymbols `true` if and only if look-alike symbols are excluded
     * @return `null` if the input is valid, or a `ValidationInfo` object explaining why the input is invalid
     */
    fun doValidate(excludeLookAlikeSymbols: Boolean): ValidationInfo? {
        val duplicateName = data.map { it.name }.firstNonDistinctOrNull()
        val emptySymbolSet = data.firstOrNull { it.symbols.isEmpty() }

        return when {
            data.isEmpty() ->
                ValidationInfo("Add at least one symbol set.", panel)
            data.any { it.name.isEmpty() } ->
                ValidationInfo("All symbol sets should have a name.", panel)
            duplicateName != null ->
                ValidationInfo("There are multiple symbol sets with the name `$duplicateName`.", panel)
            emptySymbolSet != null ->
                ValidationInfo("Symbol set `$emptySymbolSet` should contain at least one symbol.", panel)
            activeData.isEmpty() ->
                ValidationInfo("Activate at least one symbol set.", panel)
            activeData.sum(excludeLookAlikeSymbols).isEmpty() ->
                ValidationInfo(
                    "Active symbol sets should contain at least one non-look-alike character if look-alike " +
                        "characters are excluded.",
                    panel
                )
            else -> null
        }
    }


    /**
     * Holds constants.
     */
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
        const val EMPTY_TEXT = "No symbol sets configured."

        /**
         * The instruction that is displayed when the table is empty.
         */
        const val EMPTY_SUB_TEXT = "Add symbol set"


        /**
         * Creates a new placeholder [SymbolSet] instance.
         *
         * @return a new placeholder [SymbolSet] instance
         */
        private fun createElement() = EditableSymbolSet(DEFAULT_STATE, SymbolSet("", ""))
    }
}


/**
 * Returns the first string that occurs multiple times, or `null` if there is no such string.
 *
 * @return the first string that occurs multiple times, or `null` if there is no such string
 */
private fun List<String>.firstNonDistinctOrNull() = firstOrNull { indexOf(it) != lastIndexOf(it) }
