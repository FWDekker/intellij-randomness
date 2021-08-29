package com.fwdekker.randomness.array

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [ArraySchemeDecoratorEditor].
 */
object ArraySchemeDecoratorEditorTest : Spek({
    lateinit var scheme: ArraySchemeDecorator
    lateinit var editor: ArraySchemeDecoratorEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = ArraySchemeDecorator(enabled = true)
        editor = GuiActionRunner.execute<ArraySchemeDecoratorEditor> { ArraySchemeDecoratorEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("event handling") {
        it("truncates decimals in the count") {
            GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 983.24f }

            frame.spinner("arrayCount").requireValue(983)
        }

        describe("toggles space after separator depending on newline separator") {
            it("disables space after separator if newline separator is checked") {
                GuiActionRunner.execute { frame.radioButton("arraySeparatorNewline").target().isSelected = true }

                frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
            }

            it("enables space after separator if newline separator is unchecked") {
                GuiActionRunner.execute { frame.radioButton("arraySeparatorNewline").target().isSelected = false }

                frame.checkBox("arraySpaceAfterSeparator").requireEnabled()
            }
        }
    }


    describe("loadState") {
        it("loads the scheme's enabled state") {
            GuiActionRunner.execute { editor.loadState(ArraySchemeDecorator(enabled = true)) }

            frame.checkBox("arrayEnabled").requireEnabled()
        }

        it("loads the scheme's count") {
            GuiActionRunner.execute { editor.loadState(ArraySchemeDecorator(enabled = true, count = 14)) }

            frame.spinner("arrayCount").requireValue(14)
        }

        it("loads the scheme's brackets") {
            GuiActionRunner.execute { editor.loadState(ArraySchemeDecorator(enabled = true, brackets = "{}")) }

            frame.radioButton("arrayBracketsNone").requireSelected(false)
            frame.radioButton("arrayBracketsSquare").requireSelected(false)
            frame.radioButton("arrayBracketsCurly").requireSelected(true)
            frame.radioButton("arrayBracketsRound").requireSelected(false)
        }

        it("loads the scheme's separator") {
            GuiActionRunner.execute { editor.loadState(ArraySchemeDecorator(enabled = true, separator = ";")) }

            frame.radioButton("arraySeparatorComma").requireSelected(false)
            frame.radioButton("arraySeparatorSemicolon").requireSelected(true)
            frame.radioButton("arraySeparatorNewline").requireSelected(false)
        }

        it("loads the scheme's settings for using a space after separator") {
            GuiActionRunner.execute {
                editor.loadState(ArraySchemeDecorator(enabled = true, isSpaceAfterSeparator = false))
            }

            frame.checkBox("arraySpaceAfterSeparator").requireSelected(false)
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default brackets if no brackets are selected") {
                GuiActionRunner.execute {
                    editor.loadState(ArraySchemeDecorator(enabled = true, brackets = "unsupported"))
                }

                assertThat(editor.readState().brackets).isEqualTo(ArraySchemeDecorator.DEFAULT_BRACKETS)
            }

            it("returns default separator if no separator is selected") {
                GuiActionRunner.execute {
                    editor.loadState(ArraySchemeDecorator(enabled = true, separator = "unsupported"))
                }

                assertThat(editor.readState().separator).isEqualTo(ArraySchemeDecorator.DEFAULT_SEPARATOR)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.checkBox("arrayEnabled").target().isSelected = true
                frame.spinner("arrayCount").target().value = 642
                frame.radioButton("arrayBracketsCurly").target().isSelected = true
                frame.radioButton("arraySeparatorSemicolon").target().isSelected = true
                frame.checkBox("arraySpaceAfterSeparator").target().isSelected = false
            }

            val readScheme = editor.readState()
            assertThat(readScheme.enabled).isTrue()
            assertThat(readScheme.count).isEqualTo(642)
            assertThat(readScheme.brackets).isEqualTo("{}")
            assertThat(readScheme.separator).isEqualTo(";")
            assertThat(readScheme.isSpaceAfterSeparator).isFalse()
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.checkBox("arrayEnabled").target().isSelected = false }
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
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 433 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
