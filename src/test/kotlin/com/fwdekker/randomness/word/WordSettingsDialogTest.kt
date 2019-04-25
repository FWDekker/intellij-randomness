package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.JEditableList
import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Fail.fail
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import java.io.File


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
        wordSettingsDialog = GuiActionRunner.execute<WordSettingsDialog> { WordSettingsDialog(wordSettings) }
        frame = showInFrame(wordSettingsDialog.createCenterPanel())

        dialogDictionaries = frame.table("dictionaries").target() as JEditableList<Dictionary>
    }

    afterEachTest {
        frame.cleanUp()
    }



    describe("loading settings") {
        it("loads the default minimum length") {
            frame.spinner("minLength").requireValue(WordSettings().minLength.toLong())
        }

        it("loads the default maximum length") {
            frame.spinner("maxLength").requireValue(WordSettings().maxLength.toLong())
        }

        it("loads the default enclosure") {
            frame.radioButton("enclosureNone").requireNotSelected()
            frame.radioButton("enclosureSingle").requireNotSelected()
            frame.radioButton("enclosureDouble").requireSelected()
            frame.radioButton("enclosureBacktick").requireNotSelected()
        }

        it("loads the default capitalization") {
            frame.radioButton("capitalizationRetain").requireSelected()
            frame.radioButton("capitalizationSentence").requireNotSelected()
            frame.radioButton("capitalizationFirstLetter").requireNotSelected()
            frame.radioButton("capitalizationUpper").requireNotSelected()
            frame.radioButton("capitalizationLower").requireNotSelected()
        }
    }

    describe("input handling") {
        describe("length range") {
            it("truncates decimals in the minimum length") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 553.92f }

                frame.spinner("minLength").requireValue(553L)
            }

            it("truncates decimals in the maximum length") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 796.01f }

                frame.spinner("maxLength").requireValue(796L)
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

                assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(1)
            }

            it("removes a user dictionary") {
                GuiActionRunner.execute {
                    dialogDictionaries.addEntry(UserDictionary.cache.get(
                        getDictionaryFile("dictionaries/simple.dic").canonicalPath, true)
                    )
                    frame.table("dictionaries").target().clearSelection()
                    frame.table("dictionaries").target().addRowSelectionInterval(1, 1)
                    frame.button("dictionaryRemove").target().doClick()
                }

                assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(1)
            }
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            val validationInfo = GuiActionRunner.execute<ValidationInfo> { wordSettingsDialog.doValidate() }

            assertThat(validationInfo).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = -780 }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
            }

            it("fails if the maximum length overflows") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = Integer.MAX_VALUE.toLong() + 2L }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
            }

            it("fails if the minimum length is greater than the maximum length") {
                GuiActionRunner.execute {
                    frame.spinner("maxLength").target().value = WordSettings().minLength - 1
                }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
            }

            it("fails if the length range ends too low to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 0 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 0 }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
            }

            it("fails if the length range begins too high to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 1000 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 1000 }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 31.")
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
                GuiActionRunner.execute { frame.table("dictionaries").target().setValueAt(false, 0, 0) }

                val validationInfo = wordSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.table("dictionaries").target())
                assertThat(validationInfo?.message).isEqualTo("Select at least one dictionary.")
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


fun getDictionaryFile(path: String) = File(WordSettingsDialogTest::class.java.classLoader.getResource(path).path)
