package com.fwdekker.randomness.array

import com.fwdekker.randomness.matcher
import com.fwdekker.randomness.nameMatcher
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.TitledSeparator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.JCheckBox


/**
 * GUI tests for [ArrayDecoratorEditor].
 */
object ArrayDecoratorEditorTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: ArrayDecorator
    lateinit var editor: ArrayDecoratorEditor


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = ArrayDecorator(enabled = true)
        editor = GuiActionRunner.execute<ArrayDecoratorEditor> { ArrayDecoratorEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
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
            it("disables components if enabled is deselected") {
                GuiActionRunner.execute { frame.checkBox("arrayEnabled").target().isSelected = false }

                frame.spinner("arrayMinCount").requireDisabled()
            }

            it("enables components if enabled is reselected") {
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

            it("disables space after separator if the decorator is enabled but the newline separator is checked") {
                scheme.enabled = false
                scheme.separator = "\n"
                GuiActionRunner.execute { editor.reset() }

                frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
            }

            it("does not enable the text box of `customBrackets` if the radio button is not selected") {
                GuiActionRunner.execute {
                    frame.checkBox("arrayEnabled").target().isSelected = false
                    frame.checkBox("arrayEnabled").target().isSelected = true
                }

                frame.panel("arrayBracketsCustom").textBox().requireDisabled()
            }
        }

        describe("showSeparator") {
            it("shows the separator by default") {
                frame.panel(matcher(TitledSeparator::class.java) { true }).requireVisible()
            }

            it("hides the separator if desired") {
                frame.cleanUp()
                editor = GuiActionRunner.execute<ArrayDecoratorEditor> {
                    ArrayDecoratorEditor(scheme, showSeparator = false)
                }
                frame = showInFrame(editor.rootComponent)

                frame.panel(matcher(TitledSeparator::class.java) { true }).requireNotVisible()
            }
        }

        describe("toggles space-after-separator depending on newline separator") {
            it("enables space-after-separator if not disablable and newline separator is checked") {
                frame.cleanUp()
                editor = GuiActionRunner.execute<ArrayDecoratorEditor> {
                    ArrayDecoratorEditor(scheme, disablable = false)
                }
                frame = showInFrame(editor.rootComponent)

                GuiActionRunner.execute { frame.radioButton("arraySeparatorNewline").target().isSelected = true }

                frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
            }

            it("enables space-after-separator if not disablable and newline separator is unchecked") {
                frame.cleanUp()
                editor = GuiActionRunner.execute<ArrayDecoratorEditor> {
                    ArrayDecoratorEditor(scheme, disablable = false)
                }
                frame = showInFrame(editor.rootComponent)

                frame.checkBox("arraySpaceAfterSeparator").requireEnabled()
            }

            it("disables space-after-separator if newline separator is checked") {
                GuiActionRunner.execute { frame.radioButton("arraySeparatorNewline").target().isSelected = true }

                frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
            }

            it("enables space-after-separator if newline separator is unchecked") {
                GuiActionRunner.execute { frame.radioButton("arraySeparatorNewline").target().isSelected = false }

                frame.checkBox("arraySpaceAfterSeparator").requireEnabled()
            }

            it("disables space-after-separator if the newline separator is unchecked but the decorator is disabled") {
                scheme.enabled = false
                scheme.separator = ","
                GuiActionRunner.execute { editor.reset() }

                frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
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
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, brackets = "{@}")) }

            frame.radioButton("arrayBracketsNone").requireSelected(false)
            frame.radioButton("arrayBracketsSquare").requireSelected(false)
            frame.radioButton("arrayBracketsCurly").requireSelected(true)
            frame.radioButton("arrayBracketsRound").requireSelected(false)
            frame.panel("arrayBracketsCustom").radioButton().requireSelected(false)
        }

        it("loads the scheme's custom brackets") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, customBrackets = "a@b")) }

            frame.panel("arrayBracketsCustom").textBox().requireText("a@b")
        }

        it("selects the scheme's custom brackets") {
            GuiActionRunner.execute {
                editor.loadState(ArrayDecorator(enabled = true, brackets = "a@b", customBrackets = "a@b"))
            }

            frame.panel("arrayBracketsCustom").radioButton().requireSelected()
        }

        it("loads the scheme's separator") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, separator = ";")) }

            frame.radioButton("arraySeparatorComma").requireSelected(false)
            frame.radioButton("arraySeparatorSemicolon").requireSelected(true)
            frame.radioButton("arraySeparatorNewline").requireSelected(false)
            frame.panel("arraySeparatorCustom").radioButton().requireSelected(false)
        }

        it("loads the scheme's custom separator") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, customSeparator = "fashion")) }

            frame.panel("arraySeparatorCustom").textBox().requireText("fashion")
        }

        it("selects the scheme's custom separator") {
            GuiActionRunner.execute {
                editor.loadState(ArrayDecorator(enabled = true, separator = "steady", customSeparator = "steady"))
            }

            frame.panel("arraySeparatorCustom").radioButton().requireSelected()
        }

        it("loads the scheme's settings for using a space after separator") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, isSpaceAfterSeparator = false)) }

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
                frame.panel("arrayBracketsCustom").textBox().target().text = "y@v"
                frame.radioButton("arraySeparatorSemicolon").target().isSelected = true
                frame.panel("arraySeparatorCustom").textBox().target().text = "prb"
                frame.checkBox("arraySpaceAfterSeparator").target().isSelected = false
            }

            val readScheme = editor.readState()
            assertThat(readScheme.enabled).isTrue()
            assertThat(readScheme.minCount).isEqualTo(642)
            assertThat(readScheme.maxCount).isEqualTo(876)
            assertThat(readScheme.brackets).isEqualTo("{@}")
            assertThat(readScheme.customBrackets).isEqualTo("y@v")
            assertThat(readScheme.separator).isEqualTo(";")
            assertThat(readScheme.customSeparator).isEqualTo("prb")
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

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 433 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
