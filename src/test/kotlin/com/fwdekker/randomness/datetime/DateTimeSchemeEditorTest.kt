package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.dateTimeProp
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
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
 * GUI tests for [DateTimeSchemeEditor].
 */
object DateTimeSchemeEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture
    lateinit var scheme: DateTimeScheme
    lateinit var editor: DateTimeSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = DateTimeScheme()
        editor = guiGet { DateTimeSchemeEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    context("input handling") {
        test("binds the minimum and maximum times") {
            guiRun { frame.textBox("maxDateTime").dateTimeProp().set(-368L) }

            guiGet { frame.textBox("minDateTime").dateTimeProp().get() } shouldBe -368L
            guiGet { frame.textBox("maxDateTime").dateTimeProp().get() } shouldBe -368L
        }
    }

    context("apply") {
        test("makes no changes by default") {
            val before = editor.scheme.deepCopy(retainUuid = true)

            guiRun { editor.apply() }

            before shouldBe editor.scheme
        }
    }

    context("fields") {
        forAll(
            //@formatter:off
            row("minDateTime", frame.textBox("minDateTime").dateTimeProp(), editor.scheme::minDateTime.prop(), 369L),
            row("maxDateTime", frame.textBox("maxDateTime").dateTimeProp(), editor.scheme::maxDateTime.prop(), 822L),
            row("pattern", frame.textBox("pattern").textProp(), editor.scheme::pattern.prop(), "dd/MM/yyyy"),
            row("arrayDecorator", frame.spinner("arrayMinCount").valueProp(), editor.scheme.arrayDecorator::minCount.prop(), 7),
            //@formatter:on
        ) { description, editorProperty, schemeProperty, value ->
            context(description) {
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
