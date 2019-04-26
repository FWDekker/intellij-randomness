package com.fwdekker.randomness.array

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [ArraySettingsDialog].
 */
object ArraySettingsDialogTest : Spek({
    lateinit var arraySettings: ArraySettings
    lateinit var arraySettingsDialog: ArraySettingsDialog
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        arraySettings = ArraySettings()
        arraySettingsDialog = GuiActionRunner.execute<ArraySettingsDialog> { ArraySettingsDialog(arraySettings) }
        frame = showInFrame(arraySettingsDialog.createCenterPanel())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the default count") {
            frame.spinner("count").requireValue(arraySettings.count.toLong())
        }

        it("loads the default brackets") {
            frame.radioButton("bracketsSquare").requireSelected()
            frame.radioButton("bracketsCurly").requireNotSelected()
            frame.radioButton("bracketsRound").requireNotSelected()
        }

        it("loads the default separator") {
            frame.radioButton("separatorComma").requireSelected()
            frame.radioButton("separatorSemicolon").requireNotSelected()
            frame.radioButton("separatorNewline").requireNotSelected()
        }

        it("loads the default settings for using a space after separator") {
            frame.checkBox("spaceAfterSeparator").requireSelected()
        }
    }

    describe("input handling") {
        it("truncates decimals in the count") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 983.24f }

            frame.spinner("count").requireValue(983L)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            assertThat(arraySettingsDialog.doValidate()).isNull()
        }

        describe("count") {
            it("fails for a count of 0") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 0 }

                val validationInfo = arraySettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
            }

            it("passes for a count of 1") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 1 }

                assertThat(arraySettingsDialog.doValidate()).isNull()
            }

            it("fails for a negative count") {
                GuiActionRunner.execute { frame.spinner("count").target().value = -172 }

                val validationInfo = arraySettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
            }

            it("fails for an overflowing count") {
                GuiActionRunner.execute { frame.spinner("count").target().value = Integer.MAX_VALUE.toLong() + 2L }

                val validationInfo = arraySettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("count").target().value = 642
                frame.radioButton("bracketsCurly").target().isSelected = true
                frame.radioButton("separatorSemicolon").target().isSelected = true
                frame.checkBox("spaceAfterSeparator").target().isSelected = false
            }

            arraySettingsDialog.saveSettings()

            assertThat(arraySettings.count).isEqualTo(642)
            assertThat(arraySettings.brackets).isEqualTo("{}")
            assertThat(arraySettings.separator).isEqualTo(";")
            assertThat(arraySettings.isSpaceAfterSeparator).isEqualTo(false)
        }
    }
})
