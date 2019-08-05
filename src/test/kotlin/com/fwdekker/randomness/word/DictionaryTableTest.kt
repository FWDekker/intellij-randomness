package com.fwdekker.randomness.word

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe


/**
 * Unit tests for [DictionaryTable].
 */
object DictionaryTableTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var dictionaryTable: DictionaryTable


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        dictionaryTable = GuiActionRunner.execute<DictionaryTable> { DictionaryTable() }
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("dictionaries") {
        it("returns an empty list by default") {
            assertThat(dictionaryTable.dictionaries).isEmpty()
        }

        it("adds entries to the underlying model") {
            val dictionary = UserDictionary.cache.get("dictionary.dic")

            GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(dictionary) }

            assertThat(dictionaryTable.model.items)
                .containsExactly(DictionaryTable.EditableDictionary(false, dictionary))
        }

        it("removes entries from the underlying model") {
            val dictionary = UserDictionary.cache.get("dictionary.dic")

            GuiActionRunner.execute {
                dictionaryTable.dictionaries = listOf(dictionary)
                dictionaryTable.dictionaries = listOf()
            }

            assertThat(dictionaryTable.model.items).isEmpty()
        }

        it("returns items that were added directly to the model") {
            val dictionary = UserDictionary.cache.get("dictionary.dic")

            GuiActionRunner.execute {
                dictionaryTable.model.addRow(DictionaryTable.EditableDictionary(false, dictionary))
            }

            assertThat(dictionaryTable.dictionaries).containsExactly(dictionary)
        }
    }

    describe("activeDictionaries") {
        it("returns an empty list by default") {
            assertThat(dictionaryTable.activeDictionaries).isEmpty()
        }

        it("returns only active dictionaries") {
            val dictionary1 = UserDictionary.cache.get("dictionary1.dic")
            val dictionary2 = UserDictionary.cache.get("dictionary2.dic")

            GuiActionRunner.execute {
                dictionaryTable.model.addRow(DictionaryTable.EditableDictionary(false, dictionary1))
                dictionaryTable.model.addRow(DictionaryTable.EditableDictionary(true, dictionary2))
            }

            assertThat(dictionaryTable.activeDictionaries).containsExactly(dictionary2)
        }

        it("activates the given dictionaries") {
            val dictionary1 = UserDictionary.cache.get("dictionary1.dic")
            val dictionary2 = UserDictionary.cache.get("dictionary2.dic")
            GuiActionRunner.execute {
                dictionaryTable.model.addRow(DictionaryTable.EditableDictionary(false, dictionary1))
                dictionaryTable.model.addRow(DictionaryTable.EditableDictionary(false, dictionary2))
            }

            GuiActionRunner.execute { dictionaryTable.activeDictionaries = listOf(dictionary1) }

            assertThat(dictionaryTable.activeDictionaries).containsExactly(dictionary1)
        }

        it("deactivates all other dictionaries") {
            val dictionary1 = UserDictionary.cache.get("dictionary1.dic")
            val dictionary2 = UserDictionary.cache.get("dictionary2.dic")
            GuiActionRunner.execute {
                dictionaryTable.model.addRow(DictionaryTable.EditableDictionary(true, dictionary1))
                dictionaryTable.model.addRow(DictionaryTable.EditableDictionary(true, dictionary2))
            }

            GuiActionRunner.execute { dictionaryTable.activeDictionaries = listOf(dictionary2) }

            assertThat(dictionaryTable.activeDictionaries).containsExactly(dictionary2)
        }
    }

    describe("activity column") {
        it("deactivates the dictionary if the activity cell is set to false") {
            val dictionary = DictionaryTable.EditableDictionary(true, BundledDictionary.cache.get("dictionary.dic"))
            GuiActionRunner.execute { dictionaryTable.model.addRow(dictionary) }

            GuiActionRunner.execute { dictionaryTable.model.setValueAt(false, 0, 0) }

            assertThat(dictionaryTable.model.items[0].active).isFalse()
        }

        it("activates the dictionary if the activity cell is set to true") {
            val dictionary = DictionaryTable.EditableDictionary(false, BundledDictionary.cache.get("dictionary.dic"))
            GuiActionRunner.execute { dictionaryTable.model.addRow(dictionary) }

            GuiActionRunner.execute { dictionaryTable.model.setValueAt(true, 0, 0) }

            assertThat(dictionaryTable.model.items[0].active).isTrue()
        }
    }

    describe("type column") {
        it("displays 'bundled' for bundled dictionaries") {
            val dictionary = BundledDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 1)).isEqualTo("bundled")
        }

        it("displays 'user' for user dictionaries") {
            val dictionary = UserDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 1)).isEqualTo("user")
        }

        it("throws an exception for unknown dictionary types") {
            assertThatThrownBy { GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(mock {}) } }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Unexpected dictionary implementation.")
        }
    }

    describe("location column") {
        it("displays the location of a bundled dictionary") {
            val dictionary = BundledDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 2)).isEqualTo("dictionary.dic")
        }

        it("displays the location of a user dictionary") {
            val dictionary = UserDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 2)).isEqualTo("dictionary.dic")
        }

        it("throws an exception for unknown dictionary types") {
            assertThatThrownBy { GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(mock {}) } }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Unexpected dictionary implementation.")
        }

        it("changes the location of a user dictionary") {
            val oldDictionary = UserDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(oldDictionary) }

            GuiActionRunner.execute { dictionaryTable.model.setValueAt("new_dictionary.dic", 0, 2) }

            val newDictionary = dictionaryTable.model.items[0].dictionary
            assertThat(newDictionary).isInstanceOf(UserDictionary::class.java)
            assertThat((newDictionary as UserDictionary).filename).isEqualTo("new_dictionary.dic")
        }

        it("cannot edit the location of a bundled dictionary") {
            val oldDictionary = BundledDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.dictionaries = listOf(oldDictionary) }

            assertThat(dictionaryTable.model.isCellEditable(0, 2)).isFalse()
        }
    }

    // TODO: Remove button not accessible from tests
    xdescribe("itemEditor") {
        it("does not remove a bundled dictionary") {
        }

        it("removes a user dictionary") {
        }
    }
})
