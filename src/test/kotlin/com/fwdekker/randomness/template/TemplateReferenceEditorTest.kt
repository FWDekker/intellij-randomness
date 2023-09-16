package com.fwdekker.randomness.template

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.afterNonContainer
import com.fwdekker.randomness.beforeNonContainer
import com.fwdekker.randomness.editorFieldsTestFactory
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.itemProp
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.textProp
import com.fwdekker.randomness.valueProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [TemplateReferenceEditor].
 */
object TemplateReferenceEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var context: Settings
    lateinit var reference: TemplateReference
    lateinit var editor: TemplateReferenceEditor

    val theTemplate = Template("Template2", mutableListOf(DummyScheme())) // TODO: Rename this field (I was drunk)


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        context = Settings(
            TemplateList(
                mutableListOf(
                    Template("Template0", mutableListOf(DummyScheme())),
                    Template("Template1", mutableListOf(TemplateReference())),
                    theTemplate,
                )
            )
        )

        reference = context.templates[1].schemes[0] as TemplateReference
        reference.applyContext(context)
        reference.templateUuid = context.templates[0].uuid

        editor = guiGet { TemplateReferenceEditor(reference) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    context("reset") {
        test("selects nothing if the reference refers to null") {
            reference.template = null
            guiRun { editor.reset() }

            guiGet { frame.comboBox("template").itemProp().get() } shouldBe null
        }

        test("does not load the reference's parent as a selectable option") {
            val box = frame.comboBox("template").target()
            val items = (0 until box.itemCount).map { box.getItemAt(it) as Template }

            items shouldNotContain reference.parent
        }
    }

    context("apply") {
        test("makes no changes by default") {
            val before = editor.scheme.deepCopy(retainUuid = true)

            editor.apply()

            before shouldBe editor.scheme
        }
    }

    include(
        editorFieldsTestFactory(
            { editor },
            mapOf(
                "template" to
                    row(
                        { frame.comboBox("template").itemProp() },
                        { editor.scheme::template.prop() },
                        theTemplate,
                    ),
                "capitalization" to
                    row(
                        { frame.comboBox("capitalization").itemProp() },
                        { editor.scheme::capitalization.prop() },
                        CapitalizationMode.LOWER,
                    ),
                "affixDecorator" to
                    row(
                        { frame.comboBox("affixDescriptor").textProp() },
                        { editor.scheme.affixDecorator::descriptor.prop() },
                        "[@]",
                    ),
                "arrayDecorator" to
                    row(
                        { frame.spinner("arrayMaxCount").valueProp() },
                        { editor.scheme.arrayDecorator::maxCount.prop() },
                        7,
                    ),
            )
        )
    )
})
