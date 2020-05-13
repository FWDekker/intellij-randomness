package com.fwdekker.randomness.decimal

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [DecimalSettingsComponent].
 */
object DecimalSettingsComponentTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var decimalSettings: DecimalSettings
    lateinit var decimalSettingsComponent: DecimalSettingsComponent
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
                currentScheme.prefix = "."
                currentScheme.suffix = "."
            }

        decimalSettingsComponent =
            GuiActionRunner.execute<DecimalSettingsComponent> { DecimalSettingsComponent(decimalSettings) }
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

        it("loads the settings' prefix") {
            frame.textBox("prefix").requireText("")
        }

        it("loads the settings' suffix") {
            frame.textBox("suffix").requireText("")
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
                frame.textBox("prefix").target().text = "exercise"
                frame.textBox("suffix").target().text = "court"
            }

            decimalSettingsComponent.saveSettings()

            assertThat(decimalSettings.currentScheme.minValue).isEqualTo(112.54)
            assertThat(decimalSettings.currentScheme.maxValue).isEqualTo(644.74)
            assertThat(decimalSettings.currentScheme.decimalCount).isEqualTo(485)
            assertThat(decimalSettings.currentScheme.showTrailingZeroes).isEqualTo(false)
            assertThat(decimalSettings.currentScheme.groupingSeparator).isEqualTo("_")
            assertThat(decimalSettings.currentScheme.decimalSeparator).isEqualTo(",")
            assertThat(decimalSettings.currentScheme.prefix).isEqualTo("exercise")
            assertThat(decimalSettings.currentScheme.suffix).isEqualTo("court")
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
})
