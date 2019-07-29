package com.fwdekker.randomness.ui

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.CommonActionsPanel
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.NoSuchElementException


/**
 * Unit tests for [JCheckBoxList].
 */
object JEditableListTest : Spek({
    lateinit var list: JCheckBoxList<String>


    beforeEachTest {
        list = GuiActionRunner.execute<JCheckBoxList<String>> { JCheckBoxList() }
    }


    describe("getEntries") {
        it("returns nothing if no entries are added") {
            assertThat(list.entries).isEmpty()
        }
    }

    describe("addEntry") {
        it("adds a given entry") {
            GuiActionRunner.execute { list.addEntry("manscape") }

            assertThat(list.entries).containsExactly("manscape")
        }

        it("adds two given entries") {
            GuiActionRunner.execute {
                list.addEntry("osteomas")
                list.addEntry("imido")
            }

            assertThat(list.entries).containsExactly("osteomas", "imido")
        }

        it("does not add a given entry twice") {
            GuiActionRunner.execute {
                list.addEntry("bloomed")
                list.addEntry("bloomed")
            }

            assertThat(list.entries).containsExactly("bloomed")
        }
    }

    describe("setEntries") {
        it("empties an empty list") {
            GuiActionRunner.execute { list.setEntries(emptyList()) }

            assertThat(list.entries).isEmpty()
        }

        it("adds two entries to an empty list") {
            GuiActionRunner.execute { list.setEntries(listOf("gladding", "flecky")) }

            assertThat(list.entries).containsExactly("gladding", "flecky")
        }

        it("does not add duplicate entries to a list") {
            GuiActionRunner.execute { list.setEntries(listOf("orrery", "orrery")) }

            assertThat(list.entries).containsExactly("orrery")
        }

        it("empties a non-empty list") {
            GuiActionRunner.execute {
                list.addEntry("prefeast")
                list.addEntry("strip")
            }

            GuiActionRunner.execute { list.setEntries(emptyList()) }

            assertThat(list.entries).isEmpty()
        }

        it("replaces the entries in a non-empty list") {
            GuiActionRunner.execute {
                list.addEntry("pirates")
                list.addEntry("underbit")
                list.addEntry("flexured")
            }

            GuiActionRunner.execute { list.setEntries(listOf("steevely", "qual", "wheaties")) }

            assertThat(list.entries).containsExactly("steevely", "qual", "wheaties")
        }
    }

    describe("removeEntry") {
        it("removes the given entry") {
            GuiActionRunner.execute {
                list.addEntry("giftwrap")
                list.addEntry("backbite")
                list.addEntry("landwhin")
            }

            GuiActionRunner.execute { list.removeEntry("backbite") }

            assertThat(list.entries).containsExactly("giftwrap", "landwhin")
        }

        it("throws an exception if the element to be removed cannot be found") {
            GuiActionRunner.execute {
                list.addEntry("tracheid")
                list.addEntry("cocomat")
                list.addEntry("videotex")
            }

            assertThatThrownBy { list.removeEntry("unshowed") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("No row with entry `unshowed` found.")
                .hasNoCause()
        }
    }

    describe("clear") {
        it("clears an empty list") {
            GuiActionRunner.execute { list.clear() }

            assertThat(list.entries).isEmpty()
        }

        it("clears a non-empty list") {
            GuiActionRunner.execute {
                list.addEntry("lathered")
                list.addEntry("aquench")
            }

            GuiActionRunner.execute { list.clear() }

            assertThat(list.entries).isEmpty()
        }
    }

    describe("entryCount") {
        it("counts 0 elements for an empty list") {
            assertThat(list.entryCount).isEqualTo(0)
        }

        it("counts 2 elements for a list with 2 elements") {
            GuiActionRunner.execute {
                list.addEntry("musmon")
                list.addEntry("topepo")
            }

            assertThat(list.entryCount).isEqualTo(2)
        }
    }

    describe("activeEntries") {
        it("has no active entries in an empty list") {
            assertThat(list.activeEntries).isEmpty()
        }

        it("has no active entries by default") {
            GuiActionRunner.execute { list.setEntries(listOf("otxi", "chamorro", "scarab")) }

            assertThat(list.activeEntries).isEmpty()
        }

        it("can enable one entry") {
            GuiActionRunner.execute { list.setEntries(listOf("disbury", "curet", "sunhat")) }

            GuiActionRunner.execute { list.setActiveEntries(listOf("curet")) }

            assertThat(list.activeEntries).containsExactly("curet")
        }

        it("can enable all entries") {
            val entries = listOf("big", "coumaric", "hooray")
            GuiActionRunner.execute { list.setEntries(entries) }

            GuiActionRunner.execute { list.setActiveEntries(entries) }

            assertThat(list.activeEntries).containsExactlyElementsOf(entries)
        }

        it("ignores when a non-existing element is enabled") {
            val entries = listOf("forte", "blarneys")
            GuiActionRunner.execute { list.setEntries(entries) }

            GuiActionRunner.execute { list.setActiveEntries(listOf("forte", "pittings")) }

            assertThat(list.activeEntries).containsExactly("forte")
        }

        it("unchecks previously checked entries") {
            val entries = listOf("rot", "possible", "harbor")
            GuiActionRunner.execute { list.setEntries(entries) }
            GuiActionRunner.execute { list.setActiveEntries(entries) }

            GuiActionRunner.execute { list.setActiveEntries(listOf("rot", "harbor")) }

            assertThat(list.activeEntries).containsExactly("rot", "harbor")
        }
    }

    describe("isActive") {
        it("returns true iff an element is active") {
            GuiActionRunner.execute { list.setEntries(listOf("overtoil", "gouramis", "abruptly")) }

            GuiActionRunner.execute { list.setEntryActivity("overtoil", true) }

            assertThat(list.isActive("overtoil")).isTrue()
            assertThat(list.isActive("gouramis")).isFalse()
            assertThat(list.isActive("abruptly")).isFalse()
        }

        it("throws an exception if an element is not in the list") {
            assertThatThrownBy { list.isActive("nanism") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("No row with entry `nanism` found.")
                .hasNoCause()
        }
    }

    describe("activityListener") {
        it("fires when an entry's activity is changed") {
            var fired = false
            GuiActionRunner.execute { list.addEntry("scarpa") }
            list.addEntryActivityChangeListener { fired = true }

            GuiActionRunner.execute { list.setEntryActivity("scarpa", true) }

            assertThat(fired).isTrue()
        }

        it("does not fire when an entry is inserted") {
            var fired = false
            list.addEntryActivityChangeListener { fired = true }

            GuiActionRunner.execute { list.addEntry("techiest") }

            assertThat(fired).isFalse()
        }

        it("stops firing after a listener is removed") {
            var fired = false
            val listener = { _: Int -> fired = true }
            GuiActionRunner.execute { list.addEntry("optimum") }
            list.addEntryActivityChangeListener(listener)

            list.removeEntryActivityChangeListener(listener)
            GuiActionRunner.execute { list.setEntryActivity("optimum", true) }

            assertThat(fired).isFalse()
        }
    }

    describe("getHighlightedEntry") {
        it("returns null when no entry is highlighted") {
            assertThat(list.highlightedEntry).isNull()
        }

        it("returns the single highlighted entry") {
            GuiActionRunner.execute {
                list.setEntries(listOf("bahay", "woodyard", "hussies"))
                list.addRowSelectionInterval(0, 0)
            }

            assertThat(list.highlightedEntry).isEqualTo("bahay")
        }

        it("returns the first highlighted entry") {
            GuiActionRunner.execute {
                list.setEntries(listOf("shutoffs", "dentine", "scutel"))
                list.addRowSelectionInterval(0, 0)
                list.addRowSelectionInterval(2, 2)
            }

            assertThat(list.highlightedEntry).isEqualTo("scutel")
        }
    }

    describe("getColumnClass") {
        it("returns the right types for the columns") {
            // Java classes MUST be used
            assertThat(list.getColumnClass(0)).isEqualTo(java.lang.Boolean::class.java)
            assertThat(list.getColumnClass(1)).isEqualTo(java.lang.String::class.java)
        }

        it("throws an exception if a negative column index is requested") {
            assertThatThrownBy { list.getColumnClass(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("JEditableList has only two columns.")
                .hasNoCause()
        }

        it("throws an exception if column index that is too high is requested") {
            assertThatThrownBy { list.getColumnClass(2) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("JEditableList has only two columns.")
                .hasNoCause()
        }
    }

    describe("isCellEditable") {
        it("returns true iff the column is 0") {
            assertThat(list.isCellEditable(13, 0)).isTrue()
            assertThat(list.isCellEditable(67, 0)).isTrue()

            assertThat(list.isCellEditable(10, 5)).isFalse()
            assertThat(list.isCellEditable(7, 10)).isFalse()
            assertThat(list.isCellEditable(8, 2)).isFalse()
        }
    }
})


/**
 * Unit tests for [JEditableCheckBoxList].
 */
object JEditableCheckBoxListTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    fun createList(query: () -> JEditableCheckBoxList<Any>) = GuiActionRunner.execute(query)


    describe("appearance") {
        it("assigns the given name to the inner list") {
            val list = createList { JEditableCheckBoxList("thirst") }

            assertThat(list.list.name).isEqualTo("thirst")
        }

        it("does not add any buttons by default") {
            val list = createList { JEditableCheckBoxList() }

            assertThat(list.actionsPanel.actionMap.size()).isZero()
        }
    }

    describe("executes the desired actions") {
        fun <T> JEditableCheckBoxList<T>.pressButton(button: CommonActionsPanel.Buttons) =
            this.getButton(button)!!.actionPerformed(mock {})


        it("executes the add function if the add button is clicked") {
            var isCalled = false
            val list = createList { JEditableCheckBoxList(addAction = { isCalled = true }) }

            GuiActionRunner.execute { list.pressButton(CommonActionsPanel.Buttons.ADD) }

            assertThat(isCalled).isTrue()
        }

        it("executes the edit function if the edit button is clicked") {
            var editedEntry: Any? = null
            val list = createList { JEditableCheckBoxList(editAction = { editedEntry = it }) }
            GuiActionRunner.execute { list.list.addEntry("collar") }

            GuiActionRunner.execute {
                list.list.addRowSelectionInterval(0, 0)
                list.pressButton(CommonActionsPanel.Buttons.EDIT)
            }

            assertThat(editedEntry).isEqualTo("collar")
        }

        it("does nothing if the edit button is clicked but no entry is highlighted") {
            var editedEntry: Any? = null
            val list = createList { JEditableCheckBoxList(editAction = { editedEntry = "arrive" }) }
            GuiActionRunner.execute { list.list.addEntry("water") }

            GuiActionRunner.execute {
                list.list.clearSelection()
                list.pressButton(CommonActionsPanel.Buttons.EDIT)
            }

            assertThat(editedEntry).isNull()
        }

        it("executes the remove function if the remove button is clicked") {
            var removedEntry: Any? = null
            val list = createList { JEditableCheckBoxList(removeAction = { removedEntry = it }) }
            GuiActionRunner.execute { list.list.addEntry("dip") }

            GuiActionRunner.execute {
                list.list.addRowSelectionInterval(0, 0)
                list.pressButton(CommonActionsPanel.Buttons.REMOVE)
            }

            assertThat(removedEntry).isEqualTo("dip")
        }

        it("does nothing if the remove button is clicked but no entry is highlighted") {
            var removedEntry: Any? = null
            val list = createList { JEditableCheckBoxList(removeAction = { removedEntry = it }) }
            GuiActionRunner.execute { list.list.addEntry("tax") }

            GuiActionRunner.execute {
                list.list.clearSelection()
                list.pressButton(CommonActionsPanel.Buttons.REMOVE)
            }

            assertThat(removedEntry).isNull()
        }
    }

    describe("getting buttons") {
        it("returns null if the button was not added") {
            val list = createList { JEditableCheckBoxList(editAction = {}) }

            assertThat(list.getButton(CommonActionsPanel.Buttons.ADD)).isNull()
        }

        it("returns the appropriate button") {
            val list = createList { JEditableCheckBoxList(addAction = {}, removeAction = {}) }

            assertThat(list.getButton(CommonActionsPanel.Buttons.ADD)).isNotNull()
        }
    }
})
