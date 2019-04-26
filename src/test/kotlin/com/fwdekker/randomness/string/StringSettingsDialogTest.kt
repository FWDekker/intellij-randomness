package com.fwdekker.randomness.string

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
    val defaultMinValue = 144
    val defaultMaxValue = 719
    val defaultEnclosure = "\""
    val defaultAlphabets = setOf(Alphabet.ALPHABET, Alphabet.ALPHABET)

    lateinit var stringSettings: StringSettings
    lateinit var stringSettingsDialog: StringSettingsDialog
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        stringSettings = StringSettings()
        stringSettings.minLength = defaultMinValue
        stringSettings.maxLength = defaultMaxValue
        stringSettings.enclosure = defaultEnclosure
        stringSettings.alphabets = defaultAlphabets.toMutableSet()

        stringSettingsDialog =
            GuiActionRunner.execute<StringSettingsDialog> { StringSettingsDialog(stringSettings) }
        frame = showInFrame(stringSettingsDialog.createCenterPanel())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the default minimum value") {
            frame.spinner("minLength").requireValue(defaultMinValue.toLong())
        }

        it("loads the default maximum value") {
            frame.spinner("maxLength").requireValue(defaultMaxValue.toLong())
        }

        it("loads the default enclosure") {
            frame.radioButton("enclosureNone").requireNotSelected()
            frame.radioButton("enclosureSingle").requireNotSelected()
            frame.radioButton("enclosureDouble").requireSelected()
            frame.radioButton("enclosureBacktick").requireNotSelected()
        }

        it("loads the default alphabets") {
            val expectedSelected = defaultAlphabets.map { it.toString() }.toTypedArray()

            frame.list("alphabets").requireSelectedItems(*expectedSelected)
        }
    }

    describe("input handling") {
        it("truncates decimals in the minimum length") {
            GuiActionRunner.execute { frame.spinner("minLength").target().value = 553.92f }

            frame.spinner("minLength").requireValue(553L)
        }

        it("truncates decimals in the maximum length") {
            GuiActionRunner.execute { frame.spinner("maxLength").target().value = 796.01f }

            frame.spinner("maxLength").requireValue(796L)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            assertThat(stringSettingsDialog.doValidate()).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = -161 }

                val validationInfo = stringSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
            }

            it("fails if the maximum length overflows") {
                GuiActionRunner.execute {
                    frame.spinner("maxLength").target().value = Integer.MAX_VALUE.toLong() + 2L
                }

                val validationInfo = stringSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
            }

            it("fails if the minimum length is greater than the maximum length") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = defaultMinValue - 1 }

                val validationInfo = stringSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
            }
        }

        describe("alphabets") {
            it("fails if no alphabets are selected") {
                GuiActionRunner.execute { frame.list("alphabets").target().clearSelection() }

                val validationInfo = stringSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull
                assertThat(validationInfo?.component).isEqualTo(frame.list("alphabets").target())
                assertThat(validationInfo?.message).isEqualTo("Please select at least one option.")
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
                frame.list("alphabets").target().setSelectedIndices(newAlphabetsOrdinals.toIntArray())
            }

            stringSettingsDialog.saveSettings()

            assertThat(stringSettings.minLength).isEqualTo(445)
            assertThat(stringSettings.maxLength).isEqualTo(803)
            assertThat(stringSettings.enclosure).isEqualTo("`")
            assertThat(stringSettings.alphabets).isEqualTo(newAlphabets)
        }
    }
})
