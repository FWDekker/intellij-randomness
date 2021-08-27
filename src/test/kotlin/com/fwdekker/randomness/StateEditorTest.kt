package com.fwdekker.randomness

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
    lateinit var frame: FrameFixture

    lateinit var scheme: DummyScheme
    lateinit var editor: DummySchemeEditor


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = DummyScheme.from("ashamed", "bathe")
        editor = GuiActionRunner.execute<DummySchemeEditor> { DummySchemeEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loadScheme") {
        it("writes the given scheme into the original scheme") {
            val newScheme = DummyScheme.from("satisfy", "faint")

            GuiActionRunner.execute { editor.loadState(newScheme) }

            assertThat(scheme.literals).containsExactly("satisfy", "faint")
            assertThat(scheme).isNotSameAs(newScheme)
        }
    }

    describe("readScheme") {
        it("returns an identical copy of the original scheme") {
            val readScheme = editor.readState()

            assertThat(readScheme.literals).containsExactly("ashamed", "bathe")
            assertThat(readScheme).isNotSameAs(scheme)
        }

        it("returns a copy that reflects changes in the editor without adusting the original scheme") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "reflect,cover" }

            assertThat(scheme.literals).containsExactly("ashamed", "bathe")
            assertThat(editor.readState().literals).containsExactly("reflect", "cover")
        }
    }

    describe("applyScheme") {
        it("writes changes into the original scheme") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "puzzle,once" }

            editor.applyState()

            assertThat(scheme.literals).containsExactly("puzzle", "once")
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

        it("ensures that applying the scheme does not affect the original scheme") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "father" }

            GuiActionRunner.execute { editor.reset() }
            editor.applyState()

            assertThat(scheme.literals).containsExactly("ashamed", "bathe")
        }
    }

    describe("doValidate") {
        it("returns null if the scheme is valid") {
            assertThat(editor.doValidate()).isNull()
        }

        it("returns an error message if the scheme is invalid") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = DummyScheme.INVALID_OUTPUT }

            assertThat(editor.doValidate()).isEqualTo("Invalid input!")
        }
    }
})
