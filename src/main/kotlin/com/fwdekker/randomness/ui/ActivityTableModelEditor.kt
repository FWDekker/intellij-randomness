package com.fwdekker.randomness.ui

import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.table.TableModelEditor
import javax.swing.JPanel


/**
 * An entry in the table.
 *
 * Associates a datum with a boolean that indicates whether the datum is active. The datum can be "edited" by changing
 * its reference to another datum.
 *
 * @param T the type of datum
 * @property active whether the entry is active
 * @property datum a reference to a piece of data
 */
data class EditableDatum<T>(var active: Boolean, var datum: T)


/**
 * A table that associates a certain data type with a checkbox.
 *
 * @param T the data type
 * @param columns the columns of the table, excluding the activity column
 * @param itemEditor describes what happens when a row is edited
 * @param emptyText the text to display when the table is empty
 */
abstract class ActivityTableModelEditor<T>(
    columns: Array<ColumnInfo<EditableDatum<T>, *>>,
    itemEditor: CollectionItemEditor<EditableDatum<T>>,
    emptyText: String
) : TableModelEditor<EditableDatum<T>>(
    arrayOf<ColumnInfo<EditableDatum<T>, *>>(createActivityColumn()).plus(columns),
    itemEditor,
    emptyText
) {
    companion object {
        /**
         * Whether newly added data are active by default.
         */
        const val DEFAULT_STATE = true


        /**
         * Creates a new column with checkboxes for (de)activating data.
         *
         * @return a new column with checkboxes for (de)activating data
         */
        private fun <T> createActivityColumn() = object : EditableColumnInfo<EditableDatum<T>, Boolean>() {
            override fun getColumnClass() = Boolean::class.java

            override fun valueOf(item: EditableDatum<T>) = item.active

            override fun setValue(item: EditableDatum<T>, value: Boolean) {
                item.active = value
            }
        }
    }


    /**
     * All data currently in the table.
     */
    var data: Collection<T>
        get() = model.items.map { it.datum }
        set(value) {
            model.items = value.map { EditableDatum(DEFAULT_STATE, it) }
        }

    /**
     * All data of which the checkbox is currently checked.
     */
    var activeData: Collection<T>
        get() = model.items.filter { it.active }.map { it.datum }
        set(value) {
            model.items.forEachIndexed { i, item -> model.setValueAt(item.datum in value, i, 0) }
        }


    /**
     * Creates a new `JPanel` with the table and the corresponding buttons.
     *
     * @return a new `JPanel` with the table and the corresponding buttons
     */
    override fun createComponent(): JPanel =
        TableModelEditor::class.java.getDeclaredField("toolbarDecorator")
            .apply { isAccessible = true }
            .let { (it.get(this) as ToolbarDecorator).createPanel() }
}
