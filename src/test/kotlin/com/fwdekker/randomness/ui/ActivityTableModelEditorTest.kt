package com.fwdekker.randomness.ui

import com.fwdekker.randomness.clickActionButton
import com.fwdekker.randomness.ui.ActivityTableModelEditor.Companion.DEFAULT_STATE
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.ColumnInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [ActivityTableModelEditor].
 */
object ActivityTableModelEditorTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var stringTable: StringTable


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        stringTable = GuiActionRunner.execute<StringTable> { StringTable() }
        frame = Containers.showInFrame(stringTable.panel)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("data") {
        it("returns an empty list by default") {
            assertThat(stringTable.data).isEmpty()
        }

        it("adds entries to the underlying model") {
            GuiActionRunner.execute { stringTable.data = listOf("overflow") }

            assertThat(stringTable.model.items).containsExactly(EditableDatum(DEFAULT_STATE, "overflow"))
        }

        it("removes entries from the underlying model") {
            GuiActionRunner.execute {
                stringTable.data = listOf("bucket")
                stringTable.data = emptyList()
            }

            assertThat(stringTable.model.items).isEmpty()
        }

        it("returns items that were added directly to the model") {
            GuiActionRunner.execute { stringTable.model.addRow(EditableDatum(false, "hole")) }

            assertThat(stringTable.data).containsExactly("hole")
        }
    }

    describe("activeData") {
        it("returns an empty list by default") {
            assertThat(stringTable.activeData).isEmpty()
        }

        it("returns only active symbol sets") {
            GuiActionRunner.execute {
                stringTable.model.addRow(EditableDatum(false, "passage"))
                stringTable.model.addRow(EditableDatum(true, "limit"))
            }

            assertThat(stringTable.activeData).containsExactly("limit")
        }

        it("activates the given symbol sets") {
            GuiActionRunner.execute {
                stringTable.data = listOf("press", "thin")
                stringTable.activeData = listOf("press")
            }

            assertThat(stringTable.activeData).containsExactly("press")
        }

        it("deactivates all other symbol sets") {
            GuiActionRunner.execute {
                stringTable.data = listOf("receipt", "mine")
                stringTable.activeData = listOf("receipt")
                stringTable.activeData = listOf("mine")
            }

            assertThat(stringTable.activeData).containsExactly("mine")
        }
    }

    describe("activity column") {
        it("deactivates the symbol set if the activity cell is set to false") {
            GuiActionRunner.execute { stringTable.model.addRow(EditableDatum(true, "there")) }

            GuiActionRunner.execute { stringTable.model.setValueAt(false, 0, 0) }

            assertThat(stringTable.model.items[0].active).isFalse()
        }

        it("activates the symbol sets if the activity cell is set to true") {
            GuiActionRunner.execute { stringTable.model.addRow(EditableDatum(false, "flash")) }

            GuiActionRunner.execute { stringTable.model.setValueAt(true, 0, 0) }

            assertThat(stringTable.model.items[0].active).isTrue()
        }
    }


    describe("buttons") {
        describe("add") {
            it("adds a new row to the table") {
                val oldSize = stringTable.model.items.size

                GuiActionRunner.execute { frame.clickActionButton("Add") }

                assertThat(stringTable.model.items).hasSize(oldSize + 1)
                assertThat(stringTable.data.last()).isEqualTo(StringTable.DEFAULT_STRING)
            }
        }

        describe("copy") {
            it("copies a copyable element") {
                GuiActionRunner.execute { stringTable.data = listOf("sacred", "wealth") }

                GuiActionRunner.execute {
                    frame.table().target().setRowSelectionInterval(0, 0)
                    frame.clickActionButton("Copy")
                }

                assertThat(stringTable.data).containsExactly("sacred", "wealth", "sacred")
            }
        }
    }


    describe("addChangeListener") {
        var listenerInvoked = false


        beforeEachTest {
            listenerInvoked = false
            stringTable.addChangeListener { listenerInvoked = true }
        }


        it("invokes the listener when value is changed") {
            GuiActionRunner.execute { stringTable.data = listOf("cart", "wife") }
            listenerInvoked = false

            GuiActionRunner.execute { stringTable.model.setValueAt("deer", 0, 1) }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a row is added") {
            GuiActionRunner.execute { frame.clickActionButton("Add") }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener when a row is removed") {
            GuiActionRunner.execute { stringTable.data = listOf("sun", "sport") }
            listenerInvoked = false

            GuiActionRunner.execute {
                frame.table().target().setRowSelectionInterval(0, 0)
                frame.clickActionButton("Remove")
            }

            assertThat(listenerInvoked).isTrue()
        }
    }
})


/**
 * Dummy implementation that provides one column to edit a string in.
 */
private class StringTable : ActivityTableModelEditor<String>(arrayOf(DATA_COLUMN), DATA_EDITOR, "", "") {
    override fun createElement() = EditableDatum(DEFAULT_STATE, DEFAULT_STRING)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value in new rows in this table.
         */
        const val DEFAULT_STRING = "default"

        /**
         * The column showing the string of the datum.
         */
        private val DATA_COLUMN = object : ColumnInfo<EditableDatum<String>, String>("Data") {
            override fun getColumnClass() = String::class.java

            override fun valueOf(item: EditableDatum<String>) = item.datum

            override fun isCellEditable(item: EditableDatum<String>?) = true
        }

        /**
         * Describes how table rows are edited.
         */
        private val DATA_EDITOR = object : CollectionItemEditor<EditableDatum<String>> {
            override fun getItemClass() = EditableDatum(false, "")::class.java

            override fun clone(item: EditableDatum<String>, forInPlaceEditing: Boolean) =
                EditableDatum(item.active, item.datum)
        }
    }
}
