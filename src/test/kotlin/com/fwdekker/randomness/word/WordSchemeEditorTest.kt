package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.find
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.matcher
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [WordSchemeEditor].
 */
object WordSchemeEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: WordScheme
    lateinit var editor: WordSchemeEditor
    lateinit var wordsEditor: EditorComponentImpl


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = WordScheme()
        editor = guiGet { WordSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)

        wordsEditor = frame.find(matcher(EditorComponentImpl::class.java))
    }

    afterEach {
        frame.cleanUp()
        guiRun { editor.dispose() }
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("loads the scheme's words") {
            guiRun { editor.loadState(WordScheme(words = listOf("summer", "another"))) }

            assertThat(wordsEditor.text).isEqualTo("summer\nanother\n")
        }

        it("loads the scheme's quotation") {
            guiRun { editor.loadState(WordScheme(quotation = "'")) }

            frame.comboBox("quotation").requireSelection("'")
        }

        it("loads the scheme's capitalization") {
            guiRun { editor.loadState(WordScheme(capitalization = CapitalizationMode.LOWER)) }

            frame.comboBox("capitalization").requireSelection("lower")
        }
    }

    describe("readState") {
        it("removes blank lines from the words input") {
            guiRun { runWriteAction { wordsEditor.editor.document.setText("suppose\n  \nsand\n") } }

            assertThat(editor.readState().words).isEqualTo(listOf("suppose", "sand"))
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            guiRun {
                runWriteAction { wordsEditor.editor.document.setText("might\nexpense") }
                frame.comboBox("quotation").target().selectedItem = "'"
                frame.comboBox("capitalization").target().selectedItem = CapitalizationMode.LOWER
            }

            val readScheme = editor.readState()
            assertThat(readScheme.words).isEqualTo(listOf("might", "expense"))
            assertThat(readScheme.quotation).isEqualTo("'")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.LOWER)
        }

        it("returns the loaded state if no editor changes are made") {
            guiRun { frame.comboBox("quotation").target().selectedItem = "'" }
            assertThat(editor.isModified()).isTrue()

            guiRun { editor.loadState(editor.readState()) }
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


    describe("preset insertion") {
        lateinit var firstList: DefaultWordList
        lateinit var firstListAsString: String


        beforeEach {
            firstList = frame.comboBox("presets").target().getItemAt(1) as DefaultWordList
            firstListAsString = firstList.words.joinToString(separator = "\n", postfix = "\n")
        }


        describe("pre-selection") {
            it("selects the placeholder if the initial words do no match any word list") {
                guiRun { editor.loadState(WordScheme(words = listOf("ugly", "wait"))) }

                assertThat(frame.comboBox("presets").target().selectedIndex).isEqualTo(0)
            }

            it("selects the corresponding word list if the contents match that list") {
                guiRun { editor.loadState(WordScheme(words = firstList.words)) }

                assertThat(frame.comboBox("presets").target().selectedIndex).isEqualTo(1)
            }

            it("selects the placeholder if a word is changed") {
                guiRun { editor.loadState(WordScheme(words = firstList.words)) }

                guiRun { runWriteAction { wordsEditor.editor.document.setText("${firstListAsString}jealous") } }

                assertThat(frame.comboBox("presets").target().selectedIndex).isEqualTo(0)
            }

            it("retains the non-placeholder selection if a newline is appended") {
                guiRun { editor.loadState(WordScheme(words = firstList.words)) }

                guiRun { runWriteAction { wordsEditor.editor.document.setText("$firstListAsString\n \n") } }

                assertThat(frame.comboBox("presets").target().selectedIndex).isEqualTo(1)
            }
        }

        describe("insertion") {
            it("does nothing if the placeholder is selected") {
                guiRun { editor.loadState(WordScheme(words = listOf("street", "sell"))) }

                guiRun { frame.comboBox("presets").target().selectedIndex = 0 }

                assertThat(wordsEditor.text).isEqualTo("street\nsell\n")
            }

            it("does nothing if another entry is selected and then the placeholder is selected") {
                guiRun { frame.comboBox("presets").target().selectedIndex = 1 }

                guiRun { frame.comboBox("presets").target().selectedIndex = 0 }

                assertThat(wordsEditor.text).isEqualTo(firstListAsString)
            }

            it("inserts the words of the selected entry") {
                guiRun { editor.loadState(WordScheme(words = listOf("grow", "trip"))) }

                guiRun { frame.comboBox("presets").target().selectedIndex = 1 }

                assertThat(wordsEditor.text).isEqualTo(firstListAsString)
            }
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.comboBox("quotation").target().selectedItem = "`" }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            guiRun { editor.loadState(WordScheme(arrayDecorator = ArrayDecorator(enabled = true))) }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.spinner("arrayMinCount").target().value = 528 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
