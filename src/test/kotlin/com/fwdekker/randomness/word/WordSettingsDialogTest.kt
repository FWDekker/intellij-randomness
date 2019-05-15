package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.JEditableList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import org.junit.jupiter.api.fail


/**
 * GUI tests for [WordSettingsDialog].
 */
object WordSettingsDialogTest : Spek({
    lateinit var wordSettings: WordSettings
    lateinit var wordSettingsDialog: WordSettingsDialog
    lateinit var dialogDictionaries: JEditableList<Dictionary>
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    @Suppress("UNCHECKED_CAST")
    beforeEachTest {
        wordSettings = WordSettings()
        wordSettings.minLength = 4
        wordSettings.maxLength = 6
        wordSettings.enclosure = ""
        wordSettings.capitalization = CapitalizationMode.LOWER
        wordSettings.activeBundledDictionaryFiles = mutableSetOf(BundledDictionary.EXTENDED_DICTIONARY)

        wordSettingsDialog = GuiActionRunner.execute<WordSettingsDialog> { WordSettingsDialog(wordSettings) }
        frame = showInFrame(wordSettingsDialog.createCenterPanel())

        dialogDictionaries = frame.table("dictionaries").target() as JEditableList<Dictionary>
    }

    afterEachTest {
        frame.cleanUp()
    }



    describe("loading settings") {
        it("loads the settings' minimum length") {
            frame.spinner("minLength").requireValue(4)
        }

        it("loads the settings' maximum length") {
            frame.spinner("maxLength").requireValue(6)
        }

        it("loads the settings' enclosure") {
            frame.radioButton("enclosureNone").requireSelected(true)
            frame.radioButton("enclosureSingle").requireSelected(false)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the settings' capitalization") {
            frame.radioButton("capitalizationRetain").requireSelected(false)
            frame.radioButton("capitalizationSentence").requireSelected(false)
            frame.radioButton("capitalizationFirstLetter").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(false)
            frame.radioButton("capitalizationLower").requireSelected(true)
        }

        it("loads the settings' active bundled dictionaries") {
            assertThat(dialogDictionaries.activeEntries)
                .containsExactly(BundledDictionary.cache.get(BundledDictionary.EXTENDED_DICTIONARY))
        }
    }

    describe("input handling") {
        describe("length range") {
            it("truncates decimals in the minimum length") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 553.92f }

                frame.spinner("minLength").requireValue(553)
            }

            it("truncates decimals in the maximum length") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 796.01f }

                frame.spinner("maxLength").requireValue(796)
            }
        }

        describe("adding dictionaries") {
            // Disabled because AssertJ Swing doesn't work with IntelliJ file chooser
            xit("adds a given user dictionary") {
                frame.button("dictionaryAdd").click()
                // TODO Find file chooser window and select a file

                assertThat(dialogDictionaries.getEntry(1).toString().replace("\\\\".toRegex(), "/"))
                    .endsWith("dictionaries/simple.dic")
            }

            // Disabled because AssertJ Swing doesn't work with IntelliJ file chooser
            xit("does not add a duplicate user dictionary") {
                assertThat(dialogDictionaries.entryCount).isEqualTo(1)

                frame.button("dictionaryAdd").click()
                // TODO Find file chooser window and select a file
//        JFileChooserFinder.findFileChooser()
//            .selectFile(getDictionaryFile("dictionaries/simple.dic"))
//            .approve()

                // Select the same file again

                assertThat(dialogDictionaries.entryCount).isEqualTo(2)
            }
        }

        describe("removing dictionaries") {
            it("does not remove a bundled dictionary") {
                GuiActionRunner.execute {
                    frame.table("dictionaries").target().clearSelection()
                    frame.table("dictionaries").target().addRowSelectionInterval(0, 0)
                    frame.button("dictionaryRemove").target().doClick()
                }

                assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(2)
            }

            it("removes a user dictionary") {
                GuiActionRunner.execute {
                    dialogDictionaries.addEntry(UserDictionary.cache.get("dictionary.dic", true))
                    assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(3)

                    frame.table("dictionaries").target().clearSelection()
                    frame.table("dictionaries").target().addRowSelectionInterval(2, 2)
                    frame.button("dictionaryRemove").target().doClick()
                }

                assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(2)
            }
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { wordSettingsDialog.loadSettings(WordSettings()) }

            assertThat(wordSettingsDialog.doValidate()).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = -780 }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
            }

            it("fails if the minimum length is greater than the maximum length") {
                GuiActionRunner.execute {
                    frame.spinner("maxLength").target().value = WordSettings().minLength - 1
                }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should not be smaller than the minimum.")
            }

            it("fails if the length range ends too low to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 0 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 0 }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("" +
                    "Enter a value greater than or equal to 1, " +
                    "the length of the shortest word in the selected dictionaries."
                )
            }

            it("fails if the length range begins too high to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 1000 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 1000 }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("" +
                    "Enter a value less than or equal to 31, " +
                    "the length of the longest word in the selected dictionaries.")
            }
        }

        describe("dictionaries") {
            @Suppress("UNCHECKED_CAST")
            it("fails if a dictionary of a now-deleted file is given") {
                val dictionaries = frame.table("dictionaries").target() as JEditableList<Dictionary>

                val dictionaryFile = createTempFile("test", "dic")
                dictionaryFile.writeText("Limbas\nOstiary\nHackee")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, true)

                if (!dictionaryFile.delete())
                    fail("Failed to delete file as part of test.")

                GuiActionRunner.execute {
                    dictionaries.addEntry(dictionary)
                    dictionaries.setActiveEntries(listOf<Dictionary>(dictionary))
                }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
            }

            it("fails if no dictionaries are selected") {
                GuiActionRunner.execute {
                    frame.table("dictionaries").target().setValueAt(false, 0, 0)
                    frame.table("dictionaries").target().setValueAt(false, 1, 0)
                }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.table("dictionaries").target())
                assertThat(validationInfo?.message).isEqualTo("Select at least one dictionary.")
            }

            it("fails if one of the dictionaries is invalid") {
                wordSettings.userDictionaryFiles = mutableSetOf("does_not_exist.dic")
                wordSettings.activeUserDictionaryFiles = mutableSetOf("does_not_exist.dic")
                GuiActionRunner.execute { wordSettingsDialog.loadSettings(wordSettings) }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.table("dictionaries").target())
                assertThat(validationInfo?.message).isEqualTo("" +
                    "Dictionary [user] does_not_exist.dic is invalid: " +
                    "Failed to read user dictionary into memory."
                )
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minLength").target().value = 840
                frame.spinner("maxLength").target().value = 861
                frame.radioButton("enclosureSingle").target().isSelected = true
                frame.radioButton("capitalizationLower").target().isSelected = true
            }

            wordSettingsDialog.saveSettings()

            assertThat(wordSettings.minLength).isEqualTo(840)
            assertThat(wordSettings.maxLength).isEqualTo(861)
            assertThat(wordSettings.enclosure).isEqualTo("'")
            assertThat(wordSettings.capitalization).isEqualTo(CapitalizationMode.LOWER)
        }
    }
})
