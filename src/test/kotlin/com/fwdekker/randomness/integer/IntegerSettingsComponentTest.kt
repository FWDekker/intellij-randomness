package com.fwdekker.randomness.integer

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
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
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var integerSettings: IntegerSettings
    lateinit var integerSettingsComponent: IntegerSettingsComponent
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        integerSettings = IntegerSettings()
            .apply {
                currentScheme.minValue = 2_147_483_883L
                currentScheme.maxValue = 6_442_451_778L
                currentScheme.base = 10
                currentScheme.groupingSeparator = "_"
            }

        integerSettingsComponent =
            GuiActionRunner.execute<IntegerSettingsComponent> { IntegerSettingsComponent(integerSettings) }
        frame = showInFrame(integerSettingsComponent.rootPane)
    }

    afterEachTest {
        ideaFixture.tearDown()
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

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 2147483648L
                frame.spinner("maxValue").target().value = 2147483649L
                frame.spinner("base").target().value = 14
                frame.radioButton("groupingSeparatorPeriod").target().isSelected = true
            }

            integerSettingsComponent.saveSettings()

            assertThat(integerSettings.currentScheme.minValue).isEqualTo(2_147_483_648L)
            assertThat(integerSettings.currentScheme.maxValue).isEqualTo(2_147_483_649L)
            assertThat(integerSettings.currentScheme.base).isEqualTo(14)
            assertThat(integerSettings.currentScheme.groupingSeparator).isEqualTo(".")
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
                integerSettings.currentScheme.safeSetGroupingSeparator(null)

                assertThat(integerSettings.currentScheme.groupingSeparator).isEqualTo(IntegerScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                integerSettings.currentScheme.safeSetGroupingSeparator("")

                assertThat(integerSettings.currentScheme.groupingSeparator).isEqualTo(IntegerScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                integerSettings.currentScheme.safeSetGroupingSeparator("mention")

                assertThat(integerSettings.currentScheme.groupingSeparator).isEqualTo("m")
            }
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { integerSettingsComponent.loadSettings(IntegerSettings()) }

            assertThat(integerSettingsComponent.doValidate()).isNull()
        }

        describe("value range") {
            it("fails if the range size overflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = Long.MIN_VALUE
                    frame.spinner("maxValue").target().value = Long.MAX_VALUE
                }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The value range should not exceed 9.223372036854776E18.")
            }
        }

        describe("base") {
            it("fails if the base is negative") {
                GuiActionRunner.execute { frame.spinner("base").target().value = -189 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("The base should be greater than or equal to 2.")
            }

            it("fails if the base is 0") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 0 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("The base should be greater than or equal to 2.")
            }

            it("fails if the base is 1") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 1 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("The base should be greater than or equal to 2.")
            }

            it("fails if the base is greater than 36") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 68 }

                val validationInfo = integerSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("The base should be less than or equal to 36.")
            }
        }
    }
})
