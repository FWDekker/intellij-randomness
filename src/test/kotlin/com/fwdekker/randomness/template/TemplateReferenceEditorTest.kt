package com.fwdekker.randomness.template

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.itemProp
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.valueProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        context = Settings(
            TemplateList(
                mutableListOf(
                    Template("Template0", mutableListOf(DummyScheme())),
                    Template("Template1", mutableListOf(TemplateReference())),
                    Template("Template2", mutableListOf(DummyScheme()))
                )
            )
        )

        reference = context.templates[1].schemes[0] as TemplateReference
        reference.applyContext(context)
        reference.templateUuid = context.templates[0].uuid

        editor = guiGet { TemplateReferenceEditor(reference) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    test("reset") {
        test("selects nothing if the reference refers to null") {
            reference.template = null
            guiRun { editor.reset() }

            guiRun { frame.comboBox("template").itemProp().get() } shouldBe null
        }

        test("does not load the reference's parent as a selectable option") {
            val box = frame.comboBox("template").target()
            val items = (0 until box.itemCount).map { box.getItemAt(it) as Template }

            items shouldNotContain reference.parent
        }
    }

    test("apply") {
        test("makes no changes by default") {
            val before = editor.scheme.deepCopy(retainUuid = true)

            guiRun { editor.apply() }

            before shouldBe editor.scheme
        }
    }

    test("fields") {
        forAll(
            //@formatter:off
            row("template", frame.comboBox("template").itemProp(), editor.scheme::template.prop(), context.templates[2]),
            row("capitalization", frame.comboBox("capitalization").itemProp(), editor.scheme::capitalization.prop(), CapitalizationMode.LOWER),
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
