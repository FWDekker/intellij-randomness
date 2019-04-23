package com.fwdekker.randomness.ui;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link JEditableList}.
 */
final class JEditableListTest {
    private JEditableList<String> list;


    @BeforeEach
    void beforeEach() {
        list = new JEditableList<>();
    }


    @Test
    void testGetEntriesEmpty() {
        assertThat(list.getEntries())
            .isEmpty();
    }

    @Test
    void testAddEntry() {
        list.addEntry("sxTODMrLvE");

        assertThat(list.getEntries())
            .containsExactly("sxTODMrLvE");
    }

    @Test
    void testAddEntryDuplicate() {
        list.addEntry("6bqmI9JEkv");
        list.addEntry("6bqmI9JEkv");

        assertThat(list.getEntries())
            .containsExactly("6bqmI9JEkv");
    }

    @Test
    void testSetEntriesEmptyToEmpty() {
        list.setEntries(Collections.emptyList());

        assertThat(list.getEntries())
            .isEmpty();
    }

    @Test
    void testSetEntriesEmptyToNonEmpty() {
        list.setEntries(Arrays.asList("[qOBxjKQ6P", "DGw{4ag(99"));

        assertThat(list.getEntries())
            .containsExactly("[qOBxjKQ6P", "DGw{4ag(99");
    }

    @Test
    void testSetEntriesNonemptyToEmpty() {
        list.addEntry("u1}9]G<<CN");
        list.addEntry("SV9MUxo5yM");
        list.setEntries(Collections.emptyList());

        assertThat(list.getEntries())
            .isEmpty();
    }

    @Test
    void testSetEntriesNonemptyToNonempty() {
        list.addEntry("d>OO>rp2zJ");
        list.addEntry("Po(tiIXnxQ");
        list.addEntry("oq5BQYk0<7");
        list.setEntries(Arrays.asList("qKZK]8Y[vs", "cn3{[6y>de", "skE}h6H9MJ"));

        assertThat(list.getEntries())
            .containsExactly("qKZK]8Y[vs", "cn3{[6y>de", "skE}h6H9MJ");
    }

    @Test
    void testRemoveEntry() {
        list.addEntry("><t1wfkCLz");
        list.addEntry("X0hxkJGK)7");
        list.addEntry("TI5i9bUyLc");
        list.removeEntry("X0hxkJGK)7");

        assertThat(list.getEntries())
            .containsExactly("><t1wfkCLz", "TI5i9bUyLc");
    }

    @Test
    void testRemoveEntryDuplicate() {
        list.addEntry("UNIODenA]8");
        list.addEntry(">q(isDDjGx");
        list.addEntry("tyFoveRt5n");
        list.addEntry("tyFoveRt5n");
        list.removeEntry("tyFoveRt5n");

        assertThat(list.getEntries())
            .containsExactly("UNIODenA]8", ">q(isDDjGx");
    }

    @Test
    void testRemoveEntryNonExisting() {
        list.addEntry("qiGIG5Slmn");
        list.addEntry("uxlt{9}FeZ");
        list.addEntry("a4BEQoJpLJ");

        assertThatThrownBy(() -> list.removeEntry("6uy19G<}JS"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No row with entry `6uy19G<}JS` found.")
            .hasNoCause();
    }

    @Test
    void testClearEmpty() {
        list.clear();

        assertThat(list.getEntries())
            .isEmpty();
    }

    @Test
    void testClearNonempty() {
        list.addEntry("4lbw4K>}UH");
        list.addEntry("CMVe{rkBl[");
        list.clear();

        assertThat(list.getEntries())
            .isEmpty();
    }

    @Test
    void testGetEntryCountEmpty() {
        assertThat(list.getEntryCount())
            .isEqualTo(0);
    }

    @Test
    void testGetEntryCount() {
        list.addEntry("1JfHiG[CiD");
        list.addEntry("gXsoY21wzb");

        assertThat(list.getEntryCount())
            .isEqualTo(2);
    }

    @Test
    void testGetEntryCountDuplicate() {
        list.addEntry("jDwxzlHh]f");
        list.addEntry("A>)6q<f5kT");
        list.addEntry("A>)6q<f5kT");

        assertThat(list.getEntryCount())
            .isEqualTo(2);
    }


    @Test
    void testActiveEntriesEmpty() {
        assertThat(list.getActiveEntries())
            .isEmpty();
    }

    @Test
    void testActiveEntriesNone() {
        list.setEntries(Arrays.asList("DnqtROf4fd", "0<16CDsby2", "h0hu4XePhv"));

        assertThat(list.getActiveEntries())
            .isEmpty();
    }

    @Test
    void testActiveEntriesSome() {
        final List<String> entries = Arrays.asList("9}juZWluy}", "UGuD08qUXr", "eQA[]AdpYR");
        list.setEntries(entries);
        list.setActiveEntries(Arrays.asList("UGuD08qUXr"));

        assertThat(list.getActiveEntries())
            .containsExactly("UGuD08qUXr");
    }

    @Test
    void testActiveEntriesAll() {
        final List<String> entries = Arrays.asList("o28ix>b}x(", "6Zzl>yi5LB", "XyEVdjv1VM");
        list.setEntries(entries);
        list.setActiveEntries(entries);

        assertThat(list.getActiveEntries())
            .containsExactlyElementsOf(entries);
    }

    @Test
    @Disabled // TODO Re-enable this test. Should it throw an exception or not?
    void testSetActiveEntriesNonExistent() {
        final List<String> entries = Arrays.asList("JXIPoWsGR{", ">Jq7ILgv9]");
        list.setEntries(entries);
        list.setActiveEntries(Arrays.asList("JXIPoWsGR{", "j>NBYo}DnW", ">Jq7ILgv9]"));

        assertThat(list.getActiveEntries())
            .containsExactlyElementsOf(entries);
    }

    @Test
    void testIsActive() {
        final List<String> entries = Arrays.asList("I85kO5f8}6", "qcSv3u((zE", "{jjD)iFxEr");
        list.setEntries(entries);
        list.setEntryActivity("I85kO5f8}6", true);

        assertThat(list.isActive("I85kO5f8}6")).isTrue();
        assertThat(list.isActive("qcSv3u((zE")).isFalse();
        assertThat(list.isActive("{jjD)iFxEr")).isFalse();
    }

    @Test
    void testIsActiveNonExistent() {
        assertThatThrownBy(() -> list.isActive("Vr{1zIC9iH"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No row with entry `Vr{1zIC9iH` found.")
            .hasNoCause();
    }

    @Test
    void testActivityListenerOnUpdate() {
        final boolean[] fired = {false};
        list.addEntry("DsX[DtA>6{");

        list.addEntryActivityChangeListener(event -> {
            fired[0] = true;
            return Unit.INSTANCE;
        });
        list.setEntryActivity("DsX[DtA>6{", true);

        assertThat(fired[0]).isTrue();
    }

    @Test
    void testActivityListenerNoChangeOnInsert() {
        final boolean[] fired = {false};

        list.addEntryActivityChangeListener(event -> {
            fired[0] = true;
            return Unit.INSTANCE;
        });
        list.addEntry("DsX[DtA>6{");

        assertThat(fired[0]).isFalse();
    }

    @Test
    void testActivityListenerRemoveNoFire() {
        final boolean[] fired = {false};
        list.addEntry("bvDAPSZFG3");

        final Function1<Integer, Unit> changeListener = event -> {
            fired[0] = true;
            return Unit.INSTANCE;
        };
        list.addEntryActivityChangeListener(changeListener);
        list.removeEntryActivityChangeListener(changeListener);
        list.setEntryActivity("bvDAPSZFG3", true);

        assertThat(fired[0]).isFalse();
    }


    @Test
    void testGetHighlightedEntryNone() {
        assertThat(list.getHighlightedEntry()).isNull();
    }

    @Test
    void testGetHighlightedEntrySingle() {
        list.setEntries(Arrays.asList("Bb]CEbJlAD", "8QNk5l<]ln", "U5Hbo0whnn"));
        list.addRowSelectionInterval(0, 0);

        assertThat(list.getHighlightedEntry())
            .isEqualTo("Bb]CEbJlAD");
    }

    @Test
    void testGetHighlightedEntryMultiple() {
        list.setEntries(Arrays.asList("k<Goz2<IG9", "E4nRBR>wKG", "sCxbg}sfy("));
        list.addRowSelectionInterval(0, 0);
        list.addRowSelectionInterval(2, 2);

        assertThat(list.getHighlightedEntry())
            .isEqualTo("sCxbg}sfy(");
    }


    @Test
    void testGetColumnClasses() {
        assertThat(list.getColumnClass(0)).isEqualTo(Boolean.class);
        assertThat(list.getColumnClass(1)).isEqualTo(String.class);

        assertThatThrownBy(() -> list.getColumnClass(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("JEditableList has only two columns.")
            .hasNoCause();
        assertThatThrownBy(() -> list.getColumnClass(2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("JEditableList has only two columns.")
            .hasNoCause();
    }

    @Test
    void testIsCellEditable() {
        assertThat(list.isCellEditable(10, 5)).isFalse();
        assertThat(list.isCellEditable(7, 10)).isFalse();
        assertThat(list.isCellEditable(8, 2)).isFalse();
    }
}
