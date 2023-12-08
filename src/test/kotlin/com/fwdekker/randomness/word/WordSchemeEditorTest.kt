package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.editorApplyTestFactory
import com.fwdekker.randomness.editorFieldsTestFactory
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.find
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.itemProp
import com.fwdekker.randomness.testhelpers.matcher
import com.fwdekker.randomness.testhelpers.prop
import com.fwdekker.randomness.testhelpers.textProp
import com.fwdekker.randomness.testhelpers.valueProp
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [WordSchemeEditor].
 */
object WordSchemeEditorTest : FunSpec({
    tags(Tags.EDITOR, Tags.IDEA_FIXTURE, Tags.SWING)


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: WordScheme
    lateinit var editor: WordSchemeEditor
    lateinit var wordsEditor: EditorComponentImpl


    beforeSpec {
        FailOnThreadViolationRepaintManager.install()
    }

    afterSpec {
        FailOnThreadViolationRepaintManager.uninstall()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = WordScheme()
        editor = guiGet { WordSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)

        wordsEditor = frame.find(matcher(EditorComponentImpl::class.java))
    }

    afterNonContainer {
        frame.cleanUp()
        guiRun { Disposer.dispose(editor) }
        ideaFixture.tearDown()
    }


    context("input handling") {
        context("presets") {
            lateinit var firstList: DefaultWordList
            lateinit var firstListAsString: String


            beforeNonContainer {
                firstList = frame.comboBox("presets").target().getItemAt(1) as DefaultWordList
                firstListAsString = firstList.words.joinToString(separator = "\n", postfix = "\n")
            }


            context("pre-selection") {
                test("selects the placeholder if the initial words do no match any word list") {
                    scheme.words = listOf("word1", "word2")
                    guiRun { editor.reset() }

                    frame.comboBox("presets").target().selectedIndex shouldBe 0
                }

                test("selects the corresponding word list if the contents match that list") {
                    scheme.words = firstList.words
                    guiRun { editor.reset() }

                    frame.comboBox("presets").target().selectedIndex shouldBe 1
                }

                test("selects the placeholder if a word is changed") {
                    scheme.words = firstList.words
                    guiRun { editor.reset() }

                    guiRun { runWriteAction { wordsEditor.editor.document.setText("${firstListAsString}jealous") } }

                    frame.comboBox("presets").target().selectedIndex shouldBe 0
                }

                test("retains the non-placeholder selection if a newline is appended") {
                    scheme.words = firstList.words
                    guiRun { editor.reset() }

                    guiRun { runWriteAction { wordsEditor.editor.document.setText("$firstListAsString\n \n") } }

                    frame.comboBox("presets").target().selectedIndex shouldBe 1
                }
            }

            context("insertion") {
                test("does nothing if the placeholder is selected") {
                    scheme.words = listOf("word1", "word2")
                    guiRun { editor.reset() }

                    guiRun { frame.comboBox("presets").target().selectedIndex = 0 }

                    wordsEditor.text shouldBe "word1\nword2\n"
                }

                test("does nothing if another entry is selected and then the placeholder is selected") {
                    guiRun { frame.comboBox("presets").target().selectedIndex = 1 }

                    guiRun { frame.comboBox("presets").target().selectedIndex = 0 }

                    wordsEditor.text shouldBe firstListAsString
                }

                test("inserts the words of the selected entry") {
                    scheme.words = listOf("word1", "word2")
                    guiRun { editor.reset() }

                    guiRun { frame.comboBox("presets").target().selectedIndex = 1 }

                    wordsEditor.text shouldBe firstListAsString
                }
            }
        }

        context("words") {
            test("removes blank lines from the input field") {
                guiRun { runWriteAction { wordsEditor.editor.document.setText("word1\n  \nword2\n") } }

                editor.apply()

                guiGet { editor.scheme.words } shouldContainExactly listOf("word1", "word2")
            }
        }
    }


    include(editorApplyTestFactory { editor })

    include(
        editorFieldsTestFactory(
            { editor },
            mapOf(
                "words" to {
                    row(
                        wordsEditor.editor.document.prop({ it::getWordList }, { it::setWordList }),
                        editor.scheme::words.prop(),
                        listOf("word1", "word2", "word3"),
                    )
                },
                "capitalization" to {
                    row(
                        frame.comboBox("capitalization").itemProp(),
                        editor.scheme::capitalization.prop(),
                        CapitalizationMode.SENTENCE,
                    )
                },
                "affixDecorator" to {
                    row(
                        frame.comboBox("affixDescriptor").textProp(),
                        editor.scheme.affixDecorator::descriptor.prop(),
                        "[@]",
                    )
                },
                "arrayDecorator" to {
                    row(
                        frame.spinner("arrayMaxCount").valueProp(),
                        editor.scheme.arrayDecorator::maxCount.prop(),
                        7,
                    )
                },
            )
        )
    )
})
