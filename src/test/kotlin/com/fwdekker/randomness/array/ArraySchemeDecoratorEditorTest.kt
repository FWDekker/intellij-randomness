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
            GuiActionRunner.execute { frame.spinner("count").target().value = 983.24f }

            frame.spinner("count").requireValue(983)
        }

        describe("toggles space after separator depending on newline separator") {
            it("disables space after separator if newline separator is checked") {
                GuiActionRunner.execute { frame.radioButton("separatorNewline").target().isSelected = true }

                frame.checkBox("spaceAfterSeparator").requireDisabled()
            }

            it("enables space after separator if newline separator is unchecked") {
                GuiActionRunner.execute { frame.radioButton("separatorNewline").target().isSelected = false }

                frame.checkBox("spaceAfterSeparator").requireEnabled()
            }
        }
    }


    describe("loadScheme") {
        it("loads the settings' enabled state") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true)) }

            frame.checkBox("enabled").requireEnabled()
        }

        it("loads the settings' count") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true, count = 14)) }

            frame.spinner("count").requireValue(14)
        }

        it("loads the settings' brackets") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true, brackets = "{}")) }

            frame.radioButton("bracketsNone").requireSelected(false)
            frame.radioButton("bracketsSquare").requireSelected(false)
            frame.radioButton("bracketsCurly").requireSelected(true)
            frame.radioButton("bracketsRound").requireSelected(false)
        }

        it("loads the settings' separator") {
            GuiActionRunner.execute { editor.loadScheme(ArraySchemeDecorator(enabled = true, separator = ";")) }

            frame.radioButton("separatorComma").requireSelected(false)
            frame.radioButton("separatorSemicolon").requireSelected(true)
            frame.radioButton("separatorNewline").requireSelected(false)
        }

        it("loads the settings' settings for using a space after separator") {
            GuiActionRunner.execute {
                editor.loadScheme(ArraySchemeDecorator(enabled = true, isSpaceAfterSeparator = false))
            }

            frame.checkBox("spaceAfterSeparator").requireSelected(false)
        }
    }

    describe("readScheme") {
        describe("defaults") {
            it("returns default brackets if no brackets are selected") {
                GuiActionRunner.execute {
                    editor.loadScheme(ArraySchemeDecorator(enabled = true, brackets = "unsupported"))
                }

                assertThat(editor.readScheme().brackets).isEqualTo(ArraySchemeDecorator.DEFAULT_BRACKETS)
            }

            it("returns default separator if no separator is selected") {
                GuiActionRunner.execute {
                    editor.loadScheme(ArraySchemeDecorator(enabled = true, separator = "unsupported"))
                }

                assertThat(editor.readScheme().separator).isEqualTo(ArraySchemeDecorator.DEFAULT_SEPARATOR)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.checkBox("enabled").target().isSelected = true
                frame.spinner("count").target().value = 642
                frame.radioButton("bracketsCurly").target().isSelected = true
                frame.radioButton("separatorSemicolon").target().isSelected = true
                frame.checkBox("spaceAfterSeparator").target().isSelected = false
            }

            val readScheme = editor.readScheme()
            assertThat(readScheme.enabled).isEqualTo(true)
            assertThat(readScheme.count).isEqualTo(642)
            assertThat(readScheme.brackets).isEqualTo("{}")
            assertThat(readScheme.separator).isEqualTo(";")
            assertThat(readScheme.isSpaceAfterSeparator).isEqualTo(false)
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.checkBox("enabled").target().isSelected = false }
            assertThat(editor.isModified()).isEqualTo(true)

            GuiActionRunner.execute { editor.loadScheme(editor.readScheme()) }
            assertThat(editor.isModified()).isEqualTo(false)

            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("count").target().value = 433 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
