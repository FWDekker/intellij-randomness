package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.NoSuchElementException


/**
 * Unit tests for [JEditableList].
 */
class JEditableListTest {
    private lateinit var list: JEditableList<String>


    @BeforeEach
    fun beforeEach() {
        list = JEditableList()
    }


    @Test
    fun testGetEntriesEmpty() {
        assertThat(list.entries).isEmpty()
    }

    @Test
    fun testAddEntry() {
        list.addEntry("sxTODMrLvE")

        assertThat(list.entries).containsExactly("sxTODMrLvE")
    }

    @Test
    fun testAddEntryDuplicate() {
        list.addEntry("6bqmI9JEkv")
        list.addEntry("6bqmI9JEkv")

        assertThat(list.entries).containsExactly("6bqmI9JEkv")
    }

    @Test
    fun testSetEntriesEmptyToEmpty() {
        list.setEntries(emptyList())

        assertThat(list.entries).isEmpty()
    }

    @Test
    fun testSetEntriesEmptyToNonEmpty() {
        list.setEntries(listOf("[qOBxjKQ6P", "DGw{4ag(99"))

        assertThat(list.entries).containsExactly("[qOBxjKQ6P", "DGw{4ag(99")
    }

    @Test
    fun testSetEntriesNonemptyToEmpty() {
        list.addEntry("u1}9]G<<CN")
        list.addEntry("SV9MUxo5yM")
        list.setEntries(emptyList())

        assertThat(list.entries).isEmpty()
    }

    @Test
    fun testSetEntriesNonemptyToNonempty() {
        list.addEntry("d>OO>rp2zJ")
        list.addEntry("Po(tiIXnxQ")
        list.addEntry("oq5BQYk0<7")
        list.setEntries(listOf("qKZK]8Y[vs", "cn3{[6y>de", "skE}h6H9MJ"))

        assertThat(list.entries).containsExactly("qKZK]8Y[vs", "cn3{[6y>de", "skE}h6H9MJ")
    }

    @Test
    fun testRemoveEntry() {
        list.addEntry("><t1wfkCLz")
        list.addEntry("X0hxkJGK)7")
        list.addEntry("TI5i9bUyLc")
        list.removeEntry("X0hxkJGK)7")

        assertThat(list.entries).containsExactly("><t1wfkCLz", "TI5i9bUyLc")
    }

    @Test
    fun testRemoveEntryDuplicate() {
        list.addEntry("UNIODenA]8")
        list.addEntry(">q(isDDjGx")
        list.addEntry("tyFoveRt5n")
        list.addEntry("tyFoveRt5n")
        list.removeEntry("tyFoveRt5n")

        assertThat(list.entries).containsExactly("UNIODenA]8", ">q(isDDjGx")
    }

    @Test
    fun testRemoveEntryNonExisting() {
        list.addEntry("qiGIG5Slmn")
        list.addEntry("uxlt{9}FeZ")
        list.addEntry("a4BEQoJpLJ")

        assertThatThrownBy { list.removeEntry("6uy19G<}JS") }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("No row with entry `6uy19G<}JS` found.")
            .hasNoCause()
    }

    @Test
    fun testClearEmpty() {
        list.clear()

        assertThat(list.entries).isEmpty()
    }

    @Test
    fun testClearNonempty() {
        list.addEntry("4lbw4K>}UH")
        list.addEntry("CMVe{rkBl[")
        list.clear()

        assertThat(list.entries).isEmpty()
    }

    @Test
    fun testGetEntryCountEmpty() {
        assertThat(list.entryCount).isEqualTo(0)
    }

    @Test
    fun testGetEntryCount() {
        list.addEntry("1JfHiG[CiD")
        list.addEntry("gXsoY21wzb")

        assertThat(list.entryCount).isEqualTo(2)
    }

    @Test
    fun testGetEntryCountDuplicate() {
        list.addEntry("jDwxzlHh]f")
        list.addEntry("A>)6q<f5kT")
        list.addEntry("A>)6q<f5kT")

        assertThat(list.entryCount).isEqualTo(2)
    }


    @Test
    fun testActiveEntriesEmpty() {
        assertThat(list.activeEntries).isEmpty()
    }

    @Test
    fun testActiveEntriesNone() {
        list.setEntries(listOf("DnqtROf4fd", "0<16CDsby2", "h0hu4XePhv"))

        assertThat(list.activeEntries).isEmpty()
    }

    @Test
    fun testActiveEntriesSome() {
        val entries = listOf("9}juZWluy}", "UGuD08qUXr", "eQA[]AdpYR")
        list.setEntries(entries)
        list.setActiveEntries(listOf("UGuD08qUXr"))

        assertThat(list.activeEntries).containsExactly("UGuD08qUXr")
    }

    @Test
    fun testActiveEntriesAll() {
        val entries = listOf("o28ix>b}x(", "6Zzl>yi5LB", "XyEVdjv1VM")
        list.setEntries(entries)
        list.setActiveEntries(entries)

        assertThat(list.activeEntries).containsExactlyElementsOf(entries)
    }

    @Test
    fun testSetActiveEntriesNonExistent() {
        val entries = listOf("JXIPoWsGR{", ">Jq7ILgv9]")
        list.setEntries(entries)
        list.setActiveEntries(listOf("JXIPoWsGR{", "j>NBYo}DnW", ">Jq7ILgv9]"))

        assertThat(list.activeEntries).containsExactlyElementsOf(entries)
    }

    @Test
    fun testIsActive() {
        val entries = listOf("I85kO5f8}6", "qcSv3u((zE", "{jjD)iFxEr")
        list.setEntries(entries)
        list.setEntryActivity("I85kO5f8}6", true)

        assertThat(list.isActive("I85kO5f8}6")).isTrue()
        assertThat(list.isActive("qcSv3u((zE")).isFalse()
        assertThat(list.isActive("{jjD)iFxEr")).isFalse()
    }

    @Test
    fun testIsActiveNonExistent() {
        assertThatThrownBy { list.isActive("Vr{1zIC9iH") }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("No row with entry `Vr{1zIC9iH` found.")
            .hasNoCause()
    }

    @Test
    fun testActivityListenerOnUpdate() {
        val fired = booleanArrayOf(false)
        list.addEntry("DsX[DtA>6{")

        list.addEntryActivityChangeListener { fired[0] = true }
        list.setEntryActivity("DsX[DtA>6{", true)

        assertThat(fired[0]).isTrue()
    }

    @Test
    fun testActivityListenerNoChangeOnInsert() {
        val fired = booleanArrayOf(false)

        list.addEntryActivityChangeListener { fired[0] = true }
        list.addEntry("DsX[DtA>6{")

        assertThat(fired[0]).isFalse()
    }

    @Test
    fun testActivityListenerRemoveNoFire() {
        val fired = booleanArrayOf(false)
        list.addEntry("bvDAPSZFG3")

        val listener = { _: Int -> fired[0] = true }
        list.addEntryActivityChangeListener(listener)
        list.removeEntryActivityChangeListener(listener)
        list.setEntryActivity("bvDAPSZFG3", true)

        assertThat(fired[0]).isFalse()
    }


    @Test
    fun testGetHighlightedEntryNone() {
        assertThat(list.highlightedEntry).isNull()
    }

    @Test
    fun testGetHighlightedEntrySingle() {
        list.setEntries(listOf("Bb]CEbJlAD", "8QNk5l<]ln", "U5Hbo0whnn"))
        list.addRowSelectionInterval(0, 0)

        assertThat(list.highlightedEntry).isEqualTo("Bb]CEbJlAD")
    }

    @Test
    fun testGetHighlightedEntryMultiple() {
        list.setEntries(listOf("k<Goz2<IG9", "E4nRBR>wKG", "sCxbg}sfy("))
        list.addRowSelectionInterval(0, 0)
        list.addRowSelectionInterval(2, 2)

        assertThat(list.highlightedEntry).isEqualTo("sCxbg}sfy(")
    }


    @Test
    fun testGetColumnClasses() {
        assertThat(list.getColumnClass(0)).isEqualTo(java.lang.Boolean::class.java)
        assertThat(list.getColumnClass(1)).isEqualTo(java.lang.String::class.java)

        assertThatThrownBy { list.getColumnClass(-1) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("JEditableList has only two columns.")
            .hasNoCause()
        assertThatThrownBy { list.getColumnClass(2) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("JEditableList has only two columns.")
            .hasNoCause()
    }

    @Test
    fun testIsCellEditable() {
        assertThat(list.isCellEditable(10, 5)).isFalse()
        assertThat(list.isCellEditable(7, 10)).isFalse()
        assertThat(list.isCellEditable(8, 2)).isFalse()
    }
}
