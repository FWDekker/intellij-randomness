package com.fwdekker.randomness.template

import com.fwdekker.randomness.literal.LiteralScheme
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [TemplateNameEditor].
 */
object TemplateNameEditorTest : Spek({
    lateinit var frame: FrameFixture

    lateinit var template: Template
    lateinit var editor: TemplateNameEditor


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        template = Template()
        editor = GuiActionRunner.execute<TemplateNameEditor> { TemplateNameEditor(template) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("input handling") {
        it("trims the name when loading") {
            GuiActionRunner.execute { editor.loadState(Template(name = "  Homework ")) }

            frame.textBox("templateName").requireText("Homework")
        }

        it("trims the name when saving") {
            GuiActionRunner.execute { frame.textBox("templateName").target().text = " Tooth  " }

            assertThat(editor.readState().name).isEqualTo("Tooth")
        }
    }


    describe("loadScheme") {
        it("loads the template's name") {
            GuiActionRunner.execute { editor.loadState(Template(name = "Tin")) }

            frame.textBox("templateName").requireText("Tin")
        }
    }

    describe("readScheme") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute { frame.textBox("templateName").target().text = "Say" }

            assertThat(editor.readState().name).isEqualTo("Say")
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.textBox("templateName").target().text = "Alive" }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded scheme") {
            assertThat(editor.readState())
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }

        it("retains the scheme's schemes") {
            GuiActionRunner.execute { editor.loadState(Template(schemes = mutableListOf(LiteralScheme()))) }

            assertThat(editor.readState().schemes).containsExactlyElementsOf(editor.originalState.schemes)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.textBox("templateName").target().text = "Human" }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
