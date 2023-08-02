package com.fwdekker.randomness.template

import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.integer.IntegerScheme
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [TemplateEditor].
 */
object TemplateEditorTest : DescribeSpec({
    lateinit var frame: FrameFixture

    lateinit var template: Template
    lateinit var editor: TemplateEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        template = Template()
        editor = guiGet { TemplateEditor(template) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
    }


    describe("input handling") {
        it("trims the name when loading") {
            guiRun { editor.loadState(Template("  Homework ")) }

            frame.textBox("templateName").requireText("Homework")
        }

        it("trims the name when saving") {
            guiRun { frame.textBox("templateName").target().text = " Tooth  " }

            assertThat(editor.readState().name).isEqualTo("Tooth")
        }
    }


    describe("loadState") {
        it("loads the template's name") {
            guiRun { editor.loadState(Template("Tin")) }

            frame.textBox("templateName").requireText("Tin")
        }
    }

    describe("readState") {
        it("returns a template with a disabled array decorator") {
            guiRun { editor.loadState(Template(arrayDecorator = ArrayDecorator(enabled = true))) }

            assertThat(editor.readState().arrayDecorator.enabled).isFalse()
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            guiRun { frame.textBox("templateName").target().text = "Say" }

            assertThat(editor.readState().name).isEqualTo("Say")
        }

        it("returns the loaded state if no editor changes are made") {
            guiRun { frame.textBox("templateName").target().text = "Alive" }
            assertThat(editor.isModified()).isTrue()

            guiRun { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded template") {
            assertThat(editor.readState())
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
        }

        it("retains the template's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }

        it("retains the template's schemes") {
            guiRun { editor.loadState(Template(schemes = listOf(IntegerScheme()))) }

            assertThat(editor.readState().schemes).containsExactlyElementsOf(editor.originalState.schemes)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.textBox("templateName").target().text = "Human" }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
