package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.find
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.itemProp
import com.fwdekker.randomness.matcher
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.valueProp
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [WordSchemeEditor].
 */
object WordSchemeEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


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


    test("preset insertion") {
        lateinit var firstList: DefaultWordList
        lateinit var firstListAsString: String


        beforeEach {
            firstList = frame.comboBox("presets").target().getItemAt(1) as DefaultWordList
            firstListAsString = firstList.words.joinToString(separator = "\n", postfix = "\n")
        }


        test("pre-selection") {
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

        test("insertion") {
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


    test("removes blank lines from the words input") {
        guiRun { runWriteAction { wordsEditor.editor.document.setText("word1\n  \nword2\n") } }

        guiRun { editor.apply() }

        guiGet { editor.scheme.words } shouldContainExactly listOf("word1", "word2")
    }

    test("'apply' makes no changes by default") {
        val before = editor.scheme.deepCopy(retainUuid = true)

        guiRun { editor.apply() }

        before shouldBe editor.scheme
    }

    test("fields") {
        forAll(
            //@formatter:off
            // TODO: Also add word input itself?
            row("capitalization", frame.comboBox("capitalization").itemProp(), editor.scheme::capitalization.prop(), CapitalizationMode.SENTENCE),
            row("affixDecorator", frame.comboBox("affixDescriptor").itemProp(), editor.scheme.affixDecorator::descriptor.prop(), "[@]"),
            row("arrayDecorator", frame.spinner("arrayMinCount").valueProp(), editor.scheme.arrayDecorator::minCount.prop(), 7),
            //@formatter:on
        ) { description, editorProperty, schemeProperty, value ->
            test(description) {
                test("`reset` loads the scheme into the editor") {
                    guiGet { editorProperty.get() } shouldNotBe value

                    schemeProperty.set(value)
                    guiRun { editor.reset() }

                    guiGet { editorProperty.get() } shouldBe value
                }

                test("`apply` saves the editor into the scheme") {
                    schemeProperty.get() shouldNotBe value

                    guiRun { editorProperty.set(value) }
                    guiRun { editor.apply() }

                    schemeProperty.get() shouldBe value
                }

                test("`addChangeListener` invokes the change listener") {
                    var invoked = 0
                    editor.addChangeListener { invoked++ }

                    guiRun { editorProperty.set(value) }

                    invoked shouldBe 1
                }
            }
        }
    }
})
