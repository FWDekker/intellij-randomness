package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.matcher
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
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
    lateinit var frame: FrameFixture

    lateinit var scheme: WordScheme
    lateinit var editor: WordSchemeEditor
    lateinit var wordsEditor: EditorComponentImpl


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = WordScheme()
        editor = GuiActionRunner.execute<WordSchemeEditor> { WordSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)

        wordsEditor = frame.robot().finder().find(matcher(EditorComponentImpl::class.java) { it.isValid })
    }

    afterEachTest {
        frame.cleanUp()
        GuiActionRunner.execute { editor.dispose() }
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("loads the scheme's words") {
            GuiActionRunner.execute { editor.loadState(WordScheme(words = listOf("summer", "another"))) }

            assertThat(wordsEditor.text).isEqualTo("summer\nanother\n")
        }

        it("loads the scheme's quotation") {
            GuiActionRunner.execute { editor.loadState(WordScheme(quotation = "'")) }

            frame.radioButton("quotationNone").requireSelected(false)
            frame.radioButton("quotationSingle").requireSelected(true)
            frame.radioButton("quotationDouble").requireSelected(false)
            frame.radioButton("quotationBacktick").requireSelected(false)
            frame.panel("quotationCustom").radioButton().requireSelected(false)
        }

        it("loads the scheme's custom quotation") {
            GuiActionRunner.execute { editor.loadState(WordScheme(customQuotation = "eN")) }

            frame.panel("quotationCustom").textBox().requireText("eN")
        }

        it("selects the scheme's custom quotation") {
            GuiActionRunner.execute { editor.loadState(WordScheme(quotation = "s", customQuotation = "s")) }

            frame.panel("quotationCustom").radioButton().requireSelected()
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
    }

    describe("readState") {
        describe("defaults") {
            it("returns default quotation if no quotation is selected") {
                GuiActionRunner.execute { editor.loadState(WordScheme(quotation = "unsupported")) }

                assertThat(editor.readState().quotation).isEqualTo(WordScheme.DEFAULT_QUOTATION)
            }

            it("returns default capitalization if unknown capitalization is selected") {
                GuiActionRunner.execute { editor.loadState(WordScheme(capitalization = CapitalizationMode.DUMMY)) }

                assertThat(editor.readState().capitalization).isEqualTo(WordScheme.DEFAULT_CAPITALIZATION)
            }
        }

        it("removes blank lines from the words input") {
            GuiActionRunner.execute { runWriteAction { wordsEditor.editor.document.setText("suppose\n  \nsand\n") } }

            assertThat(editor.readState().words).isEqualTo(listOf("suppose", "sand"))
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute {
                runWriteAction { wordsEditor.editor.document.setText("might\nexpense") }
                frame.radioButton("quotationSingle").target().isSelected = true
                frame.radioButton("capitalizationLower").target().isSelected = true
            }

            val readScheme = editor.readState()
            assertThat(readScheme.quotation).isEqualTo("'")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.LOWER)
            assertThat(readScheme.words).isEqualTo(listOf("might", "expense"))
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.radioButton("quotationSingle").target().isSelected = true }
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
            assertThat(readState.arrayDecorator)
                .isEqualTo(editor.originalState.arrayDecorator)
                .isNotSameAs(editor.originalState.arrayDecorator)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("word list insertion") {
        it("does nothing if the first entry is selected") {
            GuiActionRunner.execute { editor.loadState(WordScheme(words = listOf("street", "sell"))) }

            GuiActionRunner.execute { frame.comboBox("wordListBox").target().selectedIndex = 0 }

            assertThat(wordsEditor.text).isEqualTo("street\nsell\n")
        }

        it("inserts the words of the selected entry") {
            GuiActionRunner.execute { editor.loadState(WordScheme(words = listOf("grow", "trip"))) }

            GuiActionRunner.execute { frame.comboBox("wordListBox").target().selectedIndex = 1 }

            val expectedList = frame.comboBox("wordListBox").target().getItemAt(1) as DefaultWordList
            assertThat(wordsEditor.text).isEqualTo(expectedList.words.joinToString(separator = "\n", postfix = "\n"))
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.radioButton("quotationBacktick").target().isSelected = true }

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
