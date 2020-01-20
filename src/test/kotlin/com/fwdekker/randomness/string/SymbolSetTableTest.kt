package com.fwdekker.randomness.string

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.style.specification.xdescribe


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

    describe("name column") {
        it("displays the name of the symbol set") {
            GuiActionRunner.execute { symbolSetTable.data = listOf(SymbolSet("coat", "proper")) }

            assertThat(symbolSetTable.model.getValueAt(0, 1)).isEqualTo("coat")
        }

        it("updates the name of the symbol set") {
            GuiActionRunner.execute { symbolSetTable.data = listOf(SymbolSet("explode", "narrow")) }

            GuiActionRunner.execute { symbolSetTable.model.setValueAt("salary", 0, 1) }

            assertThat(symbolSetTable.data.first().name).isEqualTo("salary")
        }
    }

    describe("symbols column") {
        it("displays the symbols of a symbol set") {
            GuiActionRunner.execute { symbolSetTable.data = listOf(SymbolSet("neglect", "remark")) }

            assertThat(symbolSetTable.model.getValueAt(0, 2)).isEqualTo("remark")
        }

        it("updates the symbols of the symbol set") {
            GuiActionRunner.execute { symbolSetTable.data = listOf(SymbolSet("test", "extra")) }

            GuiActionRunner.execute { symbolSetTable.model.setValueAt("drum", 0, 2) }

            assertThat(symbolSetTable.data.first().symbols).isEqualTo("drum")
        }
    }

    // TODO: Remove button not accessible from tests
    xdescribe("itemEditor") {
        it("removes a symbol set") {
        }
    }
})
