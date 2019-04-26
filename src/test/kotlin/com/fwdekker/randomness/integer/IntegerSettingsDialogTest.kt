package com.fwdekker.randomness.integer

import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [IntegerSettingsDialog].
 */
object IntegerSettingsDialogTest : Spek({
    val defaultMinValue = 2_147_483_883L
    val defaultMaxValue = 6_442_451_778L
    val defaultBase: Long = 10

    lateinit var integerSettings: IntegerSettings
    lateinit var integerSettingsDialog: IntegerSettingsDialog
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        integerSettings = IntegerSettings()
        integerSettings.minValue = defaultMinValue
        integerSettings.maxValue = defaultMaxValue
        integerSettings.base = defaultBase.toInt()

        integerSettingsDialog =
            GuiActionRunner.execute<IntegerSettingsDialog> { IntegerSettingsDialog(integerSettings) }
        frame = showInFrame(integerSettingsDialog.createCenterPanel())
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

        it("loads the default base value") {
            frame.spinner("base").requireValue(defaultBase)
        }
    }

    describe("input handling") {
        it("truncates decimals in the base") {
            GuiActionRunner.execute { frame.spinner("base").target().value = 22.62f }

            frame.spinner("base").requireValue(22L)
        }

        it("truncates decimals in the minimum value") {
            GuiActionRunner.execute { frame.spinner("minValue").target().value = 285.21f }

            frame.spinner("minValue").requireValue(285L)
        }

        it("truncates decimals in the maximum value") {
            GuiActionRunner.execute { frame.spinner("maxValue").target().value = 490.34f }

            frame.spinner("maxValue").requireValue(490L)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            val validationInfo = GuiActionRunner.execute<ValidationInfo> { integerSettingsDialog.doValidate() }

            assertThat(validationInfo).isNull()
        }

        describe("value range") {
            it("fails if the maximum value is greater than the minimum value") {
                GuiActionRunner.execute { frame.spinner("maxValue").target().value = defaultMinValue - 1 }

                val validationInfo = integerSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
            }

            it("fails if the range size overflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = Long.MIN_VALUE
                    frame.spinner("maxValue").target().value = Long.MAX_VALUE
                }

                val validationInfo = integerSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The range should not exceed 9.223372036854776E18.")
            }
        }

        describe("base") {
            it("fails if the base is negative") {
                GuiActionRunner.execute { frame.spinner("base").target().value = -189 }

                val validationInfo = integerSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 2.")
            }

            it("fails if the base is 0") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 0 }

                val validationInfo = integerSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 2.")
            }

            it("fails if the base is 1") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 1 }

                val validationInfo = integerSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 2.")
            }

            it("fails if the base is greater than 36") {
                GuiActionRunner.execute { frame.spinner("base").target().value = 68 }

                val validationInfo = integerSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("base").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 36.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = Integer.MAX_VALUE.toLong() + 1L
                frame.spinner("maxValue").target().value = Integer.MAX_VALUE.toLong() + 2L
                frame.spinner("base").target().value = 14L

                integerSettingsDialog.saveSettings()
            }

            assertThat(integerSettings.minValue).isEqualTo(2_147_483_648L)
            assertThat(integerSettings.maxValue).isEqualTo(2_147_483_649L)
            assertThat(integerSettings.base).isEqualTo(14)
        }
    }
})