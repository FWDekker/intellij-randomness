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
import com.fwdekker.randomness.testhelpers.ideaWriteEdt
import com.fwdekker.randomness.testhelpers.runEdt
import com.fwdekker.randomness.testhelpers.shouldContainExactly
import com.fwdekker.randomness.testhelpers.shouldMatchBundle
import com.fwdekker.randomness.testhelpers.useSharedBareIdeaFixture
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.openapi.util.Disposer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.Tuple2
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.tuple
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.fixture.AbstractComponentFixture
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [TemplateListEditor].
 */
object TemplateListEditorTest : FunSpec({
    tags(Tags.EDITOR)


    lateinit var frame: FrameFixture

    lateinit var templateList: TemplateList
    lateinit var editor: TemplateListEditor


    useSharedBareIdeaFixture()

    beforeSpec {
        TemplateListEditor.useTestSplitter = true
    }

    afterSpec {
        TemplateListEditor.useTestSplitter = false
    }

    beforeEach {
        templateList =
            TemplateList(
                mutableListOf(
                    Template("Template1", mutableListOf(IntegerScheme(), StringScheme())),
                    Template("Template2", mutableListOf(DecimalScheme(), WordScheme())),
                )
            )
        templateList.applyContext(Settings(templateList = templateList))

        editor = runEdt { TemplateListEditor(templateList) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        runEdt { Disposer.dispose(editor) }
    }


    context("initialSelection") {
        withTests(
            mapOf<String, Tuple2<() -> String?, Int>>(
                "selects the first template if set to `null`" to
                    tuple({ null }, 0),
                "selects the first template if set to an invalid UUID" to
                    tuple({ "invalid" }, 0),
                "selects the template with the given UUID" to
                    tuple({ templateList.templates[1].uuid }, 3),
                "selects the scheme with the given UUID" to
                    tuple({ templateList.templates[1].schemes[0].uuid }, 4),
            )
        ) { (uuid, expectedSelection) ->
            frame.cleanUp()
            runEdt { Disposer.dispose(editor) }

            editor = runEdt { TemplateListEditor(templateList, initialSelection = uuid()) }
            frame = Containers.showInFrame(editor.rootComponent)

            runEdt { frame.tree().target().selectionRows!! } shouldContainExactly arrayOf(expectedSelection)
        }
    }

    context("editor creation") {
        context("loads the appropriate editor") {
            withTests(
                mapOf(
                    "integer" to tuple(IntegerScheme()) { it.spinner("minValue") },
                    "decimal" to tuple(DecimalScheme()) { it.spinner("minValue") },
                    "string" to tuple(StringScheme()) { it.textBox("pattern") },
                    "uuid" to tuple(UuidScheme()) { it.comboBox("version") },
                    "word" to tuple(WordScheme()) { it.comboBox("presets") },
                    "date-time" to tuple(DateTimeScheme()) { it.textBox("minDateTime") },
                    "template reference" to tuple(TemplateReference()) { it.comboBox("template") },
                )
            ) { (scheme, matcher): Tuple2<Scheme, (FrameFixture) -> AbstractComponentFixture<*, *, *>> ->
                templateList.templates.setAll(listOf(Template(schemes = mutableListOf(scheme))))
                templateList.applyContext(templateList.context)

                ideaWriteEdt {
                    editor.reset()
                    frame.tree().target().setSelectionRow(1)
                }

                matcher(frame).requireVisible()
            }
        }

        test("loads an editor for templates") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf())))
            templateList.applyContext(templateList.context)

            runEdt { editor.reset() }

            frame.textBox("templateName").requireVisible()
        }

        test("throws an error for unknown scheme types") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf(DummyScheme()))))
            templateList.applyContext(templateList.context)

            runEdt { editor.reset() }

            shouldThrow<IllegalStateException> { runEdt { frame.tree().target().setSelectionRow(1) } }
                .message shouldMatchBundle "template_list.error.unknown_scheme_type"
        }
    }


    context("doValidate") {
        test("returns `null` for the default list") {
            runEdt { editor.doValidate() } shouldBe null
        }

        test("returns `null` if the template list is valid") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf(DummyScheme()))))
            templateList.applyContext(templateList.context)
            runEdt { editor.reset() }

            runEdt { editor.doValidate() } shouldBe null
        }

        test("returns a string if the template list is invalid") {
            templateList.templates.setAll(listOf(Template(schemes = mutableListOf(DummyScheme(valid = false)))))
            templateList.applyContext(templateList.context)
            runEdt { editor.reset() }

            runEdt { editor.doValidate() } shouldNotBe null
        }
    }

    context("isModified") {
        test("returns `false` if no modifications have been made") {
            runEdt { editor.isModified() } shouldBe false
        }

        test("returns `true` if modifications have been made") {
            runEdt {
                frame.tree().target().selectionRows = intArrayOf(1)
                frame.spinner("minValue").target().value = 1
            }

            runEdt { editor.isModified() } shouldBe true
        }

        test("returns `false` if modifications have been reset") {
            runEdt {
                frame.tree().target().selectionRows = intArrayOf(1)
                frame.spinner("minValue").target().value = 1
            }

            runEdt { editor.reset() }

            runEdt { editor.isModified() } shouldBe false
        }
    }

    context("apply") {
        test("applies changes to the original list") {
            runEdt {
                frame.tree().target().selectionRows = intArrayOf(1)
                frame.spinner("minValue").target().value = 3
            }

            (templateList.templates[0].schemes[0] as IntegerScheme).minValue shouldNotBe 3
            runEdt { editor.apply() }

            (templateList.templates[0].schemes[0] as IntegerScheme).minValue shouldBe 3
        }

        test("does not couple the applied state to the editor's internal state") {
            runEdt {
                frame.tree().target().selectionRows = intArrayOf(2)
                frame.textBox("pattern").target().text = "old"

                runEdt { editor.apply() }

                frame.tree().target().selectionRows = intArrayOf(2)
                frame.textBox("pattern").target().text = "new"
            }

            (templateList.templates[0].schemes[1] as StringScheme).pattern shouldBe "old"
        }
    }

    context("reset") {
        test("undoes changes to the current selection") {
            runEdt {
                frame.tree().target().setSelectionRow(1)
                frame.spinner("minValue").target().value = 7L
            }

            runEdt { editor.reset() }

            frame.spinner("minValue").target().value shouldBe 0L
        }

        test("undoes changes to another selection") {
            runEdt {
                frame.tree().target().setSelectionRow(1)
                frame.spinner("minValue").target().value = 7L
            }

            runEdt {
                frame.tree().target().setSelectionRow(3)
                editor.reset()
            }

            runEdt { frame.tree().target().setSelectionRow(1) }
            runEdt { frame.spinner("minValue").target().value } shouldBe 0L
        }
    }
})
