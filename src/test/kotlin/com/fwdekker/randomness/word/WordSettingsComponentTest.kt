package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.JCheckBoxTable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe
import org.junit.jupiter.api.fail


/**
 * GUI tests for [WordSettingsComponent].
 */
object WordSettingsComponentTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var wordSettings: WordSettings
    lateinit var wordSettingsComponent: WordSettingsComponent
    lateinit var wordSettingsComponentConfigurable: WordSettingsConfigurable
    lateinit var componentDictionaries: JCheckBoxTable<Dictionary>
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    @Suppress("UNCHECKED_CAST")
    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        wordSettings = WordSettings()
        wordSettings.minLength = 4
        wordSettings.maxLength = 6
        wordSettings.enclosure = ""
        wordSettings.capitalization = CapitalizationMode.LOWER
        wordSettings.activeBundledDictionaryFiles = mutableSetOf(BundledDictionary.EXTENDED_DICTIONARY)

        wordSettingsComponent = GuiActionRunner.execute<WordSettingsComponent> { WordSettingsComponent(wordSettings) }
        wordSettingsComponentConfigurable = WordSettingsConfigurable(wordSettingsComponent)
        frame = showInFrame(wordSettingsComponent.getRootPane())

        componentDictionaries = frame.table("dictionaries").target() as JCheckBoxTable<Dictionary>
    }

    afterEachTest {
        ideaFixture.tearDown()
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
            assertThat(componentDictionaries.activeEntries)
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

        // Disabled because AssertJ Swing doesn't work with IntelliJ file chooser
        xdescribe("adding dictionaries") {
            it("adds a given user dictionary") {
                frame.button("dictionaryAdd").click()
                // TODO Find file chooser window and select a file

                assertThat(componentDictionaries.getEntry(1).toString().replace("\\\\".toRegex(), "/"))
                    .endsWith("dictionaries/simple.dic")
            }

            // Disabled because AssertJ Swing doesn't work with IntelliJ file chooser
            it("does not add a duplicate user dictionary") {
                assertThat(componentDictionaries.entryCount).isEqualTo(1)

                frame.button("dictionaryAdd").click()
                // TODO Find file chooser window and select a file
//                JFileChooserFinder.findFileChooser()
//                    .selectFile(getDictionaryFile("dictionaries/simple.dic"))
//                    .approve()

                // Select the same file again

                assertThat(componentDictionaries.entryCount).isEqualTo(2)
            }
        }

        // Add/remove buttons not addressable from AssertJ Swing
        xdescribe("removing dictionaries") {
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
                    componentDictionaries.addEntry(UserDictionary.cache.get("dictionary.dic", true))
                    assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(3)

                    frame.table("dictionaries").target().clearSelection()
                    frame.table("dictionaries").target().addRowSelectionInterval(2, 2)
                    frame.button("dictionaryRemove").target().doClick()
                }

                assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(2)
            }

            it("removes nothing when no dictionary is highlighted") {
                val initialEntries = componentDictionaries.entries

                GuiActionRunner.execute {
                    frame.table("dictionaries").target().clearSelection()
                    frame.button("dictionaryRemove").target().doClick()
                }

                assertThat(componentDictionaries.entries).isEqualTo(initialEntries)
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
                assertThat(validationInfo?.message).isEqualTo("Enter a value greater than or equal to 1.")
            }

            it("fails if the minimum length is greater than the maximum length") {
                GuiActionRunner.execute {
                    frame.spinner("maxLength").target().value = WordSettings().minLength - 1
                }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message)
                    .isEqualTo("The maximum length should not be smaller than the minimum length.")
            }

            it("fails if the length range ends too low to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 0 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 0 }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
                assertThat(validationInfo?.message).isEqualTo("" +
                    "The shortest word in the selected dictionaries is 1 characters. " +
                    "Set the maximum length to a value less than or equal to 1."
                )
            }

            it("fails if the length range begins too high to match any words") {
                GuiActionRunner.execute { frame.spinner("minLength").target().value = 1000 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 1000 }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
                assertThat(validationInfo?.message).isEqualTo("" +
                    "The longest word in the selected dictionaries is 31 characters. " +
                    "Set the minimum length to a value less than or equal to 31."
                )
            }
        }

        describe("dictionaries") {
            @Suppress("UNCHECKED_CAST")
            it("fails if a dictionary of a now-deleted file is given") {
                val dictionaries = frame.table("dictionaries").target() as JCheckBoxTable<Dictionary>

                val dictionaryFile = createTempFile("test", "dic")
                dictionaryFile.writeText("Limbas\nOstiary\nHackee")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, true)

                if (!dictionaryFile.delete())
                    fail("Failed to delete file as part of test.")

                GuiActionRunner.execute {
                    dictionaries.addEntry(dictionary)
                    dictionaries.setActiveEntries(listOf<Dictionary>(dictionary))
                }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
            }

            it("fails if no dictionaries are selected") {
                GuiActionRunner.execute {
                    frame.table("dictionaries").target().setValueAt(false, 0, 0)
                    frame.table("dictionaries").target().setValueAt(false, 1, 0)
                }

                val validationInfo = wordSettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.table("dictionaries").target())
                assertThat(validationInfo?.message).isEqualTo("Select at least one dictionary.")
            }

            it("fails if one of the dictionaries is invalid") {
                wordSettings.userDictionaryFiles = mutableSetOf("does_not_exist.dic")
                wordSettings.activeUserDictionaryFiles = mutableSetOf("does_not_exist.dic")
                GuiActionRunner.execute { wordSettingsComponent.loadSettings(wordSettings) }

                val validationInfo = wordSettingsComponent.doValidate()

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

            wordSettingsComponent.saveSettings()

            assertThat(wordSettings.minLength).isEqualTo(840)
            assertThat(wordSettings.maxLength).isEqualTo(861)
            assertThat(wordSettings.enclosure).isEqualTo("'")
            assertThat(wordSettings.capitalization).isEqualTo(CapitalizationMode.LOWER)
        }
    }

    describe("configurable") {
        it("returns the correct display name") {
            assertThat(wordSettingsComponentConfigurable.displayName).isEqualTo("Words")
        }

        describe("saving modifications") {
            it("accepts correct settings") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 253 }

                wordSettingsComponentConfigurable.apply()

                assertThat(wordSettings.maxLength).isEqualTo(253)
            }

            it("rejects incorrect settings") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = -82 }

                assertThatThrownBy { wordSettingsComponentConfigurable.apply() }
                    .isInstanceOf(ConfigurationException::class.java)
            }
        }

        describe("modification detection") {
            it("is initially unmodified") {
                assertThat(wordSettingsComponentConfigurable.isModified).isFalse()
            }

            it("modifies a single detection") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 240 }

                assertThat(wordSettingsComponentConfigurable.isModified).isTrue()
            }

            it("ignores an undone modification") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 51 }
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = wordSettings.maxLength }

                assertThat(wordSettingsComponentConfigurable.isModified).isFalse()
            }

            it("ignores saved modifications") {
                GuiActionRunner.execute { frame.spinner("maxLength").target().value = 209 }

                wordSettingsComponentConfigurable.apply()

                assertThat(wordSettingsComponentConfigurable.isModified).isFalse()
            }
        }

        describe("resets") {
            it("resets all fields properly") {
                GuiActionRunner.execute {
                    frame.spinner("minLength").target().value = 108
                    frame.spinner("maxLength").target().value = 183
                    frame.radioButton("enclosureBacktick").target().isSelected = true
                    frame.radioButton("capitalizationLower").target().isSelected = true

                    wordSettingsComponentConfigurable.reset()
                }

                assertThat(wordSettingsComponentConfigurable.isModified).isFalse()
            }
        }
    }
})
