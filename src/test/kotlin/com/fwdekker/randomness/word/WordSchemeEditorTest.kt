package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.TempFileHelper
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.fwdekker.randomness.ui.EditableDatum
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

        dictionarySettings = DictionarySettings()
        scheme = WordScheme(dictionarySettings)
        editor = GuiActionRunner.execute<WordSchemeEditor> { WordSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)

        dictionaryTable = frame.table().target() as TableView<EditableDatum<Dictionary>>
    }

    afterEachTest {
        frame.cleanUp()
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


    describe("loadScheme") {
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
            val allUserDictionaries = setOf("dictionary1.dic", "dictionary2.dic")
            val activeUserDictionaries = setOf("dictionary1.dic")

            GuiActionRunner.execute {
                editor.loadState(
                    WordScheme(
                        dictionarySettings = DictionarySettings(emptySet(), allUserDictionaries),
                        activeUserDictionaryFiles = activeUserDictionaries
                    )
                )
            }

            assertThat(dictionaryTable.items.map { it.datum })
                .containsExactlyElementsOf(allUserDictionaries.map { DictionaryReference(isBundled = false, it) })
            assertThat(dictionaryTable.items.filter { it.active }.map { it.datum })
                .containsExactlyElementsOf(activeUserDictionaries.map { DictionaryReference(isBundled = false, it) })
        }
    }

    describe("readScheme") {
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
            assertThat(editor.readState())
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
            assertThat(editor.readState().decorator)
                .isEqualTo(editor.originalState.decorator)
                .isNotSameAs(editor.originalState.decorator)
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
            GuiActionRunner.execute { editor.loadState(WordScheme(decorator = ArraySchemeDecorator(enabled = true))) }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
