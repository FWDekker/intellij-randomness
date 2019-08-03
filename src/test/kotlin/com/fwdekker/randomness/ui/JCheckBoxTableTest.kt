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
    lateinit var table: JCheckBoxTable<String>


    beforeEachTest {
        table = GuiActionRunner.execute<JCheckBoxTable<String>> {
            JCheckBoxTable(
                columnCount = 2,
                listToEntry = { it.joinToString(",") },
                entryToList = { it.split(",") }
            )
        }
    }


    describe("getEntries") {
        it("returns nothing if no entries are added") {
            assertThat(table.entries).isEmpty()
        }
    }

    describe("addEntry") {
        it("adds a given entry") {
            GuiActionRunner.execute { table.addEntry("manscape,bicycle") }

            assertThat(table.entries).containsExactly("manscape,bicycle")
        }

        it("adds two given entries") {
            GuiActionRunner.execute {
                table.addEntry("osteomas,quart")
                table.addEntry("imido,basic")
            }

            assertThat(table.entries).containsExactly("osteomas,quart", "imido,basic")
        }

        it("does not add a given entry twice") {
            GuiActionRunner.execute {
                table.addEntry("bloomed,dull")
                table.addEntry("bloomed,dull")
            }

            assertThat(table.entries).containsExactly("bloomed,dull")
        }
    }

    describe("setEntries") {
        it("empties an empty list") {
            GuiActionRunner.execute { table.setEntries(emptyList()) }

            assertThat(table.entries).isEmpty()
        }

        it("adds two entries to an empty list") {
            GuiActionRunner.execute { table.setEntries(listOf("gladding,which", "flecky,arch")) }

            assertThat(table.entries).containsExactly("gladding,which", "flecky,arch")
        }

        it("does not add duplicate entries to a list") {
            GuiActionRunner.execute { table.setEntries(listOf("orrery,northern", "orrery,northern")) }

            assertThat(table.entries).containsExactly("orrery,northern")
        }

        it("empties a non-empty list") {
            GuiActionRunner.execute {
                table.addEntry("prefeast,sock")
                table.addEntry("strip,hurt")
            }

            GuiActionRunner.execute { table.setEntries(emptyList()) }

            assertThat(table.entries).isEmpty()
        }

        it("replaces the entries in a non-empty list") {
            GuiActionRunner.execute {
                table.addEntry("pirates,scatter")
                table.addEntry("underbit,country")
                table.addEntry("flexured,shape")
            }

            GuiActionRunner.execute { table.setEntries(listOf("steevely,scatter", "qual,country", "wheaties,shape")) }

            assertThat(table.entries).containsExactly("steevely,scatter", "qual,country", "wheaties,shape")
        }
    }

    describe("removeEntry") {
        it("removes the given entry") {
            GuiActionRunner.execute {
                table.addEntry("giftwrap,delicate")
                table.addEntry("backbite,heaven")
                table.addEntry("landwhin,patient")
            }

            GuiActionRunner.execute { table.removeEntry("backbite,heaven") }

            assertThat(table.entries).containsExactly("giftwrap,delicate", "landwhin,patient")
        }

        it("throws an exception if the element to be removed cannot be found") {
            GuiActionRunner.execute {
                table.addEntry("tracheid,dear")
                table.addEntry("cocomat,youth")
                table.addEntry("videotex,clear")
            }

            assertThatThrownBy { table.removeEntry("unshowed,general") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("No row with entry `unshowed,general` found.")
                .hasNoCause()
        }
    }

    describe("clear") {
        it("clears an empty list") {
            GuiActionRunner.execute { table.clear() }

            assertThat(table.entries).isEmpty()
        }

        it("clears a non-empty list") {
            GuiActionRunner.execute {
                table.addEntry("lathered,terrible")
                table.addEntry("aquench,hold")
            }

            GuiActionRunner.execute { table.clear() }

            assertThat(table.entries).isEmpty()
        }
    }

    describe("entryCount") {
        it("counts 0 elements for an empty list") {
            assertThat(table.entryCount).isEqualTo(0)
        }

        it("counts 2 elements for a list with 2 elements") {
            GuiActionRunner.execute {
                table.addEntry("musmon,supply")
                table.addEntry("topepo,light")
            }

            assertThat(table.entryCount).isEqualTo(2)
        }
    }

    describe("activeEntries") {
        it("has no active entries in an empty list") {
            assertThat(table.activeEntries).isEmpty()
        }

        it("has no active entries by default") {
            GuiActionRunner.execute { table.setEntries(listOf("otxi,for", "chamorro,spin", "scarab,rank")) }

            assertThat(table.activeEntries).isEmpty()
        }

        it("can enable one entry") {
            GuiActionRunner.execute { table.setEntries(listOf("disbury,another", "curet,minute", "sunhat,observe")) }

            GuiActionRunner.execute { table.setActiveEntries(listOf("curet,minute")) }

            assertThat(table.activeEntries).containsExactly("curet,minute")
        }

        it("can enable all entries") {
            val entries = listOf("big,chance", "coumaric,head", "hooray,sit")
            GuiActionRunner.execute { table.setEntries(entries) }

            GuiActionRunner.execute { table.setActiveEntries(entries) }

            assertThat(table.activeEntries).containsExactlyElementsOf(entries)
        }

        it("ignores when a non-existing element is enabled") {
            val entries = listOf("forte,dine", "blarneys,lead")
            GuiActionRunner.execute { table.setEntries(entries) }

            GuiActionRunner.execute { table.setActiveEntries(listOf("forte,dine", "pittings,being")) }

            assertThat(table.activeEntries).containsExactly("forte,dine")
        }

        it("unchecks previously checked entries") {
            val entries = listOf("rot,victory", "possible,umbrella", "harbor,heavenly")
            GuiActionRunner.execute { table.setEntries(entries) }
            GuiActionRunner.execute { table.setActiveEntries(entries) }

            GuiActionRunner.execute { table.setActiveEntries(listOf("rot,victory", "harbor,heavenly")) }

            assertThat(table.activeEntries).containsExactly("rot,victory", "harbor,heavenly")
        }
    }

    describe("isActive") {
        it("returns true iff an element is active") {
            GuiActionRunner.execute { table.setEntries(listOf("upright,compete", "hasten,chicken", "set,industry")) }

            GuiActionRunner.execute { table.setEntryActivity("upright,compete", true) }

            assertThat(table.isActive("upright,compete")).isTrue()
            assertThat(table.isActive("hasten,chicken")).isFalse()
            assertThat(table.isActive("set,industry")).isFalse()
        }

        it("throws an exception if an element is not in the list") {
            assertThatThrownBy { table.isActive("nanism,pardon") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("No row with entry `nanism,pardon` found.")
                .hasNoCause()
        }
    }

    describe("activityListener") {
        it("fires when an entry's activity is changed") {
            var fired = false
            GuiActionRunner.execute { table.addEntry("scarpa,nothing") }
            table.addEntryActivityChangeListener { fired = true }

            GuiActionRunner.execute { table.setEntryActivity("scarpa,nothing", true) }

            assertThat(fired).isTrue()
        }

        it("does not fire when an entry is inserted") {
            var fired = false
            table.addEntryActivityChangeListener { fired = true }

            GuiActionRunner.execute { table.addEntry("techiest,harbor") }

            assertThat(fired).isFalse()
        }

        it("stops firing after a listener is removed") {
            var fired = false
            val listener = { _: Int -> fired = true }
            GuiActionRunner.execute { table.addEntry("optimum,middle") }
            table.addEntryActivityChangeListener(listener)

            table.removeEntryActivityChangeListener(listener)
            GuiActionRunner.execute { table.setEntryActivity("optimum,middle", true) }

            assertThat(fired).isFalse()
        }
    }

    describe("getHighlightedEntry") {
        it("returns null when no entry is highlighted") {
            assertThat(table.highlightedEntries).isEmpty()
        }

        it("returns the single highlighted entry") {
            GuiActionRunner.execute {
                table.setEntries(listOf("bahay,leave", "woodyard,oil", "hussies,coast"))
                table.addRowSelectionInterval(0, 0)
            }

            assertThat(table.highlightedEntries).containsExactly("bahay,leave")
        }

        it("returns all highlighted entries") {
            GuiActionRunner.execute {
                table.setEntries(listOf("shutoffs,terrible", "dentine,glass", "scutel,afford"))
                table.addRowSelectionInterval(0, 0)
                table.addRowSelectionInterval(2, 2)
            }

            assertThat(table.highlightedEntries).containsExactly("shutoffs,terrible", "scutel,afford")
        }
    }

    describe("getColumnClass") {
        it("returns the right types for the columns") {
            // Java classes MUST be used
            assertThat(table.getColumnClass(0)).isEqualTo(java.lang.Boolean::class.java)
            assertThat(table.getColumnClass(1)).isEqualTo(java.lang.String::class.java)
        }

        it("throws an exception if a negative column index is requested") {
            assertThatThrownBy { table.getColumnClass(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("JEditableList has only two columns.")
                .hasNoCause()
        }

        it("throws an exception if column index that is too high is requested") {
            assertThatThrownBy { table.getColumnClass(3) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("JEditableList has only two columns.")
                .hasNoCause()
        }
    }

    describe("isCellEditable") {
        it("returns true iff the column is 0") {
            assertThat(table.isCellEditable(13, 0)).isTrue()
            assertThat(table.isCellEditable(67, 0)).isTrue()

            assertThat(table.isCellEditable(10, 5)).isFalse()
            assertThat(table.isCellEditable(7, 10)).isFalse()
            assertThat(table.isCellEditable(8, 2)).isFalse()
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


    fun createTablePanel(query: () -> JDecoratedCheckBoxTablePanel<String>) = GuiActionRunner.execute(query)


    describe("appearance") {
        it("assigns the given name to the inner list") {
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(JCheckBoxTable(
                    columnCount = 2,
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") },
                    name = "thirst"
                ))
            }

            assertThat(table.table.name).isEqualTo("thirst")
        }

        it("does not add any buttons by default") {
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(JCheckBoxTable(
                    columnCount = 2,
                    listToEntry = { it.joinToString(",") },
                    entryToList = { it.split(",") }
                ))
            }

            assertThat(table.actionsPanel.actionMap.size()).isZero()
        }
    }

    describe("executes the desired actions") {
        fun <T> JDecoratedCheckBoxTablePanel<T>.pressButton(button: CommonActionsPanel.Buttons) =
            this.getButton(button)!!.actionPerformed(mock {})


        it("executes the add function if the add button is clicked") {
            var addedEntries: List<String> = emptyList()
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(
                    JCheckBoxTable(
                        columnCount = 2,
                        listToEntry = { it.joinToString(",") },
                        entryToList = { it.split(",") }
                    ),
                    addAction = { addedEntries = it }
                )
            }
            GuiActionRunner.execute {
                table.table.addEntry("debt,promise")
                table.table.addEntry("common,gallon")
            }

            GuiActionRunner.execute {
                table.table.addRowSelectionInterval(0, 1)
                table.pressButton(CommonActionsPanel.Buttons.ADD)
            }

            assertThat(addedEntries).containsExactly("debt,promise", "common,gallon")
        }

        it("executes the edit function if the edit button is clicked") {
            var editedEntries: List<String> = emptyList()
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(
                    JCheckBoxTable(
                        columnCount = 2,
                        listToEntry = { it.joinToString(",") },
                        entryToList = { it.split(",") }
                    ),
                    editAction = { editedEntries = it }
                )
            }
            GuiActionRunner.execute {
                table.table.addEntry("collar,lend")
                table.table.addEntry("neck,language")
            }

            GuiActionRunner.execute {
                table.table.addRowSelectionInterval(0, 1)
                table.pressButton(CommonActionsPanel.Buttons.EDIT)
            }

            assertThat(editedEntries).containsExactly("collar,lend", "neck,language")
        }

        it("does nothing if the edit button is clicked but no entry is highlighted") {
            var editedEntries: List<String> = emptyList()
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(
                    JCheckBoxTable(
                        columnCount = 2,
                        listToEntry = { it.joinToString(",") },
                        entryToList = { it.split(",") }
                    ),
                    editAction = { editedEntries = it }
                )
            }
            GuiActionRunner.execute { table.table.addEntry("water,least") }

            GuiActionRunner.execute {
                table.table.clearSelection()
                table.pressButton(CommonActionsPanel.Buttons.EDIT)
            }

            assertThat(editedEntries).isEmpty()
        }

        it("executes the remove function if the remove button is clicked") {
            var removedEntries: List<String> = emptyList()
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(
                    JCheckBoxTable(
                        columnCount = 2,
                        listToEntry = { it.joinToString(",") },
                        entryToList = { it.split(",") }
                    ),
                    removeAction = { removedEntries = it }
                )
            }
            GuiActionRunner.execute { table.table.addEntry("dip,duck") }

            GuiActionRunner.execute {
                table.table.addRowSelectionInterval(0, 0)
                table.pressButton(CommonActionsPanel.Buttons.REMOVE)
            }

            assertThat(removedEntries).containsExactly("dip,duck")
        }

        it("does nothing if the remove button is clicked but no entry is highlighted") {
            var removedEntries: List<String> = emptyList()
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(
                    JCheckBoxTable(
                        columnCount = 2,
                        listToEntry = { it.joinToString(",") },
                        entryToList = { it.split(",") }
                    ),
                    removeAction = { removedEntries = it }
                )
            }
            GuiActionRunner.execute { table.table.addEntry("tax,applause") }

            GuiActionRunner.execute {
                table.table.clearSelection()
                table.pressButton(CommonActionsPanel.Buttons.REMOVE)
            }

            assertThat(removedEntries).isEmpty()
        }
    }

    describe("getting buttons") {
        it("returns null if the button was not added") {
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(
                    JCheckBoxTable(
                        columnCount = 2,
                        listToEntry = { it.joinToString(",") },
                        entryToList = { it.split(",") }
                    ),
                    editAction = {}
                )
            }

            assertThat(table.getButton(CommonActionsPanel.Buttons.ADD)).isNull()
        }

        it("returns the appropriate button") {
            val table = createTablePanel {
                JDecoratedCheckBoxTablePanel(
                    JCheckBoxTable(
                        columnCount = 2,
                        listToEntry = { it.joinToString(",") },
                        entryToList = { it.split(",") }
                    ),
                    addAction = {},
                    removeAction = {}
                )
            }

            assertThat(table.getButton(CommonActionsPanel.Buttons.ADD)).isNotNull()
        }
    }
})
