package com.fwdekker.randomness.template

import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.textProp
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [TemplateEditor].
 */
object TemplateEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var frame: FrameFixture

    lateinit var template: Template
    lateinit var editor: TemplateEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        template = Template()
        editor = guiGet { TemplateEditor(template) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
    }


    test("'apply' makes no changes by default") {
        val before = editor.scheme.deepCopy(retainUuid = true)

        guiRun { editor.apply() }

        before shouldBe editor.scheme
    }

    test("fields") {
        forAll(
            //@formatter:off
            // TODO: Test text trimming
            row("name", frame.textBox("name").textProp(), editor.scheme::name.prop(), "New Name"),
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
