package com.fwdekker.randomness.ui

import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.ArrayList
import java.util.NoSuchElementException
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.event.TableModelEvent
import javax.swing.table.DefaultTableModel


private typealias EntryActivityChangeListener = (Int) -> Any?


/**
 * A [javax.swing.JList] in which each entry has a [javax.swing.JCheckBox] in front of it.
 *
 * @param <T> the entry type
</T> */
class JEditableList<T> : JTable() {
    private val model: DefaultTableModel = DefaultTableModel(0, 2)
    private val entryActivityChangeListeners: MutableList<EntryActivityChangeListener> = ArrayList()


    /**
     * Constructs a new empty `JEditableList`.
     */
    init {
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
     * Returns a list of all entries.
     *
     * @return a list of all entries
     */
    val entries: List<T>
        get() = (0 until entryCount).map { this.getEntry(it) }.toList() // TODO Is this a copy?

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
     * Returns `true` iff. the given entry exists in the list.
     *
     * @param entry the entry to check for presence
     * @return `true` iff. the given entry exists in the list
     */
    private fun hasEntry(entry: T) = entries.any { row -> row == entry }

    /**
     * Returns the entry in the given row.
     *
     * @param row the row to return the entry of
     * @return the entry in the given row
     */
    fun getEntry(row: Int): T = model.getValueAt(row, 1) as T // Type guaranteed by design

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
     * @param entry    the entry to (un)check the checkbox of
     * @param selected `true` iff. the entry's checkbox should be checked
     */
    private fun setActive(entry: T, selected: Boolean) = setValueAt(selected, getEntryRow(entry), 0)

    /**
     * Returns `true` iff. the given entry's checkbox is checked.
     *
     * @param entry the entry of which to return the checkbox's status
     * @return `true` iff. the given entry's checkbox is checked
     */
    fun isActive(entry: T): Boolean = getValueAt(getEntryRow(entry), 0) as Boolean

    /**
     * Sets the activity of the given entry.
     *
     * @param entry    the entry to set the activity of
     * @param activity `true` iff. the entry's checkbox should be checked
     */
    fun setEntryActivity(entry: T, activity: Boolean) = setValueAt(activity, getEntryRow(entry), 0)

    /**
     * Checks the checkboxes of all given entries, and unchecks all other checkboxes.
     *
     * @param entries exactly those entries of which the checkboxes should be checked
     */
    fun setActiveEntries(entries: Collection<T>) =
        entries.forEach { entry -> setActive(entry, entries.contains(entry)) }

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
        val columnWidthPercentages = floatArrayOf(20.0f, 80.0f)

        val columnModel = getColumnModel()
        (0 until model.columnCount).forEach { i ->
            columnModel.getColumn(i).preferredWidth = (columnWidthPercentages[i] * width).toInt()
        }
    }

    override fun getColumnClass(column: Int) =
        when (column) {
            0 -> Boolean::class.java
            1 -> String::class.java
            else -> throw IllegalArgumentException("JEditableList only has two columns.")
        }

    override fun isCellEditable(row: Int, column: Int) = column == 0
}
