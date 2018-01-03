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


public final class JEditableTable<T> extends JTable {
    private final DefaultTableModel model;


    public JEditableTable() {
        super();

        model = new DefaultTableModel(0, 2);
        setModel(model);

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTableHeader(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                super.componentResized(e);
                resizeColumns();
            }
        });
    }


    public void addEntry(final T entry) {
        model.addRow(new Object[] {false, entry});
    }

    public void setEntries(final Collection<T> entries) {
        clear();
        entries.forEach(this::addEntry);
    }

    private T getEntry(final int row) {
        return (T) model.getValueAt(row, 1);
    }

    public List<T> getEntries() {
        return IntStream.range(0, model.getRowCount())
                .mapToObj(this::getEntry)
                .collect(Collectors.toList());
    }

    public void removeEntry(final T entry) {
        model.removeRow(getEntryRow(entry));
    }

    public void clear() {
        IntStream.range(0, getEntryCount()).forEach(row -> model.removeRow(row));
    }

    public int getEntryCount() {
        return model.getRowCount();
    }


    public List<T> getActiveEntries() {
        return getEntries().stream()
                .filter(this::isActive)
                .collect(Collectors.toList());
    }

    private void setActive(final T entry, final boolean selected) {
        setValueAt(selected, getEntryRow(entry), 0);
    }

    public boolean isActive(final T entry) {
        return (Boolean) getValueAt(getEntryRow(entry), 0);
    }

    public void setActiveEntries(final Collection<T> entries) {
        getEntries().stream()
                .forEach(entry -> setActive(entry, entries.contains(entry)));
    }


    public Optional<T> getHighlightedEntry() {
        return Arrays.stream(getSelectedRows())
                .mapToObj(this::getEntry)
                .findFirst();
    }


    private int getEntryRow(final T entry) {
        return IntStream.range(0, model.getRowCount())
                .filter(row -> getEntry(row).equals(entry))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No row with entry `" + entry + "` found."));
    }

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
                throw new IllegalArgumentException("JEditableTable only has two columns.");
        }
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return column == 0;
    }
}
