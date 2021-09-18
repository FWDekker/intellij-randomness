package com.fwdekker.randomness.array

import com.fwdekker.randomness.nameMatcher
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.JCheckBox
import javax.swing.JTextArea


/**
 * GUI tests for [ArrayDecoratorEditor].
 */
object ArrayDecoratorEditorTest : Spek({
    lateinit var scheme: ArrayDecorator
    lateinit var editor: ArrayDecoratorEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = ArrayDecorator(enabled = true)
        editor = GuiActionRunner.execute<ArrayDecoratorEditor> { ArrayDecoratorEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("event handling") {
        it("truncates decimals in the minimum count") {
            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 983.24f }

            frame.spinner("arrayMinCount").requireValue(983)
        }

        it("truncates decimals in the maximum count") {
            GuiActionRunner.execute { frame.spinner("arrayMaxCount").target().value = 881.78f }

            frame.spinner("arrayMaxCount").requireValue(881)
        }

        describe("enabled state") {
            it("hides components if enabled is deselected") {
                GuiActionRunner.execute { frame.checkBox("arrayEnabled").target().isSelected = false }

                frame.spinner("arrayMinCount").requireDisabled()
            }

            it("shows components if enabled is reselected") {
                GuiActionRunner.execute {
                    frame.checkBox("arrayEnabled").target().isSelected = false
                    frame.checkBox("arrayEnabled").target().isSelected = true
                }

                frame.spinner("arrayMinCount").requireEnabled()
            }

            it("keeps components visible if the editor is not disablable") {
                frame.cleanUp()
                editor = GuiActionRunner.execute<ArrayDecoratorEditor> {
                    ArrayDecoratorEditor(scheme, disablable = false)
                }
                frame = showInFrame(editor.rootComponent)

                GuiActionRunner.execute {
                    frame.checkBox(nameMatcher(JCheckBox::class.java, "arrayEnabled")).target().isSelected = false
                }

                frame.spinner("arrayMinCount").requireEnabled()
            }
        }

        describe("helpText") {
            it("hides the helpTextArea by default") {
                frame.textBox(nameMatcher(JTextArea::class.java, "helpText")).requireNotVisible()
            }

            it("shows the helpTextArea if a helpText is given") {
                frame.cleanUp()
                editor = GuiActionRunner.execute<ArrayDecoratorEditor> {
                    ArrayDecoratorEditor(scheme, helpText = "Sorrow")
                }
                frame = showInFrame(editor.rootComponent)

                frame.textBox("helpText")
                    .requireVisible()
                    .requireText("Sorrow")
            }
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
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true)) }

            frame.checkBox("arrayEnabled").requireEnabled()
        }

        it("loads the scheme's minimum count") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, minCount = 2)) }

            frame.spinner("arrayMinCount").requireValue(2)
        }

        it("loads the scheme's maximum count") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, maxCount = 14)) }

            frame.spinner("arrayMaxCount").requireValue(14)
        }

        it("loads the scheme's brackets") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, brackets = "{}")) }

            frame.radioButton("arrayBracketsNone").requireSelected(false)
            frame.radioButton("arrayBracketsSquare").requireSelected(false)
            frame.radioButton("arrayBracketsCurly").requireSelected(true)
            frame.radioButton("arrayBracketsRound").requireSelected(false)
        }

        it("loads the scheme's separator") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, separator = ";")) }

            frame.radioButton("arraySeparatorComma").requireSelected(false)
            frame.radioButton("arraySeparatorSemicolon").requireSelected(true)
            frame.radioButton("arraySeparatorNewline").requireSelected(false)
        }

        it("loads the scheme's settings for using a space after separator") {
            GuiActionRunner.execute {
                editor.loadState(ArrayDecorator(enabled = true, isSpaceAfterSeparator = false))
            }

            frame.checkBox("arraySpaceAfterSeparator").requireSelected(false)
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default brackets if no brackets are selected") {
                GuiActionRunner.execute {
                    editor.loadState(ArrayDecorator(enabled = true, brackets = "unsupported"))
                }

                assertThat(editor.readState().brackets).isEqualTo(ArrayDecorator.DEFAULT_BRACKETS)
            }

            it("returns default separator if no separator is selected") {
                GuiActionRunner.execute {
                    editor.loadState(ArrayDecorator(enabled = true, separator = "unsupported"))
                }

                assertThat(editor.readState().separator).isEqualTo(ArrayDecorator.DEFAULT_SEPARATOR)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.checkBox("arrayEnabled").target().isSelected = true
                frame.spinner("arrayMinCount").target().value = 642
                frame.spinner("arrayMaxCount").target().value = 876
                frame.radioButton("arrayBracketsCurly").target().isSelected = true
                frame.radioButton("arraySeparatorSemicolon").target().isSelected = true
                frame.checkBox("arraySpaceAfterSeparator").target().isSelected = false
            }

            val readScheme = editor.readState()
            assertThat(readScheme.enabled).isTrue()
            assertThat(readScheme.minCount).isEqualTo(642)
            assertThat(readScheme.maxCount).isEqualTo(876)
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

            GuiActionRunner.execute { frame.spinner("arrayMaxCount").target().value = 433 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
