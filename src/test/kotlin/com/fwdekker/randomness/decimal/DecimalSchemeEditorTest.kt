package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.array.ArrayDecorator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [DecimalSchemeEditor].
 */
object DecimalSchemeEditorTest : Spek({
    lateinit var scheme: DecimalScheme
    lateinit var editor: DecimalSchemeEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        scheme = DecimalScheme()
        editor = GuiActionRunner.execute<DecimalSchemeEditor> { DecimalSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }



    describe("input handling") {
        it("truncates decimals in the decimal count") {
            GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 693.57f }

            frame.spinner("decimalCount").requireValue(693)
        }
    }


    describe("loadState") {
        it("loads the scheme's minimum value") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(minValue = 157.61, maxValue = 637.03)) }

            frame.spinner("minValue").requireValue(157.61)
        }

        it("loads the scheme's maximum value") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(minValue = 212.79, maxValue = 408.68)) }

            frame.spinner("maxValue").requireValue(408.68)
        }

        it("loads the scheme's decimal count") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(decimalCount = 18)) }

            frame.spinner("decimalCount").requireValue(18)
        }

        it("loads the scheme's value for showing trailing zeroes") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(showTrailingZeroes = false)) }

            frame.checkBox("showTrailingZeroes").requireSelected(false)
        }

        it("loads the scheme's grouping separator") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(groupingSeparator = "_")) }

            frame.radioButton("groupingSeparatorNone").requireSelected(false)
            frame.radioButton("groupingSeparatorPeriod").requireSelected(false)
            frame.radioButton("groupingSeparatorComma").requireSelected(false)
            frame.radioButton("groupingSeparatorUnderscore").requireSelected(true)
            frame.panel("groupingSeparatorCustom").radioButton().requireSelected(false)
        }

        it("loads the scheme's custom grouping separator") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(customGroupingSeparator = "c")) }

            frame.panel("groupingSeparatorCustom").textBox().requireText("c")
        }

        it("selects the scheme's custom grouping separator") {
            GuiActionRunner.execute {
                editor.loadState(DecimalScheme(groupingSeparator = "s", customGroupingSeparator = "s"))
            }

            frame.panel("groupingSeparatorCustom").radioButton().requireSelected()
        }

        it("loads the scheme's decimal separator") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(decimalSeparator = ".")) }

            frame.radioButton("decimalSeparatorComma").requireSelected(false)
            frame.radioButton("decimalSeparatorPeriod").requireSelected(true)
            frame.panel("decimalSeparatorCustom").radioButton().requireSelected(false)
        }

        it("loads the scheme's custom decimal separator") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(customDecimalSeparator = "j")) }

            frame.panel("decimalSeparatorCustom").textBox().requireText("j")
        }

        it("selects the scheme's custom decimal separator") {
            GuiActionRunner.execute {
                editor.loadState(DecimalScheme(decimalSeparator = "o", customDecimalSeparator = "o"))
            }

            frame.panel("decimalSeparatorCustom").radioButton().requireSelected()
        }

        it("loads the scheme's prefix") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(prefix = "x")) }

            frame.textBox("prefix").requireText("x")
        }

        it("loads the scheme's suffix") {
            GuiActionRunner.execute { editor.loadState(DecimalScheme(suffix = "rough")) }

            frame.textBox("suffix").requireText("rough")
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default grouping separator if no grouping separator is selected") {
                GuiActionRunner.execute { editor.loadState(DecimalScheme(groupingSeparator = "unsupported")) }

                assertThat(editor.readState().groupingSeparator).isEqualTo(DecimalScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("returns default decimal separator if no decimal separator is selected") {
                GuiActionRunner.execute { editor.loadState(DecimalScheme(decimalSeparator = "unsupported")) }

                assertThat(editor.readState().decimalSeparator).isEqualTo(DecimalScheme.DEFAULT_DECIMAL_SEPARATOR)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 112.54
                frame.spinner("maxValue").target().value = 644.74
                frame.spinner("decimalCount").target().value = 485
                frame.checkBox("showTrailingZeroes").target().isSelected = false
                frame.radioButton("groupingSeparatorUnderscore").target().isSelected = true
                frame.panel("groupingSeparatorCustom").textBox().target().text = "u"
                frame.radioButton("decimalSeparatorComma").target().isSelected = true
                frame.panel("decimalSeparatorCustom").textBox().target().text = "p"
                frame.textBox("prefix").target().text = "exercise"
                frame.textBox("suffix").target().text = "court"
            }

            val readScheme = editor.readState()
            assertThat(readScheme.minValue).isEqualTo(112.54)
            assertThat(readScheme.maxValue).isEqualTo(644.74)
            assertThat(readScheme.decimalCount).isEqualTo(485)
            assertThat(readScheme.showTrailingZeroes).isFalse()
            assertThat(readScheme.groupingSeparator).isEqualTo("_")
            assertThat(readScheme.customGroupingSeparator).isEqualTo("u")
            assertThat(readScheme.decimalSeparator).isEqualTo(",")
            assertThat(readScheme.customDecimalSeparator).isEqualTo("p")
            assertThat(readScheme.prefix).isEqualTo("exercise")
            assertThat(readScheme.suffix).isEqualTo("court")
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.spinner("minValue").target().value = 112.54 }
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

            GuiActionRunner.execute { frame.spinner("minValue").target().value = 121.95 }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(DecimalScheme(arrayDecorator = ArrayDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
