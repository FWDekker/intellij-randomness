package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.isSelectedProp
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.textProp
import com.fwdekker.randomness.valueProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
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
 * GUI tests for [FixedLengthDecoratorEditor].
 */
object FixedLengthDecoratorEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: FixedLengthDecorator
    lateinit var editor: FixedLengthDecoratorEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = FixedLengthDecorator(enabled = true)
        editor = guiGet { FixedLengthDecoratorEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    test("event handling") {
        test("disables inputs when the scheme is disabled") {
            guiRun { frame.checkBox("fixedLengthEnabled").target().isSelected = false }

            frame.spinner("fixedLengthLength").requireDisabled()
            frame.textBox("fixedLengthFiller").requireDisabled()
        }

        test("re-enables inputs when the scheme is re-enabled") {
            guiRun { frame.checkBox("fixedLengthEnabled").target().isSelected = false }
            guiRun { frame.checkBox("fixedLengthEnabled").target().isSelected = true }

            frame.spinner("fixedLengthLength").requireEnabled()
            frame.textBox("fixedLengthFiller").requireEnabled()
        }

        test("enforces the filler's length filter") {
            // TODO: Test this filter in other classes as well
            guiRun { frame.textBox("fixedLengthFiller").target().text = "zAt" }

            frame.textBox("fixedLengthFiller").requireText("t")
        }
    }

    test("'apply' makes no changes by default") {
        val before = editor.scheme.deepCopy(retainUuid = true)

        guiRun { editor.apply() }

        before shouldBe editor.scheme
    }

    test("fields") {
        forAll(
            //@formatter:off
            row("enabled", frame.checkBox("fixedLengthEnabled").isSelectedProp(), editor.scheme::enabled.prop(), false),
            row("length", frame.spinner("fixedLengthLength").valueProp(), editor.scheme::length.prop(), 5L),
            row("filler", frame.textBox("fixedLengthFiller").textProp(), editor.scheme::filler.prop(), "."),
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
