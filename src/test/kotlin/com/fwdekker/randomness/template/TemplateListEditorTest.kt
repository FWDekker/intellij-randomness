package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.setAll
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.testhelpers.DummyScheme
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.matchBundle
import com.fwdekker.randomness.testhelpers.shouldContainExactly
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.Row2
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.AbstractComponentFixture
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [TemplateListEditor].
 */
object TemplateListEditorTest : FunSpec({
    tags(Tags.EDITOR, Tags.IDEA_FIXTURE, Tags.SWING)


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var templateList: TemplateList
    lateinit var editor: TemplateListEditor


    beforeSpec {
        TemplateListEditor.useTestSplitter = true
        FailOnThreadViolationRepaintManager.install()
    }

    afterSpec {
        FailOnThreadViolationRepaintManager.uninstall()
        TemplateListEditor.useTestSplitter = false
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        templateList =
            TemplateList(
                mutableListOf(
                    Template("Template1", mutableListOf(IntegerScheme(), StringScheme())),
                    Template("Template2", mutableListOf(DecimalScheme(), WordScheme())),
                )
            )
        templateList.applyContext(Settings(templateList = templateList))

        editor = guiGet { TemplateListEditor(templateList) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
        guiRun { Disposer.dispose(editor) }
        ideaFixture.tearDown()
    }


    context("initialSelection") {
        withData(
            mapOf<String, Row2<() -> String?, Int>>(
                "selects the first template if set to `null`" to
                    row({ null }, 0),
                "selects the first template if set to an invalid UUID" to
                    row({ "invalid" }, 0),
                "selects the template with the given UUID" to
                    row({ templateList.templates[1].uuid }, 3),
                "selects the scheme with the given UUID" to
                    row({ templateList.templates[1].schemes[0].uuid }, 4),
            )
        ) { (uuid, expectedSelection) ->
            frame.cleanUp()
            guiRun { Disposer.dispose(editor) }

            editor = guiGet { TemplateListEditor(templateList, initialSelection = uuid()) }
            frame = Containers.showInFrame(editor.rootComponent)

            guiGet { frame.tree().target().selectionRows!! } shouldContainExactly arrayOf(expectedSelection)
        }
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
                templateList.templates.setAll(listOf(Template(schemes = mutableListOf(scheme))))
                templateList.applyContext(templateList.context)

                guiRun {
                    editor.reset()
                    frame.tree().target().setSelectionRow(1)
                }

                matcher(frame).requireVisible()
            }
        }

        test("loads an editor for templates") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf())))
            templateList.applyContext(templateList.context)

            guiRun { editor.reset() }

            frame.textBox("templateName").requireVisible()
        }

        test("throws an error for unknown scheme types") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf(DummyScheme()))))
            templateList.applyContext(templateList.context)

            guiRun { editor.reset() }

            shouldThrow<IllegalStateException> { guiRun { frame.tree().target().setSelectionRow(1) } }
                .message should matchBundle("template_list.error.unknown_scheme_type")
        }
    }


    context("doValidate") {
        test("returns `null` for the default list") {
            guiGet { editor.doValidate() } should beNull()
        }

        test("returns `null` if the template list is valid") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf(DummyScheme()))))
            templateList.applyContext(templateList.context)
            guiRun { editor.reset() }

            guiGet { editor.doValidate() } should beNull()
        }

        test("returns a string if the template list is invalid") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf(DummyScheme(valid = false)))))
            templateList.applyContext(templateList.context)
            guiRun { editor.reset() }

            guiGet { editor.doValidate() } shouldNot beNull()
        }
    }

    context("isModified") {
        test("returns `false` if no modifications have been made") {
            guiGet { editor.isModified() } shouldBe false
        }

        test("returns `true` if modifications have been made") {
            guiRun {
                frame.tree().target().selectionRows = intArrayOf(1)
                frame.spinner("minValue").target().value = 1
            }

            guiGet { editor.isModified() } shouldBe true
        }

        test("returns `false` if modifications have been reset") {
            guiRun {
                frame.tree().target().selectionRows = intArrayOf(1)
                frame.spinner("minValue").target().value = 1
            }

            guiRun { editor.reset() }

            guiGet { editor.isModified() } shouldBe false
        }
    }

    context("apply") {
        test("applies changes to the original list") {
            guiRun {
                frame.tree().target().selectionRows = intArrayOf(1)
                frame.spinner("minValue").target().value = 3
            }

            (templateList.templates[0].schemes[0] as IntegerScheme).minValue shouldNotBe 3
            guiRun { editor.apply() }

            (templateList.templates[0].schemes[0] as IntegerScheme).minValue shouldBe 3
        }

        test("does not couple the applied state to the editor's internal state") {
            guiRun {
                frame.tree().target().selectionRows = intArrayOf(2)
                frame.textBox("pattern").target().text = "old"

                guiRun { editor.apply() }

                frame.tree().target().selectionRows = intArrayOf(2)
                frame.textBox("pattern").target().text = "new"
            }

            (templateList.templates[0].schemes[1] as StringScheme).pattern shouldBe "old"
        }
    }

    context("reset") {
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
            }

            guiRun {
                frame.tree().target().setSelectionRow(3)
                editor.reset()
            }

            guiRun { frame.tree().target().setSelectionRow(1) }
            guiGet { frame.spinner("minValue").target().value } shouldBe 0L
        }
    }
})
