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
 * Unit tests for [JCheckBoxTable].
 */
object JCheckBoxTableTest : Spek({
    val defaultColumns = listOf(JCheckBoxTable.Column(), JCheckBoxTable.Column("name"))

    lateinit var table: JCheckBoxTable<String>


    beforeEachTest {
        table = GuiActionRunner.execute<JCheckBoxTable<String>> {
            JCheckBoxTable(defaultColumns, { it.joinToString(",") }, { it.split(",") })
        }
    }


    describe("entryCount") {
        it("returns 0 for an empty list") {
            assertThat(table.entryCount).isEqualTo(0)
        }

        it("returns 2 for a list with 2 elements") {
            GuiActionRunner.execute { table.entries = listOf("mercy,supply", "wrong,light") }

            assertThat(table.entryCount).isEqualTo(2)
        }
    }

    describe("getEntries") {
        it("returns nothing if no entries are added") {
            assertThat(table.entries).isEmpty()
        }
    }

    describe("addEntry") {
        it("adds a given entry") {
            GuiActionRunner.execute { table.addEntry("bank,bicycle") }

            assertThat(table.entries).containsExactly("bank,bicycle")
        }

        it("sets the activity of the new entry as desired") {
            GuiActionRunner.execute {
                table.addEntry("meantime,treasury", true)
                table.addEntry("praise,edge", false)
            }

            assertThat(table.isActive("meantime,treasury")).isTrue()
            assertThat(table.isActive("praise,edge")).isFalse()
        }

        it("splits the entry into separate columns") {
            GuiActionRunner.execute { table.addEntry("memory,bicycle") }

            assertThat(table.getValueAt(0, 1)).isEqualTo("memory")
            assertThat(table.getValueAt(0, 2)).isEqualTo("bicycle")
        }

        it("adds two given entries") {
            GuiActionRunner.execute {
                table.addEntry("discover,quart")
                table.addEntry("prison,basic")
            }

            assertThat(table.entries).containsExactly("discover,quart", "prison,basic")
        }

        it("does not add a given entry twice") {
            GuiActionRunner.execute { table.addEntry("bloomed,dull") }

            assertThatThrownBy { GuiActionRunner.execute { table.addEntry("bloomed,dull") } }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot add duplicate entry.")
        }
    }

    describe("setEntry") {
        it("updates a given entry") {
            GuiActionRunner.execute { table.entries = listOf("although,stocking", "governor,pay") }

            GuiActionRunner.execute { table.setEntry(1, "violent,failure") }

            assertThat(table.entries).containsExactly("although,stocking", "violent,failure")
        }

        it("updates a given entry to itself") {
            GuiActionRunner.execute { table.entries = listOf("bribe,straight", "health,stuff") }

            GuiActionRunner.execute { table.setEntry(1, "health,stuff") }

            assertThat(table.entries).containsExactly("bribe,straight", "health,stuff")
        }

        it("does not set negative out-of-bounds indices") {
            assertThatThrownBy { GuiActionRunner.execute { table.setEntry(-4, "splendid,lift") } }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. rows=0, index=-4.")
        }

        it("does not set positive out-of-bounds indices") {
            GuiActionRunner.execute { GuiActionRunner.execute { table.addEntry("misery,laugh") } }

            assertThatThrownBy { table.setEntry(1, "hat,bowl") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. rows=1, index=1.")
        }

        it("does not cause duplicate entries") {
            GuiActionRunner.execute { GuiActionRunner.execute { table.entries = listOf("whip,actor", "wine,advise") } }

            assertThatThrownBy { GuiActionRunner.execute { table.setEntry(1, "whip,actor") } }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot add duplicate entry.")
        }

        it("does not change activity if null is passed") {
            GuiActionRunner.execute {
                table.entries = listOf("stretch,ready", "egg,return")
                table.activeEntries = listOf("stretch,ready")
            }

            GuiActionRunner.execute { GuiActionRunner.execute { table.setEntry(0, "swear,loyal", null) } }

            assertThat(table.isActive("swear,loyal")).isTrue()
        }

        it("changes the entry's activity as desired") {
            GuiActionRunner.execute {
                table.entries = listOf("elastic,name", "shake,gradual")
                table.activeEntries = listOf("shake,gradual")
            }

            GuiActionRunner.execute {
                table.setEntry(0, "property,barber", false)
                table.setEntry(1, "weekday,farm", true)
            }

            assertThat(table.isActive("property,barber")).isFalse()
            assertThat(table.isActive("weekday,farm")).isTrue()
        }
    }

    describe("setEntries") {
        it("empties an empty list") {
            GuiActionRunner.execute { table.entries = emptyList() }

            assertThat(table.entries).isEmpty()
        }

        it("adds two entries to an empty list") {
            GuiActionRunner.execute { table.entries = listOf("ripen,which", "luck,arch") }

            assertThat(table.entries).containsExactly("ripen,which", "luck,arch")
        }

        it("does not add duplicate entries to a list") {
            assertThatThrownBy { GuiActionRunner.execute { table.entries = listOf("soap,northern", "soap,northern") } }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot add duplicate entry.")
        }

        it("successfully adds an entry that was in the list before") {
            GuiActionRunner.execute {
                table.addEntry("border,cake")
                table.addEntry("protect,shore")
            }

            GuiActionRunner.execute { table.entries = listOf("border,cake") }

            assertThat(table.entries).containsExactly("border,cake")
        }

        it("empties a non-empty list") {
            GuiActionRunner.execute { table.entries = listOf("prefeast,sock", "strip,hurt") }

            GuiActionRunner.execute { table.entries = emptyList() }

            assertThat(table.entries).isEmpty()
        }

        it("replaces the entries in a non-empty list") {
            GuiActionRunner.execute { table.entries = listOf("pirates,scatter", "underbit,country", "flexured,shape") }

            GuiActionRunner.execute { table.entries = listOf("shirt,scatter", "detail,country", "wheaties,shape") }

            assertThat(table.entries).containsExactly("shirt,scatter", "detail,country", "wheaties,shape")
        }
    }

    describe("hasEntry") {
        it("returns false if the entry is not present") {
            GuiActionRunner.execute { table.entries = listOf("remind,foot") }

            assertThat(table.hasEntry("either,crime")).isFalse()
        }

        it("returns true if the entry is present") {
            GuiActionRunner.execute { table.entries = listOf("thus,victory") }

            assertThat(table.hasEntry("thus,victory")).isTrue()
        }
    }

    describe("getEntry") {
        it("returns the entry in the given row") {
            GuiActionRunner.execute { table.entries = listOf("commerce,arch", "whom,square") }

            assertThat(table.getEntry(0)).isEqualTo("commerce,arch")
        }

        it("does not return negative out-of-bounds entries") {
            assertThatThrownBy { table.getEntry(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. rows=0, index=-1.")
        }

        it("does not return positive out-of-bounds entries") {
            GuiActionRunner.execute { table.addEntry("float,cousin") }

            assertThatThrownBy { table.getEntry(1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. rows=1, index=1.")
        }
    }

    describe("removeEntry") {
        it("removes the given entry") {
            GuiActionRunner.execute { table.entries = listOf("lessen,delicate", "backbite,heaven", "pump,patient") }

            GuiActionRunner.execute { table.removeEntry("backbite,heaven") }

            assertThat(table.entries).containsExactly("lessen,delicate", "pump,patient")
        }

        it("throws an exception if the element to be removed cannot be found") {
            GuiActionRunner.execute { table.entries = listOf("heighten,dear", "videotex,clear") }

            assertThatThrownBy { GuiActionRunner.execute { table.removeEntry("strict,general") } }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("No row with entry `strict,general` found.")
                .hasNoCause()
        }
    }

    describe("clear") {
        it("clears an empty list") {
            GuiActionRunner.execute { table.clear() }

            assertThat(table.entries).isEmpty()
        }

        it("clears a non-empty list") {
            GuiActionRunner.execute { table.entries = listOf("lathered,terrible", "wonder,hold") }

            GuiActionRunner.execute { table.clear() }

            assertThat(table.entries).isEmpty()
        }
    }


    describe("activeEntries") {
        it("has no active entries in an empty list") {
            assertThat(table.activeEntries).isEmpty()
        }

        it("has no active entries by default") {
            GuiActionRunner.execute { table.entries = listOf("proposal,for", "mouse,spin", "scarab,rank") }

            assertThat(table.activeEntries).isEmpty()
        }

        it("returns the currently-active entries") {
            GuiActionRunner.execute { table.addEntry("widen,forest", true) }

            assertThat(table.activeEntries).containsExactly("widen,forest")
        }
    }

    describe("isActive") {
        it("returns true iff an element is active") {
            GuiActionRunner.execute {
                table.entries = listOf("upright,compete", "set,industry")
                table.activeEntries = listOf("set,industry")
            }

            assertThat(table.isActive("upright,compete")).isFalse()
            assertThat(table.isActive("set,industry")).isTrue()
        }

        it("throws an exception if an element is not in the list") {
            assertThatThrownBy { table.isActive("space,pardon") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("No row with entry `space,pardon` found.")
                .hasNoCause()
        }
    }

    describe("setActive") {
        it("updates the activity of the given entry") {
            GuiActionRunner.execute { table.entries = listOf("wax,fellow") }

            GuiActionRunner.execute { table.setActive("wax,fellow", true) }

            assertThat(table.isActive("wax,fellow")).isTrue()
        }

        it("throws an exception if the entry does not exist") {
            assertThatThrownBy { GuiActionRunner.execute { table.setActive("pardon,crop", false) } }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("No row with entry `pardon,crop` found.")
        }
    }

    describe("setActiveEntries") {
        it("can enable a given entry") {
            GuiActionRunner.execute { table.entries = listOf("toe,another", "cause,minute", "sunhat,observe") }

            GuiActionRunner.execute { table.activeEntries = listOf("cause,minute") }

            assertThat(table.activeEntries).containsExactly("cause,minute")
        }

        it("can enable all entries") {
            val entries = listOf("big,chance", "cruel,head", "hooray,sit")
            GuiActionRunner.execute { table.entries = entries }

            GuiActionRunner.execute { table.activeEntries = entries }

            assertThat(table.activeEntries).containsExactlyElementsOf(entries)
        }

        it("ignores when a non-existing element is enabled") {
            GuiActionRunner.execute { table.entries = listOf("forte,dine", "blarneys,lead") }

            GuiActionRunner.execute { table.activeEntries = listOf("forte,dine", "lamp,being") }

            assertThat(table.activeEntries).containsExactly("forte,dine")
        }

        it("unchecks previously-checked entries") {
            GuiActionRunner.execute {
                table.entries = listOf("rot,victory", "possible,umbrella", "harbor,heavenly")
                table.activeEntries = table.entries
            }

            GuiActionRunner.execute { table.activeEntries = listOf("rot,victory", "harbor,heavenly") }

            assertThat(table.activeEntries).containsExactly("rot,victory", "harbor,heavenly")
        }
    }


    describe("getHighlightedEntries") {
        it("returns an empty list when no entries are highlighted") {
            assertThat(table.highlightedEntries).isEmpty()
        }

        it("returns only the highlighted entries") {
            GuiActionRunner.execute {
                table.entries = listOf("shutoffs,terrible", "pound,glass", "size,afford")
                table.addRowSelectionInterval(0, 0)
                table.addRowSelectionInterval(2, 2)
            }

            assertThat(table.highlightedEntries).containsExactly("shutoffs,terrible", "size,afford")
        }
    }

    describe("setHighlightedEntries") {
        it("unhighlights the current entries") {
            GuiActionRunner.execute {
                table.entries = listOf("waste,remain", "clever,moment")
                table.addRowSelectionInterval(0, 1)
            }

            GuiActionRunner.execute { table.highlightedEntries = emptyList() }

            assertThat(table.highlightedEntries).isEmpty()
        }

        it("moves the highlight to a different entry") {
            GuiActionRunner.execute {
                table.entries = listOf("copper,bury", "country,study")
                table.addRowSelectionInterval(0, 0)
            }

            GuiActionRunner.execute { table.highlightedEntries = listOf("country,study") }

            assertThat(table.highlightedEntries).containsExactly("country,study")
        }
    }


    describe("getColumnName") {
        it("returns the checkbox column's name") {
            assertThat(table.getColumnName(0)).isEqualTo("")
        }

        it("returns null if the column's name is null") {
            assertThat(table.getColumnName(1)).isNull()
        }

        it("returns the column's name if it is not null") {
            assertThat(table.getColumnName(2)).isEqualTo("name")
        }

        it("throws an exception for a negative out-of-bounds column") {
            assertThatThrownBy { table.getColumnName(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. columns=3, index=-1.")
        }

        it("throws an exception for a positive out-of-bounds column") {
            assertThatThrownBy { table.getColumnName(3) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. columns=3, index=3.")
        }
    }

    describe("getColumnClass") {
        it("returns the right types for the columns") {
            // Java classes MUST be used
            assertThat(table.getColumnClass(0)).isEqualTo(java.lang.Boolean::class.java)
            assertThat(table.getColumnClass(1)).isEqualTo(java.lang.String::class.java)
        }

        it("throws an exception if a negative out-of-bounds column is requested") {
            assertThatThrownBy { table.getColumnClass(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. columns=3, index=-1.")
                .hasNoCause()
        }

        it("throws an exception if a positive out-of-bounds column is requested") {
            assertThatThrownBy { table.getColumnClass(3) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Index out of bounds. columns=3, index=3.")
                .hasNoCause()
        }
    }

    describe("isCellEditable") {
        beforeEachTest {
            table = GuiActionRunner.execute<JCheckBoxTable<String>> {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, false)),
                    { it.joinToString(",") },
                    { it.split(",") },
                    { it.startsWith("_") }
                )
            }

            GuiActionRunner.execute { table.entries = listOf("forth,thief", "_cheese,mail") }
        }


        it("return true for the checkbox column, even if the entry is not editable") {
            assertThat(table.isCellEditable(0, 0)).isTrue()
        }

        it("returns true for the checkbox column when the entry is editable") {
            assertThat(table.isCellEditable(1, 0)).isTrue()
        }

        it("returns false if the column is not editable even if the entry is") {
            assertThat(table.isCellEditable(1, 2)).isFalse()
        }

        it("returns false if the entry is not editable even if the column is") {
            assertThat(table.isCellEditable(0, 1)).isFalse()
        }

        it("returns true if both the column and the entry are editable") {
            assertThat(table.isCellEditable(1, 1)).isTrue()
        }
    }
})


/**
 * Unit tests for [JDecoratedCheckBoxTablePanel].
 */
object JDecoratedCheckBoxTablePanelTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    fun createTable(query: () -> JCheckBoxTable<String>) = GuiActionRunner.execute(query)

    fun createPanel(query: () -> JDecoratedCheckBoxTablePanel<String>) = GuiActionRunner.execute(query)


    describe("appearance") {
        it("does not add any buttons by default") {
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table) }

            assertThat(panel.actionsPanel.actionMap.size()).isZero()
        }
    }

    describe("executes the desired actions") {
        fun <T> JDecoratedCheckBoxTablePanel<T>.pressButton(button: CommonActionsPanel.Buttons) =
            this.getButton(button)!!.actionPerformed(mock {})


        it("executes the add function if the add button is clicked") {
            var addedEntries: List<String> = emptyList()
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table, addAction = { addedEntries = it }) }
            GuiActionRunner.execute {
                table.addEntry("debt,promise")
                table.addEntry("common,gallon")
            }

            GuiActionRunner.execute {
                table.addRowSelectionInterval(0, 1)
                panel.pressButton(CommonActionsPanel.Buttons.ADD)
            }

            assertThat(addedEntries).containsExactly("debt,promise", "common,gallon")
        }

        it("executes the edit function if the edit button is clicked") {
            var editedEntries: List<String> = emptyList()
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table, editAction = { editedEntries = it }) }
            GuiActionRunner.execute {
                table.addEntry("collar,lend")
                table.addEntry("neck,language")
            }

            GuiActionRunner.execute {
                table.addRowSelectionInterval(0, 1)
                panel.pressButton(CommonActionsPanel.Buttons.EDIT)
            }

            assertThat(editedEntries).containsExactly("collar,lend", "neck,language")
        }

        it("does nothing if the edit button is clicked but no entry is highlighted") {
            var editedEntries: List<String> = emptyList()
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table, editAction = { editedEntries = it }) }
            GuiActionRunner.execute { table.addEntry("water,least") }

            GuiActionRunner.execute {
                table.clearSelection()
                panel.pressButton(CommonActionsPanel.Buttons.EDIT)
            }

            assertThat(editedEntries).isEmpty()
        }

        it("executes the remove function if the remove button is clicked") {
            var removedEntries: List<String> = emptyList()
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table, removeAction = { removedEntries = it }) }
            GuiActionRunner.execute { table.addEntry("dip,duck") }

            GuiActionRunner.execute {
                table.addRowSelectionInterval(0, 0)
                panel.pressButton(CommonActionsPanel.Buttons.REMOVE)
            }

            assertThat(removedEntries).containsExactly("dip,duck")
        }

        it("does nothing if the remove button is clicked but no entry is highlighted") {
            var removedEntries: List<String> = emptyList()
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table, removeAction = { removedEntries = it }) }
            GuiActionRunner.execute { table.addEntry("tax,applause") }

            GuiActionRunner.execute {
                table.clearSelection()
                panel.pressButton(CommonActionsPanel.Buttons.REMOVE)
            }

            assertThat(removedEntries).isEmpty()
        }
    }

    describe("getting buttons") {
        it("returns null if the button was not added") {
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table, editAction = {}) }

            assertThat(panel.getButton(CommonActionsPanel.Buttons.ADD)).isNull()
        }

        it("returns the appropriate button") {
            val table = createTable {
                JCheckBoxTable(
                    listOf(JCheckBoxTable.Column(null, true), JCheckBoxTable.Column(null, true)),
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                )
            }
            val panel = createPanel { JDecoratedCheckBoxTablePanel(table, addAction = {}, removeAction = {}) }

            assertThat(panel.getButton(CommonActionsPanel.Buttons.ADD)).isNotNull()
        }
    }
})
