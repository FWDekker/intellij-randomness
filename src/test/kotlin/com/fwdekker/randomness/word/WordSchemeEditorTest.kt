package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.TempFileHelper
import com.fwdekker.randomness.array.ArrayDecorator
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
 * GUI tests for [WordSchemeEditor].
 */
object WordSchemeEditorTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var tempFileHelper: TempFileHelper
    lateinit var frame: FrameFixture

    lateinit var dictionarySettings: DictionarySettings
    lateinit var scheme: WordScheme
    lateinit var editor: WordSchemeEditor
    lateinit var dictionaryTable: TableView<EditableDatum<Dictionary>>


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    @Suppress("UNCHECKED_CAST") // I checked it myself!
    beforeEachTest {
        tempFileHelper = TempFileHelper()
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        dictionarySettings = DictionarySettings()
        scheme = WordScheme().also { it.dictionarySettings += dictionarySettings }
        editor = GuiActionRunner.execute<WordSchemeEditor> { WordSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)

        dictionaryTable = frame.table().target() as TableView<EditableDatum<Dictionary>>
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
        tempFileHelper.cleanUp()
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


    describe("loadState") {
        it("loads the scheme's minimum length") {
            GuiActionRunner.execute { editor.loadState(WordScheme(minLength = 4, maxLength = 12)) }

            frame.spinner("minLength").requireValue(4)
        }

        it("loads the scheme's maximum length") {
            GuiActionRunner.execute { editor.loadState(WordScheme(minLength = 2, maxLength = 6)) }

            frame.spinner("maxLength").requireValue(6)
        }

        it("loads the scheme's enclosure") {
            GuiActionRunner.execute { editor.loadState(WordScheme(enclosure = "'")) }

            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(true)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the scheme's capitalization") {
            GuiActionRunner.execute { editor.loadState(WordScheme(capitalization = CapitalizationMode.LOWER)) }

            frame.radioButton("capitalizationRetain").requireSelected(false)
            frame.radioButton("capitalizationLower").requireSelected(true)
            frame.radioButton("capitalizationUpper").requireSelected(false)
            frame.radioButton("capitalizationRandom").requireSelected(false)
            frame.radioButton("capitalizationSentence").requireSelected(false)
            frame.radioButton("capitalizationFirstLetter").requireSelected(false)
        }

        it("loads the scheme's bundled dictionaries") {
            val allDictionaries = setOf(UserDictionary("dictionary1.dic"), UserDictionary("dictionary2.dic"))
            val activeDictionaries = setOf(UserDictionary("dictionary1.dic"))

            GuiActionRunner.execute {
                editor.loadState(
                    WordScheme(activeDictionaries = activeDictionaries.toSet())
                        .also { it.dictionarySettings += DictionarySettings(allDictionaries.toSet()) }
                )
            }

            assertThat(dictionaryTable.items.map { it.datum })
                .containsExactlyElementsOf(allDictionaries)
            assertThat(dictionaryTable.items.filter { it.active }.map { it.datum })
                .containsExactlyElementsOf(activeDictionaries)
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default enclosure if no enclosure is selected") {
                GuiActionRunner.execute { editor.loadState(WordScheme(enclosure = "unsupported")) }

                assertThat(editor.readState().enclosure).isEqualTo(WordScheme.DEFAULT_ENCLOSURE)
            }

            it("returns default brackets if no brackets are selected") {
                GuiActionRunner.execute { editor.loadState(WordScheme(capitalization = CapitalizationMode.DUMMY)) }

                assertThat(editor.readState().capitalization).isEqualTo(WordScheme.DEFAULT_CAPITALIZATION)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                frame.spinner("minLength").target().value = 840
                frame.spinner("maxLength").target().value = 861
                frame.radioButton("enclosureSingle").target().isSelected = true
                frame.radioButton("capitalizationLower").target().isSelected = true
            }

            val readScheme = editor.readState()
            assertThat(readScheme.minLength).isEqualTo(840)
            assertThat(readScheme.maxLength).isEqualTo(861)
            assertThat(readScheme.enclosure).isEqualTo("'")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.LOWER)
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.spinner("minLength").target().value = 840 }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded scheme") {
            val readState = editor.readState()

            assertThat(readState)
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
            assertThat(+readState.dictionarySettings)
                .isEqualTo(+editor.originalState.dictionarySettings)
                .isNotSameAs(+editor.originalState.dictionarySettings)
            assertThat(readState.arrayDecorator)
                .isEqualTo(editor.originalState.arrayDecorator)
                .isNotSameAs(editor.originalState.arrayDecorator)
        }

        it("returns a scheme with a deep copy of the dictionary settings") {
            GuiActionRunner.execute {
                repeat(dictionaryTable.items.size) { dictionaryTable.listTableModel.removeRow(0) }
                dictionaryTable.listTableModel.addRow(EditableDatum(active = true, UserDictionary("fasten.dic")))
            }

            assertThat((+editor.readState().dictionarySettings).dictionaries)
                .containsExactly(UserDictionary("fasten.dic"))
            assertThat(dictionarySettings.dictionaries)
                .doesNotContain(UserDictionary("fasten.dic"))
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }

    describe("applyState") {
        it("does not change the target's reference to the dictionary settings") {
            GuiActionRunner.execute { editor.applyState() }

            assertThat(+scheme.dictionarySettings).isSameAs(dictionarySettings)
        }

        it("copies the changes from the table into the original state's dictionary settings") {
            GuiActionRunner.execute {
                repeat(dictionaryTable.items.size) { dictionaryTable.listTableModel.removeRow(0) }
                dictionaryTable.listTableModel.addRow(EditableDatum(active = true, UserDictionary("fat.dic")))
            }

            GuiActionRunner.execute { editor.applyState() }

            assertThat(dictionarySettings.dictionaries).containsExactly(UserDictionary("fat.dic"))
        }
    }


    describe("doValidate") {
        it("is invalid if a duplicate dictionary has been defined") {
            val file = tempFileHelper.createFile("lay\ngray", ".dic")
            GuiActionRunner.execute {
                dictionaryTable.listTableModel.addRow(EditableDatum(active = true, UserDictionary(file.absolutePath)))
                dictionaryTable.listTableModel.addRow(EditableDatum(active = true, UserDictionary(file.absolutePath)))
            }

            assertThat(editor.doValidate()).matches("Duplicate dictionary '.*\\.dic'\\.")
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("minLength").target().value = 840 }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(WordScheme(arrayDecorator = ArrayDecorator(enabled = true)))
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
