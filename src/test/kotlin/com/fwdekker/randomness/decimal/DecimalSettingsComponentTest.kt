package com.fwdekker.randomness.decimal

import com.intellij.openapi.options.ConfigurationException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [DecimalSettingsComponent].
 */
object DecimalSettingsComponentTest : Spek({
    lateinit var decimalSettings: DecimalSettings
    lateinit var decimalSettingsComponent: DecimalSettingsComponent
    lateinit var decimalSettingsComponentConfigurable: DecimalSettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        decimalSettings = DecimalSettings()
        decimalSettings.minValue = 157.61
        decimalSettings.maxValue = 408.68
        decimalSettings.decimalCount = 5
        decimalSettings.showTrailingZeroes = false
        decimalSettings.groupingSeparator = "_"
        decimalSettings.decimalSeparator = "."

        decimalSettingsComponent =
            GuiActionRunner.execute<DecimalSettingsComponent> { DecimalSettingsComponent(decimalSettings) }
        decimalSettingsComponentConfigurable = DecimalSettingsConfigurable(decimalSettingsComponent)
        frame = showInFrame(decimalSettingsComponent.getRootPane())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' minimum value") {
            frame.spinner("minValue").requireValue(157.61)
        }

        it("loads the settings' maximum value") {
            frame.spinner("maxValue").requireValue(408.68)
        }

        it("loads the settings' decimal count") {
            frame.spinner("decimalCount").requireValue(5)
        }

        it("loads the settings' value for showing trailing zeroes") {
            frame.checkBox("showTrailingZeroes").requireSelected(false)
        }

        it("loads the settings' grouping separator") {
            frame.radioButton("groupingSeparatorNone").requireSelected(false)
            frame.radioButton("groupingSeparatorPeriod").requireSelected(false)
            frame.radioButton("groupingSeparatorComma").requireSelected(false)
            frame.radioButton("groupingSeparatorUnderscore").requireSelected(true)
        }

        it("loads the settings' decimal separator") {
            frame.radioButton("decimalSeparatorComma").requireSelected(false)
            frame.radioButton("decimalSeparatorPeriod").requireSelected(true)
        }
    }

    describe("input handling") {
        it("truncates decimals in the decimal count") {
            GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 693.57f }

            frame.spinner("decimalCount").requireValue(693)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { decimalSettingsComponent.loadSettings(DecimalSettings()) }

            assertThat(decimalSettingsComponent.doValidate()).isNull()
        }

        describe("value range") {
            it("fails if the minimum value is greater than the maximum value") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = 46.16
                    frame.spinner("maxValue").target().value = 45.16
                }

                val validationInfo = decimalSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should not be smaller than the minimum.")
            }

            it("fails if the range size overflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = -1E53
                    frame.spinner("maxValue").target().value = 1E53
                }

                val validationInfo = decimalSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The range should not exceed 1.0E53.")
            }
        }

        describe("decimal count") {
            it("passes if the decimal count is zero") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 0 }

                assertThat(decimalSettingsComponent.doValidate()).isNull()
            }

            it("fails if the decimal count is negative") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = -851 }

                val validationInfo = decimalSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("decimalCount").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 0.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 112.54
                frame.spinner("maxValue").target().value = 644.74
                frame.spinner("decimalCount").target().value = 485
                frame.checkBox("showTrailingZeroes").target().isSelected = false
                frame.radioButton("groupingSeparatorUnderscore").target().isSelected = true
                frame.radioButton("decimalSeparatorComma").target().isSelected = true
            }

            decimalSettingsComponent.saveSettings()

            assertThat(decimalSettings.minValue).isEqualTo(112.54)
            assertThat(decimalSettings.maxValue).isEqualTo(644.74)
            assertThat(decimalSettings.decimalCount).isEqualTo(485)
            assertThat(decimalSettings.showTrailingZeroes).isEqualTo(false)
            assertThat(decimalSettings.groupingSeparator).isEqualTo("_")
            assertThat(decimalSettings.decimalSeparator).isEqualTo(",")
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(decimalSettingsComponentConfigurable.displayName).isEqualTo("Decimals")
        }

        describe("saving modifications") {
            it("accepts correct settings") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 89 }

                decimalSettingsComponentConfigurable.apply()

                assertThat(decimalSettings.decimalCount).isEqualTo(89)
            }

            it("rejects incorrect settings") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = -13 }

                Assertions.assertThatThrownBy { decimalSettingsComponentConfigurable.apply() }
                    .isInstanceOf(ConfigurationException::class.java)
            }
        }

        describe("modification detection") {
            it("is initially unmodified") {
                assertThat(decimalSettingsComponentConfigurable.isModified).isFalse()
            }

            it("modifies a single detection") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 214 }

                assertThat(decimalSettingsComponentConfigurable.isModified).isTrue()
            }

            it("ignores an undone modification") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 62 }
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = decimalSettings.decimalCount }

                assertThat(decimalSettingsComponentConfigurable.isModified).isFalse()
            }

            it("ignores saved modifications") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 102 }

                decimalSettingsComponentConfigurable.apply()

                assertThat(decimalSettingsComponentConfigurable.isModified).isFalse()
            }
        }

        describe("resets") {
            it("resets all fields properly") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = 206.90
                    frame.spinner("maxValue").target().value = 970.53
                    frame.spinner("decimalCount").target().value = 130
                    frame.checkBox("showTrailingZeroes").target().isSelected = true
                    frame.radioButton("groupingSeparatorPeriod").target().isSelected = true
                    frame.radioButton("decimalSeparatorComma").target().isSelected = true

                    decimalSettingsComponentConfigurable.reset()
                }

                assertThat(decimalSettingsComponentConfigurable.isModified).isFalse()
            }
        }
    }
})
