package com.fwdekker.randomness.ui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;


public final class JEditableTable extends JTable {
    private final DefaultTableModel model;


    public JEditableTable() {
        super();

        model = new DefaultTableModel(0, 2);

        setModel(model);
        setTableHeader(null);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                super.componentResized(e);
                resizeColumns();
            }
        });
    }


    public void addEntry(final String entry) {
        model.addRow(new Object[] {false, entry});
    }

    public void setEntries(final Collection<String> entries) {
        clear();
        entries.forEach(this::addEntry);
    }

    private String getEntry(final int row) {
        return model.getValueAt(row, 1).toString();
    }

    public List<String> getEntries() {
        return IntStream.range(0, model.getRowCount())
                .mapToObj(this::getEntry)
                .collect(Collectors.toList());
    }

    public void removeEntry(final String entry) {
        model.removeRow(getEntryRow(entry));
    }

    public void clear() {
        IntStream.range(0, getEntryCount()).forEach(row -> model.removeRow(row));
    }

    public int getEntryCount() {
        return model.getRowCount();
    }


    public List<String> getActiveEntries() {
        return getEntries().stream()
                .filter(this::isActive)
                .collect(Collectors.toList());
    }

    private void setActive(final String entry, final boolean selected) {
        setValueAt(selected, getEntryRow(entry), 0);
    }

    public boolean isActive(final String entry) {
        return (Boolean) getValueAt(getEntryRow(entry), 0);
    }

    public void setActiveEntries(final Collection<String> entries) {
        getEntries().stream()
                .forEach(entry -> setActive(entry, entries.contains(entry)));
    }


    public List<String> getHighlightedEntries() {
        return Arrays.stream(getSelectedRows())
                .mapToObj(this::getEntry)
                .collect(Collectors.toList());
    }


    private int getEntryRow(final String entry) {
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
}
