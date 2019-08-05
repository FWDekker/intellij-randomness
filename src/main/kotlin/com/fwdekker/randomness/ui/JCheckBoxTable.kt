package com.fwdekker.randomness.ui

import com.intellij.ui.AnActionButton
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.NoSuchElementException
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel


/**
 * A [JList][javax.swing.JTable] in which each row has a [JCheckBox][javax.swing.JCheckBox] in front of it.
 *
 * @param <T> the entry type
 * @param columns the columns in the table, excluding the checkbox column
 * @param listToEntry a function to convert a list of strings to the object to be stored in the table
 * @param entryToList a function to convert an object to be stored in the table to a list of strings
 * @param isEntryEditable returns `true` iff the given entry should be editable inline
 */
class JCheckBoxTable<T>(
    private val columns: List<Column>,
    private val listToEntry: ((List<String>) -> T),
    private val entryToList: ((T) -> List<String>),
    private val isEntryEditable: ((T) -> Boolean) = { false }
) : JBTable() {
    companion object {
        /**
         * The name of the checkbox column.
         */
        const val CHECKBOX_COL_NAME = ""
    }

    private val model = DefaultTableModel(0, columns.size + 1)

    /**
     * Returns the number of entries in the list.
     *
     * @return the number of entries in the list
     */
    val entryCount: Int
        get() = model.rowCount

    /**
     * Returns a list of all entries.
     *
     * @return a list of all entries
     */
    var entries: Collection<T>
        get() = (0 until entryCount).map { this.getEntry(it) }.toList()
        set(value) {
            clear()
            value.forEach { addEntry(it) }
        }

    /**
     * Returns all entries of which the checkbox is checked.
     *
     * @return all entries of which the checkbox is checked
     */
    var activeEntries: Collection<T>
        get() = entries.filter { this.isActive(it) }
        set(value) {
            entries.forEach { setActive(it, value.contains(it)) }
        }

    /**
     * Returns the entries that are currently selected by the user, if there are any.
     *
     * @return the entries that are currently selected by the user, if there are any
     */
    var highlightedEntries: List<T>
        get() = selectedRows.toList().map { this.getEntry(it) }
        set(value) {
            clearSelection()
            value.map { getEntryRow(it) }.forEach { addRowSelectionInterval(it, it) }
        }


    init {
        require(columns.isNotEmpty()) { "`JCheckBoxTable` must contain at least one column." }

        setModel(model)
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)

        if (columns.all { it.name == null })
            setTableHeader(null)
        else
            model.setColumnIdentifiers(listOf(CHECKBOX_COL_NAME).plus(columns.map { it.name }).toTypedArray())

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent?) = resizeColumns()
        })
    }


    /**
     * Returns the entry in the given row.
     *
     * This method describes the de facto method of retrieving entries from the underlying model and is used by many
     * other methods.
     *
     * @param row the row to return the entry of
     * @return the entry in the given row
     */
    fun getEntry(row: Int): T {
        require(row >= 0 && row < model.rowCount) { "Index out of bounds. rows=${model.rowCount}, index=$row." }

        return listToEntry((1 until model.columnCount).map { getValueAt(row, it) as String })
    }

    /**
     * Returns the row number of the given entry.
     *
     * @param entry the entry to return the row number of
     * @return the row number of the given entry
     */
    private fun getEntryRow(entry: T) =
        (0 until entryCount).firstOrNull { getEntry(it) == entry }
            ?: throw NoSuchElementException("No row with entry `$entry` found.")

    /**
     * Returns `true` iff the given entry exists in the list.
     *
     * @param entry the entry to check for presence
     * @return `true` iff the given entry exists in the list
     */
    fun hasEntry(entry: T) = entries.any { it == entry }

    /**
     * Adds an entry to the end of list.
     *
     * @param entry the entry to add; must not already be in the table
     * @param active `true` iff the value should be active after it is inserted
     */
    fun addEntry(entry: T, active: Boolean = false) {
        require(!hasEntry(entry)) { "Cannot add duplicate entry." }

        model.addRow(listOf<Any>(active).plus(entryToList(entry)).toTypedArray())
    }

    /**
     * Replaces the entry in the given row with the given entry, optionally changing its activity.
     *
     * This method should only be used to overwrite existing
     *
     * @param row the row to set the entry in
     * @param entry the entry to overwrite the current entry with; must not already be present in another row
     * @param active `null` if the activity should be untouched, or the value to set it to otherwise
     */
    fun setEntry(row: Int, entry: T, active: Boolean? = null) {
        require(row >= 0 && row < model.rowCount) { "Index out of bounds. rows=${model.rowCount}, index=$row." }
        require(!hasEntry(entry) || getEntryRow(entry) == row) { "Cannot add duplicate entry." }

        entryToList(entry).forEachIndexed { i, s -> setValueAt(s, row, i + 1) }

        if (active != null)
            setActive(entry, active)
    }

    /**
     * Removes the given entry and its checkbox.
     *
     * @param entry the entry to remove
     */
    fun removeEntry(entry: T) = model.removeRow(getEntryRow(entry))

    /**
     * Removes all entries.
     */
    fun clear() = repeat(model.rowCount) { model.removeRow(0) }


    /**
     * Returns `true` iff the given entry's checkbox is checked.
     *
     * @param entry the entry of which to return the checkbox's status
     * @return `true` iff the given entry's checkbox is checked
     */
    fun isActive(entry: T): Boolean = getValueAt(getEntryRow(entry), 0) as Boolean

    /**
     * Sets whether the given entry has its checkbox checked.
     *
     * @param entry the entry to (un)check the checkbox of
     * @param isActive `true` iff the entry's checkbox should be checked
     */
    fun setActive(entry: T, isActive: Boolean) = setValueAt(isActive, getEntryRow(entry), 0)


    /**
     * Returns the name of the [column]th column.
     *
     * The index uses the model's indexing rather than the indexing of the user-supplied columns. Therefore, an index of
     * `0` refers to the checkbox column.
     *
     * @param column the index of the column to return the name of
     */
    override fun getColumnName(column: Int): String? {
        require(column >= 0 && column < model.columnCount) {
            "Index out of bounds. columns=${model.columnCount}, index=$column."
        }

        return if (column == 0) CHECKBOX_COL_NAME
        else columns[column - 1].name
    }

    /**
     * Returns the class of the data in the column with index `column`.
     *
     * @param column the index of the column to return the class of
     * @return the class of the data in the column with index `column`
     */
    override fun getColumnClass(column: Int) =
        when (column) {
            // Java classes MUST be used
            0 -> java.lang.Boolean::class.java
            in 1 until model.columnCount -> java.lang.String::class.java
            else -> throw IllegalArgumentException("Index out of bounds. columns=${model.columnCount}, index=$column.")
        }

    /**
     * Returns `true` iff `column` is CHECKBOX_COL.
     *
     * Behavior is undefined if the row or column does not exist.
     *
     * @param row the row whose value is to be queried
     * @param column the columns whose value is to be queried
     * @return `true` iff `column` is CHECKBOX_COL
     */
    @Suppress("UnnecessaryParentheses") // TODO: See arturbosch/detekt#1592. Helpful around boolean conditions.
    override fun isCellEditable(row: Int, column: Int) =
        column == 0 || (columns[column - 1].isEditable && isEntryEditable(getEntry(row)))


    /**
     * Recalculates column widths.
     */
    private fun resizeColumns() {
        columnModel.getColumn(0).apply { maxWidth = minWidth }
    }


    /**
     * A column in a [JCheckBoxTable].
     *
     * @property name the name of the column, if any
     * @property isEditable `true` iff the column's value can be edited
     */
    data class Column(val name: String? = null, val isEditable: Boolean = false)
}


/**
 * A decorated version of a [JCheckBoxTable], including buttons for adding, editing, and removing entries as desired.
 *
 * @param <T> the entry type
 * @param table the table to be decorated
 * @param addAction the action to perform on the currently-highlighted entries when the add button is clicked; `null`
 * hides the button
 * @param editAction the action to perform on the currently-highlighted entries when the edit button is clicked; `null`
 * hides the button
 * @param removeAction the action to perform on the currently-highlighted entries when the remove button is clicked;
 * `null` hides the button
 * @param addActionUpdater given the currently-highlighted entries, returns {@code true} iff the add button should be
 * enabled
 * @param editActionUpdater given the currently-highlighted entries, returns {@code true} iff the edit button should be
 * enabled
 * @param removeActionUpdater given the currently-highlighted entries, returns {@code true} iff the remove button should
 * be enabled
 */
class JDecoratedCheckBoxTablePanel<T>(
    table: JCheckBoxTable<T>,
    addAction: ((List<T>) -> Unit)? = null,
    editAction: ((List<T>) -> Unit)? = null,
    removeAction: ((List<T>) -> Unit)? = null,
    addActionUpdater: ((List<T>) -> Boolean) = { true },
    editActionUpdater: ((List<T>) -> Boolean) = { it.size == 1 },
    removeActionUpdater: ((List<T>) -> Boolean) = { it.isNotEmpty() }
) : JPanel(BorderLayout()) {
    /**
     * The panel that contains the decorating buttons.
     */
    val actionsPanel: CommonActionsPanel


    init {
        ToolbarDecorator.createDecorator(table)
            .apply {
                if (addAction != null) setAddAction { addAction(table.highlightedEntries) }
                setAddActionUpdater { addActionUpdater(table.highlightedEntries) }
                if (editAction != null) setEditAction { editAction(table.highlightedEntries) }
                setEditActionUpdater { editActionUpdater(table.highlightedEntries) }
                if (removeAction != null) setRemoveAction { removeAction(table.highlightedEntries) }
                setRemoveActionUpdater { removeActionUpdater(table.highlightedEntries) }
            }
            .also { decorator ->
                add(decorator.createPanel())
                actionsPanel = decorator.actionsPanel
            }
    }


    /**
     * Returns the specified button, or `null` if it is not present in this list.
     *
     * @param button the type of the button to return
     * @return the specified button, or `null` if it is not present in this list
     */
    fun getButton(button: CommonActionsPanel.Buttons): AnActionButton? = actionsPanel.getAnActionButton(button)
}
