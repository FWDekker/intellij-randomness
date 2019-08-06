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


    describe("type column") {
        it("displays 'bundled' for bundled dictionaries") {
            val dictionary = BundledDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 1)).isEqualTo("bundled")
        }

        it("displays 'user' for user dictionaries") {
            val dictionary = UserDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 1)).isEqualTo("user")
        }

        it("throws an exception for unknown dictionary types") {
            assertThatThrownBy { GuiActionRunner.execute { dictionaryTable.data = listOf(mock {}) } }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage(DictionaryTable.DICTIONARY_CAST_EXCEPTION)
        }
    }

    describe("location column") {
        it("displays the location of a bundled dictionary") {
            val dictionary = BundledDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 2)).isEqualTo("dictionary.dic")
        }

        it("displays the location of a user dictionary") {
            val dictionary = UserDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(dictionary) }

            assertThat(dictionaryTable.model.getValueAt(0, 2)).isEqualTo("dictionary.dic")
        }

        it("throws an exception for unknown dictionary types") {
            assertThatThrownBy { GuiActionRunner.execute { dictionaryTable.data = listOf(mock {}) } }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage(DictionaryTable.DICTIONARY_CAST_EXCEPTION)
        }

        it("changes the location of a user dictionary") {
            val oldDictionary = UserDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(oldDictionary) }

            GuiActionRunner.execute { dictionaryTable.model.setValueAt("new_dictionary.dic", 0, 2) }

            val newDictionary = dictionaryTable.data.first()
            assertThat(newDictionary).isInstanceOf(UserDictionary::class.java)
            assertThat((newDictionary as UserDictionary).filename).isEqualTo("new_dictionary.dic")
        }

        it("cannot edit the location of a bundled dictionary") {
            val oldDictionary = BundledDictionary.cache.get("dictionary.dic")
            GuiActionRunner.execute { dictionaryTable.data = listOf(oldDictionary) }

            assertThat(dictionaryTable.model.isCellEditable(0, 2)).isFalse()
        }
    }

    // TODO: Remove and copy buttons not accessible from tests
    xdescribe("itemEditor") {
        describe("remove") {
            it("does not remove a bundled dictionary") {}

            it("removes a user dictionary") {}
        }

        describe("copy") {
            it("copies a bundled dictionary to a user dictionary") {}

            it("copies a user dictionary to a user dictionary") {}

            it("cannot copy a different type of dictionary") {}
        }
    }
})
