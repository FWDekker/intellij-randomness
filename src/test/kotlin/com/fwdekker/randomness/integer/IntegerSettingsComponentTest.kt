package com.fwdekker.randomness.integer

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
 * GUI tests for [IntegerSettingsComponent].
 */
object IntegerSettingsComponentTest : Spek({
    lateinit var integerSettings: IntegerSettings
    lateinit var integerSettingsComponent: IntegerSettingsComponent
    lateinit var integerSettingsComponentConfigurable: IntegerSettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        integerSettings = IntegerSettings()
        integerSettings.minValue = 2_147_483_883L
        integerSettings.maxValue = 6_442_451_778L
        integerSettings.base = 10
        integerSettings.groupingSeparator = "_"

        integerSettingsComponent =
            GuiActionRunner.execute<IntegerSettingsComponent> { IntegerSettingsComponent(integerSettings) }
        integerSettingsComponentConfigurable = IntegerSettingsConfigurable(integerSettingsComponent)
        frame = showInFrame(integerSettingsComponent.getRootPane())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' minimum value") {
            frame.spinner("minValue").requireValue(2_147_483_883L)
        }

        it("loads the settings' maximum value") {
            frame.spinner("maxValue").requireValue(6_442_451_778L)
        }

        it("loads the settings' base value") {
            frame.spinner("base").requireValue(10)
        }

        it("loads the settings' base value") {
            frame.radioButton("groupingSeparatorNone").requireSelected(false)
            frame.radioButton("groupingSeparatorPeriod").requireSelected(false)
            frame.radioButton("groupingSeparatorComma").requireSelected(false)
            frame.radioButton("groupingSeparatorUnderscore").requireSelected(true)
        }
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

        describe("grouping separator") {
            it("uses the default separator if null is set") {
                integerSettings.safeSetGroupingSeparator(null)

                assertThat(integerSettings.groupingSeparator).isEqualTo(IntegerSettings.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                integerSettings.safeSetGroupingSeparator("")

                assertThat(integerSettings.groupingSeparator).isEqualTo(IntegerSettings.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                integerSettings.safeSetGroupingSeparator("mention")

                assertThat(integerSettings.groupingSeparator).isEqualTo("m")
            }
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { integerSettingsComponent.loadSettings(IntegerSettings()) }

            assertThat(integerSettingsComponent.doValidate()).isNull()
        }

        describe("value range") {
            it("fails if the minimum value is greater than the maximum value") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = 98
                    frame.spinner("maxValue").target().value = 97
                }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should not be smaller than the minimum.")
            }

            it("fails if the range size overflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = Long.MIN_VALUE
                    frame.spinner("maxValue").target().value = Long.MAX_VALUE
                }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The range should not exceed 9.223372036854776E18.")
            }
        }

        describe("base") {
            it("fails if the base is negative") {
                GuiActionRunner.execute { frame.spinner("base").target().value = -189 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 2.")
            }

            it("fails if the base is 0") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 0 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 2.")
            }

            it("fails if the base is 1") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 1 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 2.")
            }

            it("fails if the base is greater than 36") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 68 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 36.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 2147483648L
                frame.spinner("maxValue").target().value = 2147483649L
                frame.spinner("base").target().value = 14
                frame.radioButton("groupingSeparatorPeriod").target().isSelected = true
            }

            integerSettingsComponent.saveSettings()

            assertThat(integerSettings.minValue).isEqualTo(2_147_483_648L)
            assertThat(integerSettings.maxValue).isEqualTo(2_147_483_649L)
            assertThat(integerSettings.base).isEqualTo(14)
            assertThat(integerSettings.groupingSeparator).isEqualTo(".")
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(integerSettingsComponentConfigurable.displayName).isEqualTo("Integers")
        }

        describe("saving modifications") {
            it("accepts correct settings") {
                GuiActionRunner.execute { frame.spinner("minValue").target().value = 92 }

                integerSettingsComponentConfigurable.apply()

                assertThat(integerSettings.minValue).isEqualTo(92)
            }

            it("rejects incorrect settings") {
                GuiActionRunner.execute { frame.spinner("minValue").target().value = 83 }
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = 1 }

                Assertions.assertThatThrownBy { integerSettingsComponentConfigurable.apply() }
                    .isInstanceOf(ConfigurationException::class.java)
            }
        }

        describe("modification detection") {
            it("is initially unmodified") {
                assertThat(integerSettingsComponentConfigurable.isModified).isFalse()
            }

            it("modifies a single detection") {
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = 232 }

                assertThat(integerSettingsComponentConfigurable.isModified).isTrue()
            }

            it("ignores an undone modification") {
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = 199 }
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = integerSettings.maxValue }

                assertThat(integerSettingsComponentConfigurable.isModified).isFalse()
            }

            it("ignores saved modifications") {
                GuiActionRunner.execute { frame.spinner("minValue").target().value = 70 }
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = 169 }

                integerSettingsComponentConfigurable.apply()

                assertThat(integerSettingsComponentConfigurable.isModified).isFalse()
            }
        }

        describe("resets") {
            it("resets all fields properly") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = 159
                    frame.spinner("maxValue").target().value = 181
                    frame.spinner("base").target().value = 12
                    frame.radioButton("groupingSeparatorComma").target().isSelected = true

                    integerSettingsComponentConfigurable.reset()
                }

                assertThat(integerSettingsComponentConfigurable.isModified).isFalse()
            }
        }
    }
})