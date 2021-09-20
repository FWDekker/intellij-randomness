package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
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
    Bundle("word.dictionary.ui.empty"), Bundle("word.dictionary.ui.empty_sub"),
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
     * Returns true if and only if the given item can be edited.
     *
     * @param item the item to check for editability
     * @return true if and only if the given item can be edited
     */
    override fun isEditable(item: EditableDatum<Dictionary>) = item.datum !is BundledDictionary


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The column showing the type of the dictionary.
         */
        private val TYPE_COLUMN =
            object : ColumnInfo<EditableDictionary, String>(Bundle("word.dictionary.ui.type_column")) {
                override fun getColumnClass() = String::class.java

                override fun valueOf(item: EditableDictionary) =
                    if (item.datum is BundledDictionary) Bundle("word.dictionary.bundled")
                    else Bundle("word.dictionary.custom")
            }

        /**
         * The column with the dictionary's file location.
         */
        private val LOCATION_COLUMN =
            object : EditableColumnInfo<EditableDictionary, String>(Bundle("word.dictionary.ui.location_column")) {
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
         * Creates a new placeholder [Dictionary] instance.
         *
         * @return a new placeholder [Dictionary] instance
         */
        private fun createElement() = EditableDictionary(DEFAULT_STATE, UserDictionary(""))
    }
}
