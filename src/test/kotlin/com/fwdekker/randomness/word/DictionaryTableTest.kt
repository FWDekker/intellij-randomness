package com.fwdekker.randomness.word

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.style.specification.xdescribe


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
