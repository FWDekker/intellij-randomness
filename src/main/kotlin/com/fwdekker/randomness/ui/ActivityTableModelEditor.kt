package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.StatusText
import com.intellij.util.ui.table.TableModelEditor
import javax.swing.JPanel
import javax.swing.event.TableModelEvent
import javax.swing.table.TableColumn


/**
 * An entry in the table.
 *
 * Associates a datum with a boolean that indicates whether the datum is active. The datum can be "edited" by changing
 * its reference to another datum.
 *
 * @param T the type of datum
 * @property active Whether the entry is active.
 * @property datum A reference to a piece of data.
 */
data class EditableDatum<T>(var active: Boolean, var datum: T)


/**
 * A table that associates a certain data type with a checkbox.
 *
 * @param T the data type
 * @param columns the columns of the table, excluding the activity column
 * @param itemEditor describes what happens when a row is edited
 * @param emptyText the text to display when the table is empty
 * @param emptySubText the instruction to display when the table is empty
 * @param isCopyable returns `true` if and only if the given datum can be copied
 * @param columnAdjuster invoked on the list of columns (including the activity column) to allow adjusting their
 * properties
 */
abstract class ActivityTableModelEditor<T>(
    columns: Array<ColumnInfo<EditableDatum<T>, *>>,
    itemEditor: CollectionItemEditor<EditableDatum<T>>,
    emptyText: String,
    private val emptySubText: String,
    private val isCopyable: (T) -> Boolean = { true },
    private val columnAdjuster: (List<TableColumn>) -> Unit = {}
) : TableModelEditor<EditableDatum<T>>(
    arrayOf<ColumnInfo<EditableDatum<T>, *>>(createActivityColumn()).plus(columns),
    itemEditor,
    emptyText
) {
    /**
     * The panel in which the table editor is present.
     */
    val panel: JPanel

    /**
     * All data currently in the table.
     */
    var data: List<T>
        get() = model.items.map { it.datum }
        set(value) {
            model.items = value.map { EditableDatum(DEFAULT_STATE, it) }
        }

    /**
     * All data of which the checkbox is currently checked.
     */
    var activeData: List<T>
        get() = model.items.filter { it.active }.map { it.datum }
        set(value) {
            model.items.forEachIndexed { i, item -> model.setValueAt(item.datum in value, i, 0) }
        }


    init {
        panel = createComponent()
    }


    /**
     * Creates a new [JPanel] with the table and the corresponding buttons.
     *
     * Do not use this method; instead, use the [panel] property.
     *
     * @return a new [JPanel] with the table and the corresponding buttons
     */
    final override fun createComponent(): JPanel {
        @Suppress("UNCHECKED_CAST") // Reflection, see superclass for correctness
        val table = TableModelEditor::class.java.getDeclaredField("table")
            .apply { isAccessible = true }
            .get(this) as TableView<EditableDatum<T>>

        columnAdjuster(table.columnModel.columns.toList())

        table.emptyText.apply {
            appendSecondaryText(emptySubText, SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES) { addItem(table) }
            appendSecondaryText(
                " (${KeymapUtil.getFirstKeyboardShortcutText(CommonShortcuts.getNew())})",
                StatusText.DEFAULT_ATTRIBUTES,
                null
            )
        }

        // Implementation based on `TableModelEditor#createComponent`
        return TableModelEditor::class.java.getDeclaredField("toolbarDecorator")
            .apply { isAccessible = true }
            .let { it.get(this) as ToolbarDecorator }
            .setAddAction { addItem(table) }
            .setEditAction {
                if (table.selectedObject != null)
                    table.editCellAt(table.selectedRow, table.selectedColumn)
            }
            .setEditActionUpdater { table.selectedObject?.let { isEditable(it) } == true }
            .addExtraAction(
                object : ToolbarDecorator.ElementActionButton(
                    Bundle("shared.action.copy"),
                    PlatformIcons.COPY_ICON
                ) {
                    override fun actionPerformed(e: AnActionEvent) = copySelectedItems(table)

                    override fun isEnabled() = table.selection.all { isCopyable(it.datum) }
                }
            )
            .createPanel()
    }

    /**
     * Adds [listener] so that it is invoked whenever this table is updated.
     *
     * @param listener the listener to invoke
     */
    fun addChangeListener(listener: () -> Unit) {
        modelListener(object : DataChangedListener<EditableDatum<T>>() {
            override fun tableChanged(e: TableModelEvent) = listener()

            override fun dataChanged(columnInfo: ColumnInfo<EditableDatum<T>, *>, rowIndex: Int) = listener()
        })
    }

    /**
     * Returns `true`.
     *
     * @return `true`
     */
    final override fun canCreateElement() = true


    /**
     * Adds a new item to [table], brings that item into view, and applies focus to that item's first editable column.
     *
     * @param table the table to add the item to
     */
    private fun addItem(table: TableView<EditableDatum<T>>) {
        // Implementation based on `TableToolbarDecorator#createDefaultTableActions`
        TableUtil.stopEditing(table)

        table.rowCount.let { rowCount ->
            model.addRow(createElement())

            if (rowCount == table.rowCount) return
        }

        val newRowIndex = model.rowCount - 1
        val firstEditableColumn = (1 until model.columnCount).first { model.isCellEditable(newRowIndex, it) }
        table.setRowSelectionInterval(newRowIndex, newRowIndex)
        table.setColumnSelectionInterval(firstEditableColumn, firstEditableColumn)
        table.editCellAt(newRowIndex, firstEditableColumn)

        TableUtil.updateScroller(table)
        table.editorComponent?.let { table.scrollRectToVisible(it.bounds) }
        table.editCellAt(newRowIndex, firstEditableColumn)
    }

    /**
     * Copies all selected items in [table].
     *
     * @param table the table to copy the selected items in
     */
    private fun copySelectedItems(table: TableView<EditableDatum<T>>) {
        TableUtil.stopEditing(table)

        table.selectedObjects.forEach { model.addRow(itemEditor.clone(it, false)) }

        TableUtil.updateScroller(table)
    }


    /**
     * Holds constants.
     */
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
}
