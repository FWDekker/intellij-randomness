package com.fwdekker.randomness.word

import com.fwdekker.randomness.clickActionButton
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [DictionaryTable].
 */
object DictionaryTableTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var dictionaryTable: DictionaryTable


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        dictionaryTable = GuiActionRunner.execute<DictionaryTable> { DictionaryTable() }
        frame = Containers.showInFrame(dictionaryTable.panel)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("type column") {
        it("displays 'bundled' for bundled dictionaries") {
            val dictionary = DictionaryReference(true, "dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 1)).isEqualTo("bundled")
        }

        it("displays 'user' for user dictionaries") {
            val dictionary = DictionaryReference(false, "dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 1)).isEqualTo("user")
        }
    }

    describe("location column") {
        it("displays the location of a bundled dictionary") {
            val dictionary = DictionaryReference(true, "dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 2)).isEqualTo("dictionary.dic")
        }

        it("displays the location of a user dictionary") {
            val dictionary = DictionaryReference(false, "dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 2)).isEqualTo("dictionary.dic")
        }

        it("cannot edit the location of a bundled dictionary") {
            val oldDictionary = DictionaryReference(true, "dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(oldDictionary) }

            assertThat(dictionaryTable.model.isCellEditable(0, 2)).isFalse()
        }

        it("changes the location of a user dictionary") {
            val oldDictionary = DictionaryReference(false, "dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(oldDictionary) }

            GuiActionRunner.execute { dictionaryTable.model.setValueAt("new_dictionary.dic", 0, 2) }

            val newDictionary = dictionaryTable.data.first()
            assertThat(newDictionary.isBundled).isFalse()
            assertThat(newDictionary.filename).isEqualTo("new_dictionary.dic")
        }
    }


    describe("itemEditor") {
        describe("remove") {
            it("does not remove a bundled dictionary") {
                val bundledDictionary = DictionaryReference(true, "dictionary.dic")
                GuiActionRunner.execute { dictionaryTable.data = listOf(bundledDictionary) }

                GuiActionRunner.execute {
                    frame.table().target().setRowSelectionInterval(0, 0)
                    frame.clickActionButton("Remove")
                }

                assertThat(dictionaryTable.data).hasSize(1)
            }

            it("removes a user dictionary") {
                val userDictionary = DictionaryReference(false, "dictionary.dic")
                GuiActionRunner.execute { dictionaryTable.data = listOf(userDictionary) }

                GuiActionRunner.execute {
                    frame.table().target().setRowSelectionInterval(0, 0)
                    frame.clickActionButton("Remove")
                }

                assertThat(dictionaryTable.data).hasSize(0)
            }
        }
    }
})
