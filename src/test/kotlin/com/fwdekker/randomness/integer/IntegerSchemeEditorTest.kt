package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArraySchemeDecorator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [IntegerSchemeEditor].
 */
object IntegerSchemeEditorTest : Spek({
    lateinit var scheme: IntegerScheme
    lateinit var editor: IntegerSchemeEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = IntegerScheme()
        editor = GuiActionRunner.execute<IntegerSchemeEditor> { IntegerSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }



    describe("input handling") {
        describe("base") {
            it("truncates decimals in the base") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 22.62f }

                frame.spinner("base").requireValue(22)
            }
        }

        describe("minimum value") {
            it("truncates decimals in the minimum value") {
                GuiActionRunner.execute { frame.spinner("minValue").target().value = 285.21f }

                frame.spinner("minValue").requireValue(285L)
            }
        }

        describe("maximum value") {
            it("truncates decimals in the maximum value") {
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = 490.34f }

                frame.spinner("maxValue").requireValue(490L)
            }
        }
    }


    describe("loadState") {
        it("loads the scheme's minimum value") {
            GuiActionRunner.execute { editor.loadState(IntegerScheme(minValue = 145L, maxValue = 341L)) }

            frame.spinner("minValue").requireValue(145L)
        }

        it("loads the scheme's maximum value") {
            GuiActionRunner.execute { editor.loadState(IntegerScheme(minValue = 337L, maxValue = 614L)) }

            frame.spinner("maxValue").requireValue(614L)
        }

        it("loads the scheme's base value") {
            GuiActionRunner.execute { editor.loadState(IntegerScheme(base = 25)) }

            frame.spinner("base").requireValue(25)
        }

        it("loads the scheme's grouping separator") {
            GuiActionRunner.execute { editor.loadState(IntegerScheme(groupingSeparator = "_")) }

            frame.radioButton("groupingSeparatorNone").requireSelected(false)
            frame.radioButton("groupingSeparatorPeriod").requireSelected(false)
            frame.radioButton("groupingSeparatorComma").requireSelected(false)
            frame.radioButton("groupingSeparatorUnderscore").requireSelected(true)
        }

        it("loads the scheme's capitalization mode") {
            GuiActionRunner.execute { editor.loadState(IntegerScheme(capitalization = CapitalizationMode.LOWER)) }

            frame.radioButton("capitalizationLower").requireSelected(true)
            frame.radioButton("capitalizationUpper").requireSelected(false)
        }

        it("loads the scheme's prefix") {
            GuiActionRunner.execute { editor.loadState(IntegerScheme(prefix = "wage")) }

            frame.textBox("prefix").requireText("wage")
        }

        it("loads the scheme's suffix") {
            GuiActionRunner.execute { editor.loadState(IntegerScheme(suffix = "fat")) }

            frame.textBox("suffix").requireText("fat")
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default brackets if no brackets are selected") {
                GuiActionRunner.execute { editor.loadState(IntegerScheme(groupingSeparator = "unsupported")) }

                assertThat(editor.readState().groupingSeparator).isEqualTo(IntegerScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("returns default brackets if no brackets are selected") {
                GuiActionRunner.execute { editor.loadState(IntegerScheme(capitalization = CapitalizationMode.DUMMY)) }

                assertThat(editor.readState().capitalization).isEqualTo(IntegerScheme.DEFAULT_CAPITALIZATION)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 2_147_483_648L
                frame.spinner("maxValue").target().value = 2_147_483_649L
                frame.spinner("base").target().value = 14
                frame.radioButton("groupingSeparatorPeriod").target().isSelected = true
                frame.radioButton("capitalizationUpper").target().isSelected = true
                frame.textBox("prefix").target().text = "silent"
                frame.textBox("suffix").target().text = "pain"
            }

            val readScheme = editor.readState()
            assertThat(readScheme.minValue).isEqualTo(2_147_483_648L)
            assertThat(readScheme.maxValue).isEqualTo(2_147_483_649L)
            assertThat(readScheme.base).isEqualTo(14)
            assertThat(readScheme.groupingSeparator).isEqualTo(".")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(readScheme.prefix).isEqualTo("silent")
            assertThat(readScheme.suffix).isEqualTo("pain")
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.spinner("minValue").target().value = 242L }
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
            assertThat(readState.decorator)
                .isEqualTo(editor.originalState.decorator)
                .isNotSameAs(editor.originalState.decorator)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("minValue").target().value = 76L }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(IntegerScheme(decorator = ArraySchemeDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
