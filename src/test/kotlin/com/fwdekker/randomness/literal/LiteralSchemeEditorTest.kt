package com.fwdekker.randomness.literal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [LiteralSchemeEditor].
 */
object LiteralSchemeEditorTest : Spek({
    lateinit var scheme: LiteralScheme
    lateinit var editor: LiteralSchemeEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = LiteralScheme()
        editor = GuiActionRunner.execute<LiteralSchemeEditor> { LiteralSchemeEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loadScheme") {
        it("loads the scheme's literal") {
            GuiActionRunner.execute { editor.loadScheme(LiteralScheme(literal = "scrape")) }

            frame.textBox("literal").requireText("scrape")
        }
    }

    describe("readScheme") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.textBox("literal").target().text = "waste"
            }

            val readScheme = editor.readScheme()
            assertThat(readScheme.literal).isEqualTo("waste")
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.textBox("literal").target().text = "tie" }
            assertThat(editor.isModified()).isEqualTo(true)

            GuiActionRunner.execute { editor.loadScheme(editor.readScheme()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.textBox("literal").target().text = "boil" }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
