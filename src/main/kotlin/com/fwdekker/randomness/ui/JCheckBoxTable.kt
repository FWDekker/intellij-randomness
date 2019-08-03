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
import javax.swing.event.TableModelEvent
import javax.swing.table.DefaultTableModel


private typealias EntryActivityChangeListener = (Int) -> Unit


/**
 * A [JList][javax.swing.JTable] in which each row has a [JCheckBox][javax.swing.JCheckBox] in front of it.
 *
 * @param <T> the entry type
 * @param columnCount the number of columns in the table, excluding the checkbox column
 * @param editableColumns the indices of the columns that can be edited, ignoring the checkbox column
 * @param columnNames the names of the columns, excluding the checkbox column
 * @param listToEntry a function to convert a list of strings to the object to be stored in the table
 * @param entryToList a function to convert an object to be stored in the table to a list of strings
 * @param name the name of this component
 */
class JCheckBoxTable<T>(
    columnCount: Int,
    editableColumns: Collection<Int> = IntRange(0, columnCount).toList(),
    columnNames: Collection<String> = emptyList(),
    val isEntryEditable: ((T) -> Boolean) = { true },
    val listToEntry: ((List<String>) -> T),
    val entryToList: ((T) -> List<String>),
    name: String? = null
) : JBTable() {
    companion object {
        /**
         * The column index of the checkbox column.
         */
        private const val CHECKBOX_COL = 0

        /**
         * The width of the table that should be taken up by the checkbox column, expressed as a percentage.
         */
        private const val CHECKBOX_WIDTH_PERCENTAGE = 0.1
    }

    private val model = DefaultTableModel(0, columnCount + 1)
    private val entryActivityChangeListeners = mutableListOf<EntryActivityChangeListener>()
    private val editableColumns = editableColumns.map { if (it < CHECKBOX_COL) it else it + 1 }.plus(CHECKBOX_COL)
    private val columnNames = columnNames.toMutableList().also { if (it.isNotEmpty()) it.add(CHECKBOX_COL, "") }

    /**
     * Returns a list of all entries.
     *
     * @return a list of all entries
     */
    val entries: List<T>
        get() = (0 until entryCount).map { this.getEntry(it) }.toList()

    /**
     * Returns the number of entries in the list.
     *
     * @return the number of entries in the list
     */
    val entryCount: Int
        get() = model.rowCount

    /**
     * Returns all entries of which the checkbox is checked.
     *
     * @return all entries of which the checkbox is checked
     */
    val activeEntries: List<T>
        get() = entries.filter { this.isActive(it) }

    /**
     * Returns the entry that is currently selected by the user, if there is one.
     *
     * @return the entry that is currently selected by the user, if there is one
     */
    val highlightedEntries: List<T>
        get() = selectedRows.toList().map { this.getEntry(it) }


    init {
        require(columnCount >= 1) { "`columnCount` must be at least 1." }

        setName(name)
        setModel(model)

        model.addTableModelListener { event ->
            if (event.type == TableModelEvent.UPDATE && event.column == CHECKBOX_COL)
                entryActivityChangeListeners.forEach { listener -> listener(event.firstRow) }
        }

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        if (columnNames.isEmpty())
            setTableHeader(null)
        else
            model.setColumnIdentifiers(this.columnNames.toTypedArray())

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent?) = resizeColumns()
        })
    }


    /**
     * Adds an entry to the list.
     *
     *
     * If the given entry is already in the list, nothing happens.
     *
     * @param entry the entry to add
     */
    fun addEntry(entry: T) {
        if (!hasEntry(entry)) {
            model.addRow(
                mutableListOf<Any>().also { list ->
                    list.addAll(entryToList(entry))
                    list.add(CHECKBOX_COL, false)
                }.toTypedArray()
            )
        }
    }

    /**
     * Removes all current entries, and adds the given entries.
     *
     * @param entries the entries to add
     */
    fun setEntries(entries: Collection<T>) {
        clear()
        entries.forEach { this.addEntry(it) }
    }

    /**
     * Returns `true` iff the given entry exists in the list.
     *
     * @param entry the entry to check for presence
     * @return `true` iff the given entry exists in the list
     */
    private fun hasEntry(entry: T) = entries.any { it == entry }

    /**
     * Returns the entry in the given row.
     *
     * @param row the row to return the entry of
     * @return the entry in the given row
     */
    fun getEntry(row: Int): T =
        listToEntry((0 until model.columnCount)
            .minus(CHECKBOX_COL)
            .map { getValueAt(row, it) as String })

    /**
     * Returns the row number of the given entry.
     *
     * @param entry the entry to return the row number of
     * @return the row number of the given entry
     */
    private fun getEntryRow(entry: T) =
        (0 until entryCount)
            .firstOrNull { getEntry(it) == entry }
            ?: throw NoSuchElementException("No row with entry `$entry` found.")

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
     * Sets whether the given entry has its checkbox checked.
     *
     * @param entry the entry to (un)check the checkbox of
     * @param selected `true` iff the entry's checkbox should be checked
     */
    private fun setActive(entry: T, selected: Boolean) = setValueAt(selected, getEntryRow(entry), CHECKBOX_COL)

    /**
     * Returns `true` iff the given entry's checkbox is checked.
     *
     * @param entry the entry of which to return the checkbox's status
     * @return `true` iff the given entry's checkbox is checked
     */
    fun isActive(entry: T): Boolean = getValueAt(getEntryRow(entry), CHECKBOX_COL) as Boolean

    /**
     * Sets the activity of the given entry.
     *
     * @param entry the entry to set the activity of
     * @param activity `true` iff the entry's checkbox should be checked
     */
    fun setEntryActivity(entry: T, activity: Boolean) = setValueAt(activity, getEntryRow(entry), CHECKBOX_COL)

    /**
     * Checks the checkboxes of all given entries, and unchecks all other checkboxes.
     *
     * @param entries exactly those entries of which the checkboxes should be checked
     */
    fun setActiveEntries(entries: Collection<T>) = this.entries.forEach { setActive(it, entries.contains(it)) }

    /**
     * Adds a listener that is called when a row's activity is changed by the user.
     *
     * @param listener the listener to be added
     */
    fun addEntryActivityChangeListener(listener: EntryActivityChangeListener) {
        entryActivityChangeListeners.add(listener)
    }

    /**
     * Removes a listener that was called when a row's activity was changed by the user.
     *
     * @param listener the listener to be removed
     */
    fun removeEntryActivityChangeListener(listener: EntryActivityChangeListener) {
        entryActivityChangeListeners.remove(listener)
    }


    /**
     * Recalculates column widths.
     */
    private fun resizeColumns() {
        columnModel.getColumn(CHECKBOX_COL).apply { maxWidth = minWidth }
    }

    /**
     * Returns the name of the [column]th column.
     *
     * @param column the index of the column to return the name of
     */
    override fun getColumnName(column: Int) = columnNames[column]

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
            else -> throw IllegalArgumentException("JEditableList has only two columns.")
        }

    /**
     * Returns `true` iff `column` is CHECKBOX_COL.
     *
     * @param row the row whose value is to be queried
     * @param column the columns whose value is to be queried
     * @return `true` iff `column` is CHECKBOX_COL
     */
    override fun isCellEditable(row: Int, column: Int) = column in editableColumns && isEntryEditable(getEntry(row))
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
    val table: JCheckBoxTable<T>,
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
