package com.fwdekker.randomness.word

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.LocalPathCellEditor
import com.intellij.util.ui.table.TableModelEditor
import javax.swing.JPanel


/**
 * An editable table for selecting and editing [Dictionaries][Dictionary].
 */
class DictionaryTable : TableModelEditor<DictionaryTable.EditableDictionary>(
    arrayOf(ACTIVITY_COLUMN, TYPE_COLUMN, LOCATION_COLUMN), ITEM_EDITOR, EMPTY_TEXT) {
    companion object {
        /**
         * The column with checkboxes for (de)activating dictionaries.
         */
        private val ACTIVITY_COLUMN = object : EditableColumnInfo<EditableDictionary, Boolean>() {
            override fun getColumnClass() = Boolean::class.java

            override fun valueOf(item: EditableDictionary) = item.active

            override fun setValue(item: EditableDictionary, value: Boolean) {
                item.active = value
            }
        }

        /**
         * The column showing the type of the dictionary.
         */
        private val TYPE_COLUMN = object : ColumnInfo<EditableDictionary, String>("Type") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableDictionary) =
                item.dictionary.let { dictionary ->
                    when (dictionary) {
                        is BundledDictionary -> "bundled"
                        is UserDictionary -> "user"
                        else -> throw IllegalStateException("Unexpected dictionary implementation.")
                    }
                }
        }

        /**
         * The column with the dictionary's file location.
         */
        private val LOCATION_COLUMN = object : EditableColumnInfo<EditableDictionary, String>("Location") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableDictionary) =
                item.dictionary.let { dictionary ->
                    when (dictionary) {
                        is BundledDictionary -> dictionary.filename
                        is UserDictionary -> dictionary.filename
                        else -> throw IllegalStateException("Unexpected dictionary implementation.")
                    }
                }

            override fun setValue(item: EditableDictionary, value: String) {
                item.dictionary = UserDictionary.cache.get(value, true)
            }

            override fun isCellEditable(item: EditableDictionary) = item.dictionary is UserDictionary

            override fun getEditor(item: EditableDictionary?) =
                LocalPathCellEditor()
                    .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"))
                    .normalizePath(true)
        }

        /**
         * Describes how table rows are edited.
         */
        private val ITEM_EDITOR = object : CollectionItemEditor<EditableDictionary> {
            override fun getItemClass() = EditableDictionary::class.java

            override fun isRemovable(item: EditableDictionary) = item.dictionary is UserDictionary

            override fun clone(item: EditableDictionary, forInPlaceEditing: Boolean) =
                EditableDictionary(item.active, item.dictionary)
        }

        /**
         * The text that is displayed when the table is empty.
         */
        private const val EMPTY_TEXT = "No dictionaries configured."

        /**
         * Whether newly added dictionaries are active by default.
         */
        private const val DEFAULT_STATE = false
    }


    /**
     * All dictionaries currently in the table.
     */
    var dictionaries: Collection<Dictionary>
        get() = model.items.map { it.dictionary }
        set(value) {
            model.items = value.map { EditableDictionary(DEFAULT_STATE, it) }
        }

    /**
     * All dictionaries of which the checkbox is currently checked.
     */
    var activeDictionaries: Collection<Dictionary>
        get() = model.items.filter { it.active }.map { it.dictionary }
        set(value) {
            model.items.forEachIndexed { i, item -> model.setValueAt(item.dictionary in value, i, 0) }
        }


    /**
     * Creates a new `JPanel` with the table and the corresponding buttons.
     *
     * @return a new `JPanel` with the table and the corresponding buttons
     */
    override fun createComponent() = super.createComponent() as JPanel

    /**
     * Creates a new placeholder [Dictionary] instance.
     *
     * @return a new placeholder [Dictionary] instance
     */
    override fun createElement() = EditableDictionary(false, UserDictionary.cache.get("", true))


    /**
     * An entry in the table.
     *
     * Contains a reference to a dictionary that can be swapped with another reference, effectively allowing one to
     * edit the dictionary.
     *
     * @property active whether the dictionary is active
     * @property dictionary a reference to a dictionary
     */
    data class EditableDictionary(var active: Boolean, var dictionary: Dictionary)
}
