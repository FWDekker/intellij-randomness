package com.fwdekker.randomness.literal

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArrayDecorator
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


    describe("loadState") {
        it("loads the scheme's literal") {
            GuiActionRunner.execute { editor.loadState(LiteralScheme(literal = "scrape")) }

            frame.textBox("literal").requireText("scrape")
        }

        it("loads the scheme's capitalization") {
            GuiActionRunner.execute { editor.loadState(LiteralScheme(capitalization = CapitalizationMode.RANDOM)) }

            frame.radioButton("capitalizationRetain").requireSelected(false)
            frame.radioButton("capitalizationRandom").requireSelected(true)
        }
    }

    describe("readState") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.textBox("literal").target().text = "waste"
                frame.radioButton("capitalizationRandom").target().isSelected = true
            }

            val readScheme = editor.readState()
            assertThat(readScheme.literal).isEqualTo("waste")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.RANDOM)
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.textBox("literal").target().text = "tie" }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded scheme") {
            val readState = editor.readState()

            assertThat(readState)
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
            assertThat(readState.arrayDecorator)
                .isEqualTo(editor.originalState.arrayDecorator)
                .isNotSameAs(editor.originalState.arrayDecorator)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.textBox("literal").target().text = "boil" }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(LiteralScheme(arrayDecorator = ArrayDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
