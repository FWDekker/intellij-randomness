package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [StringSettingsDialog].
 */
object StringSettingsDialogTest : Spek({
    lateinit var stringSettings: StringSettings
    lateinit var stringSettingsDialog: StringSettingsDialog
    lateinit var stringSettingsDialogConfigurable: StringSettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        stringSettings = StringSettings()
        stringSettings.minLength = 144
        stringSettings.maxLength = 719
        stringSettings.enclosure = "\""
        stringSettings.capitalization = CapitalizationMode.RANDOM
        stringSettings.alphabets = mutableSetOf(Alphabet.ALPHABET, Alphabet.HEXADECIMAL)

        stringSettingsDialog = GuiActionRunner.execute<StringSettingsDialog> { StringSettingsDialog(stringSettings) }
        stringSettingsDialogConfigurable = StringSettingsConfigurable(stringSettingsDialog)
        frame = showInFrame(stringSettingsDialog.getRootPane())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' minimum value") {
            frame.spinner("minLength").requireValue(144)
        }

        it("loads the settings' maximum value") {
            frame.spinner("maxLength").requireValue(719)
        }

        it("loads the settings' enclosure") {
            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(false)
            frame.radioButton("enclosureDouble").requireSelected(true)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the settings' capitalization") {
            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(false)
            frame.radioButton("capitalizationRandom").requireSelected(true)
        }

        it("loads the settings' alphabets") {
            frame.list("alphabets")
                .requireSelectedItems("Alphabet (a, b, c, ...)", "Hexadecimal (0, 1, 2, ..., d, e, f)")
        }
    }

    describe("input handling") {
        it("truncates decimals in the minimum length") {
            GuiActionRunner.execute { frame.spinner("minLength").target().value = 553.92f }

            frame.spinner("minLength").requireValue(553)
        }

        it("truncates decimals in the maximum length") {
            GuiActionRunner.execute { frame.spinner("maxLength").target().value = 796.01f }

            frame.spinner("maxLength").requireValue(796)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { stringSettingsDialog.loadSettings(StringSettings()) }

            assertThat(stringSettingsDialog.doValidate()).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = -161 }

                val validationInfo = stringSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
            }

            it("fails if the minimum length is greater than the maximum length") {
                GuiActionRunner.execute {
                    frame.spinner("minLength").target().value = 234
                    frame.spinner("maxLength").target().value = 233
                }

                val validationInfo = stringSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should not be smaller than the minimum.")
            }
        }

        describe("alphabets") {
            it("fails if no alphabets are selected") {
                GuiActionRunner.execute { frame.list("alphabets").target().clearSelection() }

                val validationInfo = stringSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.list("alphabets").target())
                assertThat(validationInfo?.message).isEqualTo("Please select at least one alphabet.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            val newAlphabets = setOf(Alphabet.DIGITS, Alphabet.ALPHABET, Alphabet.SPECIAL)
            val newAlphabetsOrdinals = newAlphabets.map { it.ordinal }

            GuiActionRunner.execute {
                frame.spinner("minLength").target().value = 445
                frame.spinner("maxLength").target().value = 803
                frame.radioButton("enclosureBacktick").target().isSelected = true
                frame.radioButton("capitalizationUpper").target().isSelected = true
                frame.list("alphabets").target().selectedIndices = newAlphabetsOrdinals.toIntArray()
            }

            stringSettingsDialog.saveSettings()

            assertThat(stringSettings.minLength).isEqualTo(445)
            assertThat(stringSettings.maxLength).isEqualTo(803)
            assertThat(stringSettings.enclosure).isEqualTo("`")
            assertThat(stringSettings.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(stringSettings.alphabets).isEqualTo(newAlphabets)
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(stringSettingsDialogConfigurable.displayName).isEqualTo("Strings")
        }

        describe("modification detection") {
            it("is initially unmodified") {
                assertThat(stringSettingsDialogConfigurable.isModified).isFalse()
            }

            it("modifies a single detection") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 91 }

                assertThat(stringSettingsDialogConfigurable.isModified).isTrue()
            }

            it("ignores an undone modification") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 84 }
                GuiActionRunner.execute { frame.spinner("minLength").target().value = stringSettings.minLength }

                assertThat(stringSettingsDialogConfigurable.isModified).isFalse()
            }

            it("ignores saved modifications") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 204 }

                stringSettingsDialogConfigurable.apply()

                assertThat(stringSettingsDialogConfigurable.isModified).isFalse()
            }
        }

        describe("resets") {
            it("resets all fields properly") {
                val newAlphabets = setOf(Alphabet.ALPHABET, Alphabet.SPECIAL)
                val newAlphabetsOrdinals = newAlphabets.map { it.ordinal }

                GuiActionRunner.execute {
                    frame.spinner("minLength").target().value = 102
                    frame.spinner("maxLength").target().value = 75
                    frame.radioButton("enclosureSingle").target().isSelected = true
                    frame.radioButton("capitalizationLower").target().isSelected = true
                    frame.list("alphabets").target().selectedIndices = newAlphabetsOrdinals.toIntArray()

                    stringSettingsDialogConfigurable.reset()
                }

                assertThat(stringSettingsDialogConfigurable.isModified).isFalse()
            }
        }
    }
})
