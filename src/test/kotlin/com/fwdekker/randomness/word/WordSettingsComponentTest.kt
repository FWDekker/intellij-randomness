package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.EditableDatum
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.table.TableView
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [WordSettingsComponent].
 */
object WordSettingsComponentTest : Spek({
    lateinit var tempFileHelper: TempFileHelper
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var wordSettings: WordSettings
    lateinit var wordSettingsComponent: WordSettingsComponent
    lateinit var dictionaryTable: TableView<EditableDatum<Dictionary>>
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    @Suppress("UNCHECKED_CAST")
    beforeEachTest {
        tempFileHelper = TempFileHelper()

        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        wordSettings = WordSettings()
            .apply {
                currentScheme.minLength = 4
                currentScheme.maxLength = 6
                currentScheme.enclosure = ""
                currentScheme.capitalization = CapitalizationMode.LOWER
                currentScheme.activeBundledDictionaryFiles = mutableSetOf(BundledDictionary.EXTENDED_DICTIONARY)
            }

        wordSettingsComponent = GuiActionRunner.execute<WordSettingsComponent> { WordSettingsComponent(wordSettings) }
        frame = showInFrame(wordSettingsComponent.rootPane)

        dictionaryTable = frame.table().target() as TableView<EditableDatum<Dictionary>>
    }

    afterEachTest {
        ideaFixture.tearDown()
        frame.cleanUp()
        tempFileHelper.cleanUp()
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

        it("loads the settings' bundled dictionaries") {
            assertThat(dictionaryTable.items.map { it.datum }).containsExactly(
                BundledDictionary.cache.get(BundledDictionary.SIMPLE_DICTIONARY),
                BundledDictionary.cache.get(BundledDictionary.EXTENDED_DICTIONARY)
            )
            assertThat(dictionaryTable.items.filter { it.active }.map { it.datum }).containsExactly(
                BundledDictionary.cache.get(BundledDictionary.EXTENDED_DICTIONARY)
            )
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

            wordSettingsComponent.saveSettings()

            assertThat(wordSettings.currentScheme.minLength).isEqualTo(840)
            assertThat(wordSettings.currentScheme.maxLength).isEqualTo(861)
            assertThat(wordSettings.currentScheme.enclosure).isEqualTo("'")
            assertThat(wordSettings.currentScheme.capitalization).isEqualTo(CapitalizationMode.LOWER)
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
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { wordSettingsComponent.loadSettings(WordSettings()) }

            assertThat(wordSettingsComponent.doValidate()).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = -780 }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message)
                    .isEqualTo("The minimum length should be greater than or equal to 1.")
            }

            it("fails if the length range ends too low to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 0 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 0 }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo(
                    "The shortest word in the selected dictionaries is 1 characters. Set the maximum length to a " +
                        "value less than or equal to 1."
                )
            }

            it("fails if the length range begins too high to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 1000 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 1000 }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo(
                    "The longest word in the selected dictionaries is 31 characters. Set the minimum length to a " +
                        "value less than or equal to 31."
                )
            }
        }

        describe("dictionaries") {
            it("fails if a dictionary of a now-deleted file is given") {
                val dictionaryFile = tempFileHelper.createFile("explore\nworm\ndamp", ".dic")
                    .also { it.delete() }
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, true)

                GuiActionRunner.execute {
                    dictionaryTable.listTableModel.addRow(EditableDatum(true, dictionary))
                }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("dictionaryPanel").target())
                assertThat(validationInfo?.message)
                    .matches("Dictionary `.*\\.dic` is invalid: Failed to read user dictionary into memory\\.")
            }

            it("fails if no dictionaries are selected") {
                GuiActionRunner.execute {
                    dictionaryTable.setValueAt(false, 0, 0)
                    dictionaryTable.setValueAt(false, 1, 0)
                }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("dictionaryPanel").target())
                assertThat(validationInfo?.message).isEqualTo("Select at least one dictionary.")
            }

            it("fails if one of the dictionaries is invalid") {
                wordSettings.currentScheme.userDictionaryFiles = mutableSetOf("does_not_exist.dic")
                wordSettings.currentScheme.activeUserDictionaryFiles = wordSettings.currentScheme.userDictionaryFiles
                GuiActionRunner.execute { wordSettingsComponent.loadSettings(wordSettings) }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("dictionaryPanel").target())
                assertThat(validationInfo?.message).isEqualTo(
                    "Dictionary `[user] does_not_exist.dic` is invalid: Failed to read user dictionary into memory."
                )
            }

            it("fails if one the dictionaries is empty") {
                val dictionaryFile = tempFileHelper.createFile("", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, true)

                wordSettings.currentScheme.userDictionaries = setOf(dictionary)
                wordSettings.currentScheme.activeUserDictionaries = wordSettings.currentScheme.userDictionaries
                GuiActionRunner.execute { wordSettingsComponent.loadSettings(wordSettings) }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("dictionaryPanel").target())
                assertThat(validationInfo?.message).matches("Dictionary `.*\\.dic` is empty\\.")
            }

            it("fails if a dictionary is added twice") {
                val dictionaryFile = tempFileHelper.createFile("whistle", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, true)

                wordSettings.currentScheme.userDictionaries = setOf(dictionary)
                wordSettings.currentScheme.activeUserDictionaries = wordSettings.currentScheme.userDictionaries
                GuiActionRunner.execute {
                    wordSettingsComponent.loadSettings(wordSettings)
                    dictionaryTable.listTableModel.addRow(EditableDatum(true, dictionary))
                    dictionaryTable.listTableModel.addRow(EditableDatum(true, dictionary))
                }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.panel("dictionaryPanel").target())
                assertThat(validationInfo?.message).matches("Dictionaries must be unique.")
            }
        }
    }
})
