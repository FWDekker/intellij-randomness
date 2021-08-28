package com.fwdekker.randomness.word

import com.fwdekker.randomness.ui.ActivityTableModelEditor
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.LocalPathCellEditor


private typealias EditableDictionary = EditableDatum<Dictionary>


/**
 * An editable table for selecting and editing [Dictionaries][Dictionary].
 *
 * @see WordSchemeEditor
 */
class DictionaryTable : ActivityTableModelEditor<Dictionary>(
    arrayOf(TYPE_COLUMN, LOCATION_COLUMN),
    ITEM_EDITOR,
    EMPTY_TEXT, EMPTY_SUB_TEXT,
    { it !is BundledDictionary },
    columnAdjuster = { it[1].maxWidth = it[1].preferredWidth }
) {
    /**
     * Creates a new placeholder [Dictionary] instance.
     *
     * @return a new placeholder [Dictionary] instance
     */
    override fun createElement() = Companion.createElement()


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The column showing the type of the dictionary.
         */
        private val TYPE_COLUMN = object : ColumnInfo<EditableDictionary, String>("Type") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableDictionary) =
                if (item.datum is BundledDictionary) "bundled"
                else "custom"
        }

        /**
         * The column with the dictionary's file location.
         */
        private val LOCATION_COLUMN = object : EditableColumnInfo<EditableDictionary, String>("Location") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableDictionary) = item.datum.toString()

            override fun setValue(item: EditableDictionary, value: String) {
                val datum = item.datum
                if (datum is UserDictionary)
                    datum.filename = value.removePrefix("file://")
            }

            override fun isCellEditable(item: EditableDictionary) = item.datum !is BundledDictionary

            override fun getEditor(item: EditableDictionary?) =
                LocalPathCellEditor()
                    .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"))
                    .normalizePath(true)
        }

        /**
         * Describes how table rows are edited.
         */
        private val ITEM_EDITOR = object : CollectionItemEditor<EditableDictionary> {
            override fun getItemClass() = createElement()::class.java

            override fun isRemovable(item: EditableDictionary) = item.datum !is BundledDictionary

            override fun clone(item: EditableDictionary, forInPlaceEditing: Boolean) =
                EditableDatum(item.active, item.datum.deepCopy())
        }

        /**
         * The text that is displayed when the table is empty.
         */
        const val EMPTY_TEXT = "No dictionaries configured."

        /**
         * The instruction that is displayed when the table is empty.
         */
        const val EMPTY_SUB_TEXT = "Add dictionary"


        /**
         * Creates a new placeholder [Dictionary] instance.
         *
         * @return a new placeholder [Dictionary] instance
         */
        private fun createElement() = EditableDictionary(DEFAULT_STATE, UserDictionary(""))
    }
}
