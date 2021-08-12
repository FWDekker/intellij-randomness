package com.fwdekker.randomness.decimal

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


    describe("loadScheme") {
        it("loads the settings' minimum value") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(minValue = 157.61, maxValue = 637.03)) }

            frame.spinner("minValue").requireValue(157.61)
        }

        it("loads the settings' maximum value") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(minValue = 212.79, maxValue = 408.68)) }

            frame.spinner("maxValue").requireValue(408.68)
        }

        it("loads the settings' decimal count") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(decimalCount = 18)) }

            frame.spinner("decimalCount").requireValue(18)
        }

        it("loads the settings' value for showing trailing zeroes") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(showTrailingZeroes = false)) }

            frame.checkBox("showTrailingZeroes").requireSelected(false)
        }

        it("loads the settings' grouping separator") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(groupingSeparator = "_")) }

            frame.radioButton("groupingSeparatorNone").requireSelected(false)
            frame.radioButton("groupingSeparatorPeriod").requireSelected(false)
            frame.radioButton("groupingSeparatorComma").requireSelected(false)
            frame.radioButton("groupingSeparatorUnderscore").requireSelected(true)
        }

        it("loads the settings' decimal separator") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(decimalSeparator = ".")) }

            frame.radioButton("decimalSeparatorComma").requireSelected(false)
            frame.radioButton("decimalSeparatorPeriod").requireSelected(true)
        }

        it("loads the settings' prefix") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(prefix = "x")) }

            frame.textBox("prefix").requireText("x")
        }

        it("loads the settings' suffix") {
            GuiActionRunner.execute { editor.loadScheme(DecimalScheme(suffix = "rough")) }

            frame.textBox("suffix").requireText("rough")
        }
    }

    describe("readScheme") {
        describe("defaults") {
            it("returns default grouping separator if no grouping separator is selected") {
                GuiActionRunner.execute { editor.loadScheme(DecimalScheme(groupingSeparator = "unsupported")) }

                assertThat(editor.readScheme().groupingSeparator).isEqualTo(DecimalScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("returns default decimal separator if no decimal separator is selected") {
                GuiActionRunner.execute { editor.loadScheme(DecimalScheme(decimalSeparator = "unsupported")) }

                assertThat(editor.readScheme().decimalSeparator).isEqualTo(DecimalScheme.DEFAULT_DECIMAL_SEPARATOR)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readScheme()).isEqualTo(editor.originalScheme)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 112.54
                frame.spinner("maxValue").target().value = 644.74
                frame.spinner("decimalCount").target().value = 485
                frame.checkBox("showTrailingZeroes").target().isSelected = false
                frame.radioButton("groupingSeparatorUnderscore").target().isSelected = true
                frame.radioButton("decimalSeparatorComma").target().isSelected = true
                frame.textBox("prefix").target().text = "exercise"
                frame.textBox("suffix").target().text = "court"
            }

            val readScheme = editor.readScheme()
            assertThat(readScheme.minValue).isEqualTo(112.54)
            assertThat(readScheme.maxValue).isEqualTo(644.74)
            assertThat(readScheme.decimalCount).isEqualTo(485)
            assertThat(readScheme.showTrailingZeroes).isEqualTo(false)
            assertThat(readScheme.groupingSeparator).isEqualTo("_")
            assertThat(readScheme.decimalSeparator).isEqualTo(",")
            assertThat(readScheme.prefix).isEqualTo("exercise")
            assertThat(readScheme.suffix).isEqualTo("court")
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.spinner("minValue").target().value = 112.54 }
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

            GuiActionRunner.execute { frame.spinner("minValue").target().value = 121.95 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
