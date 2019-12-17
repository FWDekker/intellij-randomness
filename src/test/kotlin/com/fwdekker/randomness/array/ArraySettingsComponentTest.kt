package com.fwdekker.randomness.array

import com.intellij.openapi.options.ConfigurationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [ArraySettingsComponent].
 */
object ArraySettingsComponentTest : Spek({
    lateinit var arraySettings: ArraySettings
    lateinit var arraySettingsComponent: ArraySettingsComponent
    lateinit var arraySettingsComponentConfigurable: ArraySettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        arraySettings = ArraySettings()
        arraySettings.count = 6
        arraySettings.brackets = "[]"
        arraySettings.separator = ","
        arraySettings.isSpaceAfterSeparator = false

        arraySettingsComponent =
            GuiActionRunner.execute<ArraySettingsComponent> { ArraySettingsComponent(arraySettings) }
        arraySettingsComponentConfigurable = ArraySettingsConfigurable(arraySettingsComponent)
        frame = showInFrame(arraySettingsComponent.rootPane)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' count") {
            frame.spinner("count").requireValue(6)
        }

        it("loads the settings' brackets") {
            frame.radioButton("bracketsNone").requireSelected(false)
            frame.radioButton("bracketsSquare").requireSelected(true)
            frame.radioButton("bracketsCurly").requireSelected(false)
            frame.radioButton("bracketsRound").requireSelected(false)
        }

        it("loads the settings' separator") {
            frame.radioButton("separatorComma").requireSelected(true)
            frame.radioButton("separatorSemicolon").requireSelected(false)
            frame.radioButton("separatorNewline").requireSelected(false)
        }

        it("loads the settings' settings for using a space after separator") {
            frame.checkBox("spaceAfterSeparator").requireSelected(false)
        }
    }

    describe("input handling") {
        it("truncates decimals in the count") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 983.24f }

            frame.spinner("count").requireValue(983)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { arraySettingsComponent.loadSettings(ArraySettings()) }

            assertThat(arraySettingsComponent.doValidate()).isNull()
        }

        describe("count") {
            it("fails for a count of 0") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 0 }

                val validationInfo = arraySettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
                assertThat(validationInfo?.message).isEqualTo("The count should be greater than or equal to 1.")
            }

            it("passes for a count of 1") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 1 }

                assertThat(arraySettingsComponent.doValidate()).isNull()
            }

            it("fails for a negative count") {
                GuiActionRunner.execute { frame.spinner("count").target().value = -172 }

                val validationInfo = arraySettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
                assertThat(validationInfo?.message).isEqualTo("The count should be greater than or equal to 1.")
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

            arraySettingsComponent.saveSettings()

            assertThat(arraySettings.count).isEqualTo(642)
            assertThat(arraySettings.brackets).isEqualTo("{}")
            assertThat(arraySettings.separator).isEqualTo(";")
            assertThat(arraySettings.isSpaceAfterSeparator).isEqualTo(false)
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(arraySettingsComponentConfigurable.displayName).isEqualTo("Arrays")
        }

        describe("saving modifications") {
            it("accepts correct settings") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 39 }

                arraySettingsComponentConfigurable.apply()

                assertThat(arraySettings.count).isEqualTo(39)
            }

            it("rejects incorrect settings") {
                GuiActionRunner.execute { frame.spinner("count").target().value = -3 }

                assertThatThrownBy { arraySettingsComponentConfigurable.apply() }
                    .isInstanceOf(ConfigurationException::class.java)
            }
        }

        describe("modification detection") {
            it("is initially unmodified") {
                assertThat(arraySettingsComponentConfigurable.isModified).isFalse()
            }

            it("modifies a single detection") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 124 }

                assertThat(arraySettingsComponentConfigurable.isModified).isTrue()
            }

            it("ignores an undone modification") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 17 }
                GuiActionRunner.execute { frame.spinner("count").target().value = arraySettings.count }

                assertThat(arraySettingsComponentConfigurable.isModified).isFalse()
            }

            it("ignores saved modifications") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 110 }

                arraySettingsComponentConfigurable.apply()

                assertThat(arraySettingsComponentConfigurable.isModified).isFalse()
            }
        }

        describe("resets") {
            it("resets all fields properly") {
                GuiActionRunner.execute {
                    frame.spinner("count").target().value = 642
                    frame.radioButton("bracketsCurly").target().isSelected = true
                    frame.radioButton("separatorSemicolon").target().isSelected = true
                    frame.checkBox("spaceAfterSeparator").target().isSelected = false

                    arraySettingsComponentConfigurable.reset()
                }

                assertThat(arraySettingsComponentConfigurable.isModified).isFalse()
            }
        }
    }
})
