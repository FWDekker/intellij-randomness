package com.fwdekker.randomness.ui

import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StatusText
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
 * @param emptySubText the instruction to display when the table is empty
 * @param isCopyable returns `true` if and only if the given datum can be copied
 */
abstract class ActivityTableModelEditor<T>(
    columns: Array<ColumnInfo<EditableDatum<T>, *>>,
    itemEditor: CollectionItemEditor<EditableDatum<T>>,
    emptyText: String,
    val emptySubText: String,
    private val isCopyable: (T) -> Boolean = { true }
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
    override fun createComponent(): JPanel {
        @Suppress("UNCHECKED_CAST") // Reflection, see superclass for correctness
        val table = TableModelEditor::class.java.getDeclaredField("table")
            .apply { isAccessible = true }
            .get(this) as TableView<EditableDatum<T>>

        // TODO (#209) Use `LINK_PLAIN_ATTRIBUTES` instead of custom `SimpleTextAttributes`
        val style = SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBUI.CurrentTheme.Link.linkColor())
        table.emptyText.apply {
            appendSecondaryText(emptySubText, style) {
                model.addRow(createElement())

                val firstEditableColumn = (1..model.columnCount).first { model.isCellEditable(0, it) }
                table.setRowSelectionInterval(0, 0)
                table.setColumnSelectionInterval(0, 0)
                table.editCellAt(0, firstEditableColumn)
            }
            appendSecondaryText(
                " (${KeymapUtil.getFirstKeyboardShortcutText(CommonShortcuts.getNew())})",
                StatusText.DEFAULT_ATTRIBUTES,
                null
            )
        }

        return TableModelEditor::class.java.getDeclaredField("toolbarDecorator")
            .apply { isAccessible = true }
            .let { it.get(this) as ToolbarDecorator }
            .setAddAction {
                // Implementation based on `TableToolbarDecorator#createDefaultTableActions`
                TableUtil.stopEditing(table)

                table.rowCount.let { rowCount ->
                    if (canCreateElement()) model.addRow(createElement())
                    else model.addRow()

                    if (rowCount == table.rowCount) return@setAddAction
                }

                val newRowIndex = model.rowCount - 1
                val firstEditableColumn = (1..model.columnCount).first { model.isCellEditable(newRowIndex, it) }
                table.setRowSelectionInterval(newRowIndex, newRowIndex)
                table.setColumnSelectionInterval(0, 0)
                table.editCellAt(newRowIndex, firstEditableColumn)

                TableUtil.updateScroller(table)
                table.editorComponent?.let { table.scrollRectToVisible(it.bounds) }
                IdeFocusManager.getGlobalInstance()
                    .doWhenFocusSettlesDown { IdeFocusManager.getGlobalInstance().requestFocus(table, true) }
            }
            .addExtraAction(object :
                ToolbarDecorator.ElementActionButton(IdeBundle.message("button.copy"), PlatformIcons.COPY_ICON) {
                // Implementation based on `TableModelEditor#createComponent`
                override fun actionPerformed(e: AnActionEvent) {
                    TableUtil.stopEditing(table)

                    table.selectedObjects
                        .also { if (it.isEmpty()) return }
                        .forEach { model.addRow(itemEditor.clone(it, false)) }

                    TableUtil.updateScroller(table)
                    IdeFocusManager
                        .getGlobalInstance()
                        .doWhenFocusSettlesDown { IdeFocusManager.getGlobalInstance().requestFocus(table, true) }
                }

                override fun isEnabled() = table.selection.all { isCopyable(it.datum) }
            })
            .createPanel()
    }

    /**
     * Adds a listener that is called whenever this table is updated.
     *
     * @param listener the listener to invoke
     */
    fun addChangeListener(listener: () -> Unit) {
        modelListener(object : DataChangedListener<EditableDatum<T>>() {
            override fun dataChanged(columnInfo: ColumnInfo<EditableDatum<T>, *>, rowIndex: Int) = listener()
        })
    }
}
