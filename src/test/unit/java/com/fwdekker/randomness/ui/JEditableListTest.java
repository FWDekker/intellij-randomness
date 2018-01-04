package com.fwdekker.randomness.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link JEditableList}.
 */
public final class JEditableListTest {
    private JEditableList<String> list;


    @Before
    public void beforeEach() {
        list = new JEditableList<>();
    }


    @Test
    public void testGetEntriesEmpty() {
        assertThat(list.getEntries())
                .isEmpty();
    }

    @Test
    public void testAddEntry() {
        list.addEntry("sxTODMrLvE");

        assertThat(list.getEntries())
                .containsExactly("sxTODMrLvE");
    }

    @Test
    public void testAddEntryDuplicate() {
        list.addEntry("6bqmI9JEkv");
        list.addEntry("6bqmI9JEkv");

        assertThat(list.getEntries())
                .containsExactly("6bqmI9JEkv");
    }

    @Test
    public void testSetEntriesEmptyToEmpty() {
        list.setEntries(Collections.emptyList());

        assertThat(list.getEntries())
                .isEmpty();
    }

    @Test
    public void testSetEntriesEmptyToNonEmpty() {
        list.setEntries(Arrays.asList("[qOBxjKQ6P", "DGw{4ag(99"));

        assertThat(list.getEntries())
                .containsExactly("[qOBxjKQ6P", "DGw{4ag(99");
    }

    @Test
    public void testSetEntriesNonemptyToEmpty() {
        list.addEntry("u1}9]G<<CN");
        list.addEntry("SV9MUxo5yM");
        list.setEntries(Collections.emptyList());

        assertThat(list.getEntries())
                .isEmpty();
    }

    @Test
    public void testSetEntriesNonemptyToNonempty() {
        list.addEntry("d>OO>rp2zJ");
        list.addEntry("Po(tiIXnxQ");
        list.addEntry("oq5BQYk0<7");
        list.setEntries(Arrays.asList("qKZK]8Y[vs", "cn3{[6y>de", "skE}h6H9MJ"));

        assertThat(list.getEntries())
                .containsExactly("qKZK]8Y[vs", "cn3{[6y>de", "skE}h6H9MJ");
    }

    @Test
    public void testRemoveEntry() {
        list.addEntry("><t1wfkCLz");
        list.addEntry("X0hxkJGK)7");
        list.addEntry("TI5i9bUyLc");
        list.removeEntry("X0hxkJGK)7");

        assertThat(list.getEntries())
                .containsExactly("><t1wfkCLz", "TI5i9bUyLc");
    }

    @Test
    public void testRemoveEntryDuplicate() {
        list.addEntry("UNIODenA]8");
        list.addEntry(">q(isDDjGx");
        list.addEntry("tyFoveRt5n");
        list.addEntry("tyFoveRt5n");
        list.removeEntry("tyFoveRt5n");

        assertThat(list.getEntries())
                .containsExactly("UNIODenA]8", ">q(isDDjGx");
    }

    @Test
    public void testRemoveEntryNonExisting() {
        list.addEntry("qiGIG5Slmn");
        list.addEntry("uxlt{9}FeZ");
        list.addEntry("a4BEQoJpLJ");

        assertThatThrownBy(() -> list.removeEntry("6uy19G<}JS"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No row with entry `6uy19G<}JS` found.")
                .hasNoCause();
    }

    @Test
    public void testClearEmpty() {
        list.clear();

        assertThat(list.getEntries())
                .isEmpty();
    }

    @Test
    public void testClearNonempty() {
        list.addEntry("4lbw4K>}UH");
        list.addEntry("CMVe{rkBl[");
        list.clear();

        assertThat(list.getEntries())
                .isEmpty();
    }

    @Test
    public void testGetEntryCountEmpty() {
        assertThat(list.getEntryCount())
                .isEqualTo(0);
    }

    @Test
    public void testGetEntryCount() {
        list.addEntry("1JfHiG[CiD");
        list.addEntry("gXsoY21wzb");

        assertThat(list.getEntryCount())
                .isEqualTo(2);
    }

    @Test
    public void testGetEntryCountDuplicate() {
        list.addEntry("jDwxzlHh]f");
        list.addEntry("A>)6q<f5kT");
        list.addEntry("A>)6q<f5kT");

        assertThat(list.getEntryCount())
                .isEqualTo(2);
    }


    @Test
    public void testGetColumnClasses() {
        assertThat(list.getColumnClass(0)).isEqualTo(Boolean.class);
        assertThat(list.getColumnClass(1)).isEqualTo(String.class);

        assertThatThrownBy(() -> list.getColumnClass(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JEditableList only has two columns.")
                .hasNoCause();
        assertThatThrownBy(() -> list.getColumnClass(2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JEditableList only has two columns.")
                .hasNoCause();
    }

    @Test
    public void testIsCellEditable() {
        assertThat(list.isCellEditable(10, 5)).isFalse();
        assertThat(list.isCellEditable(7, 10)).isFalse();
        assertThat(list.isCellEditable(8, 2)).isFalse();
    }
}
