package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.setAll
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.testhelpers.DummyScheme
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.shouldContainExactly
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.JBSplitter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.Row2
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.AbstractComponentFixture
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [TemplateListEditor].
 */
object TemplateListEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var context: Settings
    lateinit var editor: TemplateListEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()

        TemplateListEditor.createSplitter =
            { vertical, proportionKey, defaultProportion -> JBSplitter(vertical, proportionKey, defaultProportion) }
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        context = Settings(
            TemplateList(
                mutableListOf(
                    Template("Template1", mutableListOf(IntegerScheme(), StringScheme())),
                    Template("Template2", mutableListOf(DecimalScheme(), WordScheme()))
                )
            )
        )
        context.templateList.applyContext(context)

        editor = guiGet { TemplateListEditor(context) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
        guiRun { editor.dispose() }
        ideaFixture.tearDown()
    }


    context("editor creation") {
        context("loads the appropriate editor") {
            withData(
                mapOf(
                    "integer" to row(IntegerScheme()) { it.spinner("minValue") },
                    "decimal" to row(DecimalScheme()) { it.spinner("minValue") },
                    "string" to row(StringScheme()) { it.textBox("pattern") },
                    "uuid" to row(UuidScheme()) { it.radioButton("type1") },
                    "word" to row(WordScheme()) { it.comboBox("presets") },
                    "date-time" to row(DateTimeScheme()) { it.textBox("minDateTime") },
                    "template reference" to row(TemplateReference()) { it.comboBox("template") },
                )
            ) { (scheme, matcher): Row2<Scheme, (FrameFixture) -> AbstractComponentFixture<*, *, *>> ->
                context.templates.setAll(listOf(Template(schemes = mutableListOf(scheme))))
                context.templateList.applyContext(context)

                guiRun {
                    editor.reset()
                    frame.tree().target().setSelectionRow(1)
                }

                matcher(frame).requireVisible()
            }
        }

        test("loads an editor for templates") {
            context.templates.setAll(listOf(Template(schemes = mutableListOf())))
            context.templateList.applyContext(context)

            guiRun { editor.reset() }

            frame.textBox("templateName").requireVisible()
        }

        test("throws an error for unknown scheme types") {
            context.templates.setAll(listOf(Template(schemes = mutableListOf(DummyScheme()))))
            context.templateList.applyContext(context)

            shouldThrow<IllegalStateException> {
                guiRun {
                    editor.reset()
                    frame.tree().target().setSelectionRow(1)
                }
            }.message shouldBe "Unknown scheme type 'com.fwdekker.randomness.testhelpers.DummyScheme'."
        }
    }


    context("reset") {
        context("undoing changes") {
            test("undoes changes to the current selection") {
                guiRun {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 7L
                }

                guiRun { editor.reset() }

                frame.spinner("minValue").target().value shouldBe 0L
            }

            test("undoes changes to another selection") {
                guiRun {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value = 7L
                    frame.tree().target().setSelectionRow(3)
                }

                guiRun { editor.reset() }

                guiRun {
                    frame.tree().target().setSelectionRow(1)
                    frame.spinner("minValue").target().value shouldBe 0L
                }
            }
        }

        context("selection") {
            test("retains the existing selection if `queueSelection` is null") {
                guiRun { frame.tree().target().selectionRows = intArrayOf(1) }

                editor.queueSelection = null
                guiRun { editor.reset() }

                frame.tree().target().selectionRows!! shouldContainExactly arrayOf(1)
            }

            test("resets the existing selection if the indicated scheme could not be found") {
                guiRun { frame.tree().target().selectionRows = intArrayOf(3) }

                editor.queueSelection = "231ee9da-8f72-4535-b770-0119fdf68f70"
                guiRun { editor.reset() }

                frame.tree().target().selectionRows!! shouldContainExactly arrayOf(0)
            }

            test("selects the indicated template") {
                guiRun { frame.tree().target().selectionRows = intArrayOf(4) }

                editor.queueSelection = context.templateList.templates[1].uuid
                guiRun { editor.reset() }

                frame.tree().target().selectionRows!! shouldContainExactly arrayOf(3)
            }

            test("selects the indicated scheme") {
                guiRun { frame.tree().target().selectionRows = intArrayOf(2) }

                editor.queueSelection = context.templateList.templates[1].schemes[0].uuid
                guiRun { editor.reset() }

                frame.tree().target().selectionRows!! shouldContainExactly arrayOf(4)
            }
        }
    }
})
