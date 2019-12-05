package com.fwdekker.randomness.ui

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.util.ui.CollectionItemEditor
import org.assertj.core.api.Assertions
import org.assertj.swing.edt.GuiActionRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe


/**
 * Unit tests for [ActivityTableModelEditor].
 */
object ActivityTableModelEditorTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var modelEditor: ActivityTableModelEditor<String>


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        val itemEditor = object : CollectionItemEditor<EditableDatum<String>> {
            // TODO Do not instantiate instance of `EditableSymbolSet`
            override fun getItemClass() = EditableDatum(false, "")::class.java

            override fun clone(item: EditableDatum<String>, forInPlaceEditing: Boolean) =
                EditableDatum(item.active, item.datum)
        }
        modelEditor = GuiActionRunner.execute<ActivityTableModelEditor<String>> {
            object : ActivityTableModelEditor<String>(arrayOf(), itemEditor, "") {}
        }
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("data") {
        it("returns an empty list by default") {
            Assertions.assertThat(modelEditor.data).isEmpty()
        }

        it("adds entries to the underlying model") {
            GuiActionRunner.execute { modelEditor.data = listOf("overflow") }

            Assertions.assertThat(modelEditor.model.items)
                .containsExactly(EditableDatum(ActivityTableModelEditor.DEFAULT_STATE, "overflow"))
        }

        it("removes entries from the underlying model") {
            GuiActionRunner.execute {
                modelEditor.data = listOf("bucket")
                modelEditor.data = listOf()
            }

            Assertions.assertThat(modelEditor.model.items).isEmpty()
        }

        it("returns items that were added directly to the model") {
            GuiActionRunner.execute { modelEditor.model.addRow(EditableDatum(false, "hole")) }

            Assertions.assertThat(modelEditor.data).containsExactly("hole")
        }
    }

    describe("activeData") {
        it("returns an empty list by default") {
            Assertions.assertThat(modelEditor.activeData).isEmpty()
        }

        it("returns only active symbol sets") {
            GuiActionRunner.execute {
                modelEditor.model.addRow(EditableDatum(false, "passage"))
                modelEditor.model.addRow(EditableDatum(true, "limit"))
            }

            Assertions.assertThat(modelEditor.activeData).containsExactly("limit")
        }

        it("activates the given symbol sets") {
            GuiActionRunner.execute {
                modelEditor.data = listOf("press", "thin")
                modelEditor.activeData = listOf("press")
            }

            Assertions.assertThat(modelEditor.activeData).containsExactly("press")
        }

        it("deactivates all other symbol sets") {

            GuiActionRunner.execute {
                modelEditor.data = listOf("receipt", "mine")
                modelEditor.activeData = listOf("receipt")
                modelEditor.activeData = listOf("mine")
            }

            Assertions.assertThat(modelEditor.activeData).containsExactly("mine")
        }
    }

    describe("activity column") {
        it("deactivates the symbol set if the activity cell is set to false") {
            GuiActionRunner.execute { modelEditor.model.addRow(EditableDatum(true, "there")) }

            GuiActionRunner.execute { modelEditor.model.setValueAt(false, 0, 0) }

            Assertions.assertThat(modelEditor.model.items[0].active).isFalse()
        }

        it("activates the symbol sets if the activity cell is set to true") {
            GuiActionRunner.execute { modelEditor.model.addRow(EditableDatum(false, "flash")) }

            GuiActionRunner.execute { modelEditor.model.setValueAt(true, 0, 0) }

            Assertions.assertThat(modelEditor.model.items[0].active).isTrue()
        }
    }

    // TODO: Copy functionality not accessible from tests
    xdescribe("copying") {
        it("copies a copyable element") {}

        it("copies copyable elements") {}

        it("does not copy an uncopyable element") {}

        it("does not copy uncopyable elements") {}

        it("does not copy a mixture of copyable and uncopyable elements") {}
    }
})
