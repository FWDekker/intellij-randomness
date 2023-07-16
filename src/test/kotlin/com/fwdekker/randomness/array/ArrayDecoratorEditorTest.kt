package com.fwdekker.randomness.array

import com.fwdekker.randomness.matcher
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.TitledSeparator
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import javax.swing.JCheckBox


/**
 * GUI tests for [ArrayDecoratorEditor].
 */
object ArrayDecoratorEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: ArrayDecorator
    lateinit var editor: ArrayDecoratorEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = ArrayDecorator(enabled = true)
        editor = GuiActionRunner.execute<ArrayDecoratorEditor> { ArrayDecoratorEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("separator visibility") {
        it("shows the separator by default") {
            frame.panel(matcher(TitledSeparator::class.java)).requireVisible()
        }

        it("hides the separator if the editor is embedded") {
            frame.cleanUp()
            editor = GuiActionRunner.execute<ArrayDecoratorEditor> { ArrayDecoratorEditor(scheme, embedded = true) }
            frame = showInFrame(editor.rootComponent)

            frame.panel(matcher(TitledSeparator::class.java)).requireNotVisible()
        }
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

            it("keeps components visible if the editor is embedded") {
                frame.cleanUp()
                editor = GuiActionRunner.execute<ArrayDecoratorEditor> { ArrayDecoratorEditor(scheme, embedded = true) }
                frame = showInFrame(editor.rootComponent)

                // This special matcher does not require the component to be visible
                frame.checkBox(matcher(JCheckBox::class.java) { it.name == "arrayEnabled" }).requireSelected()

                frame.spinner("arrayMinCount").requireEnabled()
            }

            it("disables space-after-separator if decorator is enabled but separator is a newline") {
                scheme.enabled = false
                scheme.separator = "\\n"
                GuiActionRunner.execute { editor.reset() }

                frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
            }
        }

        describe("toggles space-after-separator depending on separator") {
            describe("embedded") {
                beforeEach {
                    frame.cleanUp()
                    editor = GuiActionRunner.execute<ArrayDecoratorEditor> {
                        ArrayDecoratorEditor(scheme, embedded = true)
                    }
                    frame = showInFrame(editor.rootComponent)
                }


                it("disables space-after-separator if separator is a newline") {
                    GuiActionRunner.execute { frame.comboBox("arraySeparator").target().selectedItem = "\\n" }

                    frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
                }

                it("enables space-after-separator if separator is not a newline") {
                    GuiActionRunner.execute { frame.comboBox("arraySeparator").target().selectedItem = "," }

                    frame.checkBox("arraySpaceAfterSeparator").requireEnabled()
                }
            }

            describe("not embedded") {
                it("disables space-after-separator if separator is a newline") {
                    GuiActionRunner.execute { frame.comboBox("arraySeparator").target().selectedItem = "\\n" }

                    frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
                }

                it("enables space-after-separator if separator is not a newline") {
                    GuiActionRunner.execute { frame.comboBox("arraySeparator").target().selectedItem = "," }

                    frame.checkBox("arraySpaceAfterSeparator").requireEnabled()
                }

                it("disables space-after-separator if separator is not a newline, but decorator is disabled") {
                    scheme.enabled = false
                    scheme.separator = ","
                    GuiActionRunner.execute { editor.reset() }

                    frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
                }
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

            frame.comboBox("arrayBrackets").requireSelection("{@}")
        }

        it("loads the scheme's separator") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, separator = ";")) }

            frame.comboBox("arraySeparator").requireSelection(";")
        }

        it("loads the scheme's settings for using a space after separator") {
            GuiActionRunner.execute { editor.loadState(ArrayDecorator(enabled = true, isSpaceAfterSeparator = false)) }

            frame.checkBox("arraySpaceAfterSeparator").requireSelected(false)
        }
    }

    describe("readState") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.checkBox("arrayEnabled").target().isSelected = true
                frame.spinner("arrayMinCount").target().value = 642
                frame.spinner("arrayMaxCount").target().value = 876
                frame.comboBox("arrayBrackets").target().selectedItem = "{@}"
                frame.comboBox("arraySeparator").target().selectedItem = ";"
                frame.checkBox("arraySpaceAfterSeparator").target().isSelected = false
            }

            val readScheme = editor.readState()
            assertThat(readScheme.enabled).isTrue()
            assertThat(readScheme.minCount).isEqualTo(642)
            assertThat(readScheme.maxCount).isEqualTo(876)
            assertThat(readScheme.brackets).isEqualTo("{@}")
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

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 433 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
