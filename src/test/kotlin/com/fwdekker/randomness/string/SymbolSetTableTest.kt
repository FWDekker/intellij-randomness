package com.fwdekker.randomness.string

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe


/**
 * Unit tests for [SymbolSetTable].
 */
object SymbolSetTableTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var symbolSetTable: SymbolSetTable


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        symbolSetTable = GuiActionRunner.execute<SymbolSetTable> { SymbolSetTable() }
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("symbolSets") {
        it("returns an empty list by default") {
            assertThat(symbolSetTable.symbolSets).isEmpty()
        }

        it("adds entries to the underlying model") {
            val symbolSet = SymbolSet("hit", "interest")

            GuiActionRunner.execute { symbolSetTable.symbolSets = listOf(symbolSet) }

            assertThat(symbolSetTable.model.items).containsExactly(SymbolSetTable.EditableSymbolSet(false, symbolSet))
        }

        it("removes entries from the underlying model") {
            val symbolSet = SymbolSet("mere", "with")

            GuiActionRunner.execute {
                symbolSetTable.symbolSets = listOf(symbolSet)
                symbolSetTable.symbolSets = listOf()
            }

            assertThat(symbolSetTable.model.items).isEmpty()
        }

        it("returns items that were added directly to the model") {
            val symbolSet = SymbolSet("record", "between")

            GuiActionRunner.execute { symbolSetTable.model.addRow(SymbolSetTable.EditableSymbolSet(false, symbolSet)) }

            assertThat(symbolSetTable.symbolSets).containsExactly(symbolSet)
        }
    }

    describe("activeSymbolSets") {
        it("returns an empty list by default") {
            assertThat(symbolSetTable.activeSymbolSets).isEmpty()
        }

        it("returns only active symbol sets") {
            val symbolSet1 = SymbolSet("throat", "stroke")
            val symbolSet2 = SymbolSet("distant", "late")

            GuiActionRunner.execute {
                symbolSetTable.model.addRow(SymbolSetTable.EditableSymbolSet(false, symbolSet1))
                symbolSetTable.model.addRow(SymbolSetTable.EditableSymbolSet(true, symbolSet2))
            }

            assertThat(symbolSetTable.activeSymbolSets).containsExactly(symbolSet2)
        }

        it("activates the given symbol sets") {
            val symbolSet1 = SymbolSet("blue", "resign")
            val symbolSet2 = SymbolSet("tree", "tonight")
            GuiActionRunner.execute {
                symbolSetTable.model.addRow(SymbolSetTable.EditableSymbolSet(false, symbolSet1))
                symbolSetTable.model.addRow(SymbolSetTable.EditableSymbolSet(false, symbolSet2))
            }

            GuiActionRunner.execute { symbolSetTable.activeSymbolSets = listOf(symbolSet1) }

            assertThat(symbolSetTable.activeSymbolSets).containsExactly(symbolSet1)
        }

        it("deactivates all other symbol sets") {
            val symbolSet1 = SymbolSet("pipe", "secret")
            val symbolSet2 = SymbolSet("success", "voice")
            GuiActionRunner.execute {
                symbolSetTable.model.addRow(SymbolSetTable.EditableSymbolSet(true, symbolSet1))
                symbolSetTable.model.addRow(SymbolSetTable.EditableSymbolSet(true, symbolSet2))
            }

            GuiActionRunner.execute { symbolSetTable.activeSymbolSets = listOf(symbolSet2) }

            assertThat(symbolSetTable.activeSymbolSets).containsExactly(symbolSet2)
        }
    }

    describe("activity column") {
        it("deactivates the symbol set if the activity cell is set to false") {
            val symbolSet = SymbolSetTable.EditableSymbolSet(true, SymbolSet("multiply", "sadden"))
            GuiActionRunner.execute { symbolSetTable.model.addRow(symbolSet) }

            GuiActionRunner.execute { symbolSetTable.model.setValueAt(false, 0, 0) }

            assertThat(symbolSetTable.model.items[0].active).isFalse()
        }

        it("activates the symbol sets if the activity cell is set to true") {
            val symbolSet = SymbolSetTable.EditableSymbolSet(false, SymbolSet("fat", "usual"))
            GuiActionRunner.execute { symbolSetTable.model.addRow(symbolSet) }

            GuiActionRunner.execute { symbolSetTable.model.setValueAt(true, 0, 0) }

            assertThat(symbolSetTable.model.items[0].active).isTrue()
        }
    }

    describe("name column") {
        it("displays the name of the symbol set") {
            val symbolSet = SymbolSet("coat", "proper")
            GuiActionRunner.execute { symbolSetTable.symbolSets = listOf(symbolSet) }

            assertThat(symbolSetTable.model.getValueAt(0, 1)).isEqualTo("coat")
        }

        it("updates the name of the symbol set") {
            val symbolSet = SymbolSet("explode", "narrow")
            GuiActionRunner.execute { symbolSetTable.symbolSets = listOf(symbolSet) }

            GuiActionRunner.execute {symbolSetTable.model.setValueAt("salary", 0, 1)}

            assertThat(symbolSetTable.symbolSets.first().name).isEqualTo("salary")
        }
    }

    describe("symbols column") {
        it("displays the symbols of a symbol set") {
            val symbolSet = SymbolSet("neglect", "remark")
            GuiActionRunner.execute { symbolSetTable.symbolSets = listOf(symbolSet) }

            assertThat(symbolSetTable.model.getValueAt(0, 2)).isEqualTo("remark")
        }

        it("updates the symbols of the symbol set") {
            val symbolSet = SymbolSet("test", "extra")
            GuiActionRunner.execute { symbolSetTable.symbolSets = listOf(symbolSet) }

            GuiActionRunner.execute {symbolSetTable.model.setValueAt("drum", 0, 2)}

            assertThat(symbolSetTable.symbolSets.first().symbols).isEqualTo("drum")
        }
    }

    // TODO: Remove button not accessible from tests
    xdescribe("itemEditor") {
        it("removes a symbol set") {
        }
    }
})
