package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.isSelectedProp
import com.fwdekker.randomness.itemProp
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
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [StringSchemeEditor].
 */
object StringSchemeEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: StringScheme
    lateinit var editor: StringSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = StringScheme()
        editor = guiGet { StringSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    test("'apply' makes no changes by default") {
        val before = editor.scheme.deepCopy(retainUuid = true)

        guiRun { editor.apply() }

        before shouldBe editor.scheme
    }

    test("fields") {
        forAll(
            //@formatter:off
            row("pattern", frame.textBox("pattern").textProp(), editor.scheme::pattern.prop(), "[a-z]{3,4}"),
            row("isRegex", frame.checkBox("isRegex").isSelectedProp(), editor.scheme::isRegex.prop(), false),
            row("removeLookAlikeCharacters", frame.checkBox("removeLookAlikeCharacters").isSelectedProp(), editor.scheme::removeLookAlikeSymbols.prop(), true),
            row("capitalization", frame.comboBox("capitalization").itemProp(), editor.scheme::capitalization.prop(), CapitalizationMode.RANDOM),
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
