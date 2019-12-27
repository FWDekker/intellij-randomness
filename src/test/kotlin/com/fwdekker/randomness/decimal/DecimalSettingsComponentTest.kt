package com.fwdekker.randomness.decimal

import com.intellij.openapi.options.ConfigurationException
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
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
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var decimalSettings: DecimalSettings
    lateinit var decimalSettingsComponent: DecimalSettingsComponent
    lateinit var decimalSettingsComponentConfigurable: DecimalSettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        decimalSettings = DecimalSettings()
            .apply {
                currentScheme.minValue = 157.61
                currentScheme.maxValue = 408.68
                currentScheme.decimalCount = 5
                currentScheme.showTrailingZeroes = false
                currentScheme.groupingSeparator = "_"
                currentScheme.decimalSeparator = "."
            }

        decimalSettingsComponent =
            GuiActionRunner.execute<DecimalSettingsComponent> { DecimalSettingsComponent(decimalSettings) }
        decimalSettingsComponentConfigurable = DecimalSettingsConfigurable(decimalSettingsComponent)
        frame = showInFrame(decimalSettingsComponent.rootPane)
    }

    afterEachTest {
        ideaFixture.tearDown()
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
            it("fails if the range size overflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = -1E53
                    frame.spinner("maxValue").target().value = 1E53
                }

                val validationInfo = decimalSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The value range should not exceed 1.0E53.")
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
                assertThat(validationInfo?.message).isEqualTo("The decimal count should be greater than or equal to 0.")
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

            assertThat(decimalSettings.currentScheme.minValue).isEqualTo(112.54)
            assertThat(decimalSettings.currentScheme.maxValue).isEqualTo(644.74)
            assertThat(decimalSettings.currentScheme.decimalCount).isEqualTo(485)
            assertThat(decimalSettings.currentScheme.showTrailingZeroes).isEqualTo(false)
            assertThat(decimalSettings.currentScheme.groupingSeparator).isEqualTo("_")
            assertThat(decimalSettings.currentScheme.decimalSeparator).isEqualTo(",")
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

                assertThat(decimalSettings.currentScheme.decimalCount).isEqualTo(89)
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
                GuiActionRunner.execute {
                    frame.spinner("decimalCount").target().value = decimalSettings.currentScheme.decimalCount
                }

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
