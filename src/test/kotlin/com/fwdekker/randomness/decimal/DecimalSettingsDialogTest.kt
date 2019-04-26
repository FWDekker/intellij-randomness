package com.fwdekker.randomness.decimal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [DecimalSettingsDialog].
 */
object DecimalSettingsDialogTest : Spek({
    val defaultMinValue = 157.61
    val defaultMaxValue = 408.68
    val defaultDecimalCount = 5

    lateinit var decimalSettings: DecimalSettings
    lateinit var decimalSettingsDialog: DecimalSettingsDialog
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        decimalSettings = DecimalSettings()
        decimalSettings.minValue = defaultMinValue
        decimalSettings.maxValue = defaultMaxValue
        decimalSettings.decimalCount = defaultDecimalCount

        decimalSettingsDialog =
            GuiActionRunner.execute<DecimalSettingsDialog> { DecimalSettingsDialog(decimalSettings) }
        frame = showInFrame(decimalSettingsDialog.createCenterPanel())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the default minimum value") {
            frame.spinner("minValue").requireValue(defaultMinValue)
        }

        it("loads the default maximum value") {
            frame.spinner("maxValue").requireValue(defaultMaxValue)
        }

        it("loads the default decimal count") {
            frame.spinner("decimalCount").requireValue(defaultDecimalCount.toLong())
        }
    }

    describe("input handling") {
        it("truncates decimals in the decimal count") {
            GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 693.57f }

            frame.spinner("decimalCount").requireValue(693L)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            assertThat(decimalSettingsDialog.doValidate()).isNull()
        }

        describe("value range") {
            it("fails if the minimum value underflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = -1E54
                    frame.spinner("maxValue").target().value = -1E53
                }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minValue").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to -1.0E53.")
            }

            it("fails if the maximum value overflows") {
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = 1E54 }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 1.0E53.")
            }

            it("fails if the maximum value is greater than the minimum value") {
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = defaultMinValue - 1 }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
            }

            it("fails if the range size overflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = -1E53
                    frame.spinner("maxValue").target().value = 1E53
                }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The range should not exceed 1.0E53.")
            }
        }

        describe("decimal count") {
            it("passes if the decimal count is zero") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 0 }

                assertThat(decimalSettingsDialog.doValidate()).isNull()
            }

            it("fails if the decimal count is negative") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = -851 }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("decimalCount").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 0.")
            }

            it("fails if the decimal count overflows") {
                GuiActionRunner.execute {
                    frame.spinner("decimalCount").target().value = Integer.MAX_VALUE.toLong() + 1L
                }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("decimalCount").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 112.54
                frame.spinner("maxValue").target().value = 644.74
                frame.spinner("decimalCount").target().value = 485
                frame.radioButton("groupingSeparatorUnderscore").target().isSelected = true
                frame.radioButton("decimalSeparatorComma").target().isSelected = true
            }

            decimalSettingsDialog.saveSettings()

            assertThat(decimalSettings.minValue).isEqualTo(112.54)
            assertThat(decimalSettings.maxValue).isEqualTo(644.74)
            assertThat(decimalSettings.decimalCount).isEqualTo(485)
            assertThat(decimalSettings.groupingSeparator).isEqualTo('_')
            assertThat(decimalSettings.decimalSeparator).isEqualTo(',')
        }
    }
})
