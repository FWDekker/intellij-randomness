package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.setAll
import com.fwdekker.randomness.shouldContainExactly
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.JBSplitter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.AbstractComponentFixture
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [TemplateListEditor].
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

    beforeEach {
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

    afterEach {
        frame.cleanUp()
        guiRun { editor.dispose() }
        ideaFixture.tearDown()
    }


    test("reset") {
        // TODO: Rewrite this maybe?

        test("undoes changes to the initial selection") {
            guiRun {
                frame.tree().target().setSelectionRow(1)
                frame.spinner("minValue").target().value = 7
            }

            guiRun { editor.reset() }

            frame.spinner("minValue").target().value shouldBe 0L
        }

        test("retains the selection if `queueSelection` is null") {
            editor.queueSelection = null

            guiRun { editor.reset() }

            frame.tree().target().selectionRows!! shouldContainExactly arrayOf(0)
        }

        test("selects the indicated template after reset") {
            editor.queueSelection = context.templateList.templates[1].uuid

            guiRun { editor.reset() }

            frame.tree().target().selectionRows!! shouldContainExactly arrayOf(3)
        }

        test("does nothing if the indicated template could not be found") {
            editor.queueSelection = "231ee9da-8f72-4535-b770-0119fdf68f70"

            guiRun { editor.reset() }

            frame.tree().target().selectionRows!! shouldContainExactly arrayOf(0)
        }
    }


    test("editor creation") {
        test("loads the appropriate editor") {
            forAll(
                table(
                    headers("name", "scheme", "matcher"),
                    row("integer", IntegerScheme()) { it.spinner("minValue") },
                    row("decimal", DecimalScheme()) { it.spinner("minValue") },
                    row("string", StringScheme()) { it.textBox("pattern") },
                    row("uuid", UuidScheme()) { it.radioButton("type1") },
                    row("word", WordScheme()) { it.comboBox("presets") },
                    row("date-time", DateTimeScheme()) { it.textBox("minDateTime") },
                    row("template reference", TemplateReference()) { it.comboBox("template") },
                )
            ) { _, scheme, matcher: (FrameFixture) -> AbstractComponentFixture<*, *, *> ->
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
            }.message shouldBe "Unknown scheme type 'com.fwdekker.randomness.DummyScheme'."
        }
    }
})
