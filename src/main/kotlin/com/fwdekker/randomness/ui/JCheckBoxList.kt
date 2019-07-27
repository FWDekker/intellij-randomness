package com.fwdekker.randomness.ui

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
 * A [JList][javax.swing.JList] in which each entry has a [JCheckBox][javax.swing.JCheckBox] in front of it.
 *
 * @param <T> the entry type
 * @param name the name of this component
 */
class JCheckBoxList<T>(name: String? = null) : JBTable() {
    companion object {
        /**
         * The relative width of the checkbox column.
         */
        private const val CHECKBOX_WIDTH = 20.0f
        /**
         * The relative width of the text column.
         */
        private const val TEXT_WIDTH = 80.0f
    }

    private val model = DefaultTableModel(0, 2)
    private val entryActivityChangeListeners = mutableListOf<EntryActivityChangeListener>()

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
    val highlightedEntry: T?
        get() = selectedRows.toList().map { this.getEntry(it) }.firstOrNull()


    init {
        setName(name)
        setModel(model)

        model.addTableModelListener { event ->
            if (event.type == TableModelEvent.UPDATE && event.column == 0) {
                entryActivityChangeListeners.forEach { listener -> listener(event.firstRow) }
            }
        }

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        setTableHeader(null)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent?) {
                super.componentResized(event)

                resizeColumns()
            }
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
        if (!hasEntry(entry))
            model.addRow(arrayOf(false, entry))
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
    private fun hasEntry(entry: T) = entries.any { row -> row == entry }

    /**
     * Returns the entry in the given row.
     *
     * @param row the row to return the entry of
     * @return the entry in the given row
     */
    @Suppress("UNCHECKED_CAST") // Type guaranteed by design
    fun getEntry(row: Int): T = model.getValueAt(row, 1) as T

    /**
     * Returns the row number of the given entry.
     *
     * @param entry the entry to return the row number of
     * @return the row number of the given entry
     */
    private fun getEntryRow(entry: T) =
        (0 until entryCount)
            .firstOrNull { row -> getEntry(row) == entry }
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
    fun clear() = repeat(entryCount) { model.removeRow(0) }

    /**
     * Sets whether the given entry has its checkbox checked.
     *
     * @param entry the entry to (un)check the checkbox of
     * @param selected `true` iff the entry's checkbox should be checked
     */
    private fun setActive(entry: T, selected: Boolean) = setValueAt(selected, getEntryRow(entry), 0)

    /**
     * Returns `true` iff the given entry's checkbox is checked.
     *
     * @param entry the entry of which to return the checkbox's status
     * @return `true` iff the given entry's checkbox is checked
     */
    fun isActive(entry: T): Boolean = getValueAt(getEntryRow(entry), 0) as Boolean

    /**
     * Sets the activity of the given entry.
     *
     * @param entry the entry to set the activity of
     * @param activity `true` iff the entry's checkbox should be checked
     */
    fun setEntryActivity(entry: T, activity: Boolean) = setValueAt(activity, getEntryRow(entry), 0)

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
        val columnWidthPercentages = floatArrayOf(CHECKBOX_WIDTH, TEXT_WIDTH)

        val columnModel = getColumnModel()
        (0 until model.columnCount).forEach { i ->
            columnModel.getColumn(i).preferredWidth = (columnWidthPercentages[i] * width).toInt()
        }
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
            1 -> java.lang.String::class.java
            else -> throw IllegalArgumentException("JEditableList has only two columns.")
        }

    /**
     * Returns `true` iff `column` is 0.
     *
     * @param row the row whose value is to be queried
     * @param column the columns whose value is to be queried
     * @return `true` iff `column` is 0
     */
    override fun isCellEditable(row: Int, column: Int) = column == 0
}


/**
 * A decorated version of a [JCheckBoxList], including buttons for adding, editing, and removing entries as desired.
 *
 * @param <T> the entry type
 * @param name the name of the inner [JCheckBoxList]
 * @param addAction the action to perform when the add button is clicked; `null` hides the button
 * @param editAction the action to perform on the currently-highlighted entry when the edit button is clicked; `null`
 * hides the button
 * @param removeAction the action to perform on the currently-highlighted entry when the remove button is clicked;
 * `null` hides the button
 */
class JEditableCheckBoxList<T>(
    innerName: String? = null,
    addAction: (() -> Unit)? = null,
    editAction: ((T) -> Unit)? = null,
    removeAction: ((T) -> Unit)? = null
) : JPanel(BorderLayout()) {
    /**
     * The inner list of entries that is to be decorated.
     */
    val list: JCheckBoxList<T> = JCheckBoxList(innerName)
    /**
     * The panel that contains the decorating buttons.
     */
    val actionsPanel: CommonActionsPanel


    init {
        val decorator = ToolbarDecorator.createDecorator(list)
        if (addAction != null)
            decorator.setAddAction { addAction() }
        if (editAction != null)
            decorator.setEditAction { list.highlightedEntry?.let { editAction(it) } }
        if (removeAction != null)
            decorator.setRemoveAction { list.highlightedEntry?.let { removeAction(it) } }

        val panel = decorator.createPanel()
        add(panel)

        actionsPanel = decorator.actionsPanel
    }
}
