package com.fwdekker.randomness.word

import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.ui.ActivityTableModelEditor
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.LocalPathCellEditor


private typealias EditableDictionary = EditableDatum<DictionaryReference>


/**
 * An editable table for selecting and editing [Dictionaries][Dictionary].
 *
 * @see WordSchemeEditor
 */
class DictionaryTable : ActivityTableModelEditor<DictionaryReference>(
    arrayOf(TYPE_COLUMN, LOCATION_COLUMN),
    ITEM_EDITOR,
    EMPTY_TEXT, EMPTY_SUB_TEXT,
    { !it.isBundled },
    columnAdjuster = { it[1].maxWidth = it[1].preferredWidth }
) {
    /**
     * Creates a new placeholder [Dictionary] instance.
     *
     * @return a new placeholder [Dictionary] instance
     */
    override fun createElement() = Companion.createElement()

    /**
     * Returns `null` if a unique, non-empty selection of valid dictionaries has been made, or a `ValidationInfo` object
     * explaining which input should be changed.
     *
     * @return `null` if a unique, non-empty selection of valid dictionaries has been made, or a `ValidationInfo` object
     * explaining which input should be changed
     */
    @Suppress("ReturnCount") // Acceptable for validation functions
    fun doValidate(): ValidationInfo? {
        if (data.distinct().size != data.size)
            return ValidationInfo("Dictionaries must be unique.", panel)
        if (activeData.isEmpty())
            return ValidationInfo("Select at least one dictionary.", panel)

        data.forEach { dictionary ->
            try {
                dictionary.validate()
            } catch (e: InvalidDictionaryException) {
                return ValidationInfo("Dictionary $dictionary is invalid: ${e.message}", panel)
            }

            if (dictionary.words.isEmpty())
                return ValidationInfo("Dictionary $dictionary is empty.", panel)
        }

        return null
    }


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
                if (item.datum.isBundled) "bundled"
                else "user"
        }

        /**
         * The column with the dictionary's file location.
         */
        private val LOCATION_COLUMN = object : EditableColumnInfo<EditableDictionary, String>("Location") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableDictionary) = item.datum.filename

            override fun setValue(item: EditableDictionary, value: String) {
                item.datum.filename = value.removePrefix("file://")
            }

            override fun isCellEditable(item: EditableDictionary) = !item.datum.isBundled

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

            override fun isRemovable(item: EditableDictionary) = !item.datum.isBundled

            override fun clone(item: EditableDictionary, forInPlaceEditing: Boolean) =
                EditableDatum(item.active, item.datum.copy())
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
        private fun createElement() = EditableDictionary(DEFAULT_STATE, DictionaryReference(false, ""))
    }
}
