package com.fwdekker.randomness.ui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;


/**
 * A {@link javax.swing.JList} in which each entry has a {@link javax.swing.JCheckBox} in front of it.
 *
 * @param <T> the entry type
 */
public final class JEditableList<T> extends JTable {
    private final DefaultTableModel model;


    /**
     * Constructs a new empty {@code JEditableList}.
     */
    public JEditableList() {
        super();

        model = new DefaultTableModel(0, 2);
        setModel(model);

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTableHeader(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent event) {
                super.componentResized(event);
                resizeColumns();
            }
        });
    }


    /**
     * Adds an entry to the list.
     *
     * @param entry the entry to add
     */
    public void addEntry(final T entry) {
        model.addRow(new Object[] {false, entry});
    }

    /**
     * Removes all current entries, and adds the given entries.
     *
     * @param entries the entries to add
     */
    public void setEntries(final Collection<T> entries) {
        clear();
        entries.forEach(this::addEntry);
    }

    /**
     * Returns the entry in the given row.
     *
     * @param row the row to return the entry of
     * @return the entry in the given row
     */
    private T getEntry(final int row) {
        return (T) model.getValueAt(row, 1);
    }

    /**
     * Returns a list of all entries.
     *
     * @return a list of all entries
     */
    public List<T> getEntries() {
        return IntStream.range(0, model.getRowCount())
                .mapToObj(this::getEntry)
                .collect(Collectors.toList());
    }

    /**
     * Removes the given entry and its checkbox.
     *
     * @param entry the entry to remove
     */
    public void removeEntry(final T entry) {
        model.removeRow(getEntryRow(entry));
    }

    /**
     * Removes all entries.
     */
    public void clear() {
        IntStream.range(0, getEntryCount()).forEach(model::removeRow);
    }

    /**
     * Returns the number of entries in the list.
     *
     * @return the number of entries in the list
     */
    public int getEntryCount() {
        return model.getRowCount();
    }


    /**
     * Returns all entries of which the checkbox is checked.
     *
     * @return all entries of which the checkbox is checked
     */
    public List<T> getActiveEntries() {
        return getEntries().stream()
                .filter(this::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Sets whether the given entry has its checkbox checked.
     *
     * @param entry    the entry to (un)check the checkbox of
     * @param selected {@code true} iff. the entry's checkbox should be checked
     */
    private void setActive(final T entry, final boolean selected) {
        setValueAt(selected, getEntryRow(entry), 0);
    }

    /**
     * Returns {@code true} iff. the given entry's checkbox is checked.
     *
     * @param entry the entry of which to return the checkbox's status
     * @return {@code true} iff. the given entry's checkbox is checked
     */
    public boolean isActive(final T entry) {
        return (Boolean) getValueAt(getEntryRow(entry), 0);
    }

    /**
     * Checks the checkboxes of all given entries, and unchecks all other checkboxes.
     *
     * @param entries exactly those entries of which the checkboxes should be checked
     */
    public void setActiveEntries(final Collection<T> entries) {
        getEntries().stream().forEach(entry -> setActive(entry, entries.contains(entry)));
    }


    /**
     * Returns the entry that is currently selected by the user, if there is one.
     *
     * @return the entry that is currently selected by the user, if there is one
     */
    public Optional<T> getHighlightedEntry() {
        return Arrays.stream(getSelectedRows())
                .mapToObj(this::getEntry)
                .findFirst();
    }


    /**
     * Returns the row number of the given entry.
     *
     * @param entry the entry to return the row number of
     * @return the row number of the given entry
     */
    private int getEntryRow(final T entry) {
        return IntStream.range(0, model.getRowCount())
                .filter(row -> getEntry(row).equals(entry))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No row with entry `" + entry + "` found."));
    }

    /**
     * Recalculates column widths.
     */
    private void resizeColumns() {
        final float[] columnWidthPercentages = {20.0f, 80.0f};

        final TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth((int) (columnWidthPercentages[i] * getWidth()));
        }
    }


    @Override
    public Class<?> getColumnClass(final int column) {
        switch (column) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            default:
                throw new IllegalArgumentException("JEditableList only has two columns.");
        }
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return column == 0;
    }
}
