package com.fwdekker.randomness

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [StateEditor].
 */
object StateEditorTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var state: DummyScheme
    lateinit var editor: DummySchemeEditor


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        state = DummyScheme.from("ashamed", "bathe")
        editor = GuiActionRunner.execute<DummySchemeEditor> { DummySchemeEditor(state) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("writes the given state into the original state") {
            val newState = DummyScheme.from("satisfy", "faint")

            GuiActionRunner.execute { editor.loadState(newState) }

            assertThat(state.literals).containsExactly("satisfy", "faint")
            assertThat(state).isNotSameAs(newState)
        }
    }

    describe("readState") {
        it("returns an identical copy of the original state") {
            val readState = editor.readState()

            assertThat(readState.literals).containsExactly("ashamed", "bathe")
            assertThat(readState).isNotSameAs(state)
        }

        it("returns a copy that reflects changes in the editor without adusting the original state") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "reflect,cover" }

            assertThat(state.literals).containsExactly("ashamed", "bathe")
            assertThat(editor.readState().literals).containsExactly("reflect", "cover")
        }
    }

    describe("applyState") {
        it("writes changes into the original state") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "puzzle,once" }

            editor.applyState()

            assertThat(state.literals).containsExactly("puzzle", "once")
        }
    }

    describe("isModified") {
        it("returns false by default") {
            assertThat(editor.isModified()).isFalse()
        }

        it("returns true if the editor contains modifications") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "deafen" }

            assertThat(editor.isModified()).isTrue()
        }

        it("returns false if the modifications have been applied") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "sympathy,arch" }

            editor.applyState()

            assertThat(editor.isModified()).isFalse()
        }
    }

    describe("reset") {
        it("undoes changes in the editor") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "sympathy,arch" }

            GuiActionRunner.execute { editor.reset() }

            frame.textBox("literals").requireText("ashamed,bathe")
        }

        it("marks the editor as unmodified") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "admire" }

            GuiActionRunner.execute { editor.reset() }

            assertThat(editor.isModified()).isFalse()
        }

        it("ensures that applying the state does not affect the original state") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "father" }

            GuiActionRunner.execute { editor.reset() }
            editor.applyState()

            assertThat(state.literals).containsExactly("ashamed", "bathe")
        }
    }

    describe("doValidate") {
        it("returns null if the state is valid") {
            assertThat(editor.doValidate()).isNull()
        }

        it("returns an error message if the state is invalid") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = DummyScheme.INVALID_OUTPUT }

            assertThat(editor.doValidate()).isEqualTo("Invalid input!")
        }
    }
})
