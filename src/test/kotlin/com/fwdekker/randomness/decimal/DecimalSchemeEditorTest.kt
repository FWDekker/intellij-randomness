package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.isSelectedProp
import com.fwdekker.randomness.itemProp
import com.fwdekker.randomness.prop
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
 * GUI tests for [DecimalSchemeEditor].
 */
object DecimalSchemeEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: DecimalScheme
    lateinit var editor: DecimalSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = DecimalScheme()
        editor = guiGet { DecimalSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }



    test("input handling") {
        test("binds the minimum and maximum values") {
            guiRun { frame.spinner("minValue").target().value = 670L }

            frame.spinner("minValue").requireValue(670L)
            frame.spinner("maxValue").requireValue(670L)
        }

        test("truncates decimals in the decimal count") {
            guiRun { frame.spinner("decimalCount").target().value = 693.57f }

            frame.spinner("decimalCount").requireValue(693)
        }

        test("toggles the grouping separator input") {
            test("enables the input if the checkbox is selected") {
                guiRun { frame.checkBox("groupingSeparatorEnabled").target().isSelected = true }

                frame.comboBox("groupingSeparator").requireEnabled()
            }

            test("disables the input if the checkbox is not selected") {
                guiRun { frame.checkBox("groupingSeparatorEnabled").target().isSelected = false }

                frame.comboBox("groupingSeparator").requireDisabled()
            }
        }
    }

    test("'apply' makes no changes by default") {
        // TODO: Move into `test("apply")` block
        val before = editor.scheme.deepCopy(retainUuid = true)

        guiRun { editor.apply() }

        before shouldBe editor.scheme
    }

    test("fields") {
        forAll(
            //@formatter:off
            row("minValue", frame.spinner("minValue").valueProp(), editor.scheme::minValue.prop(), 189L),
            row("maxValue", frame.spinner("maxValue").valueProp(), editor.scheme::maxValue.prop(), 200L),
            row("decimalCount", frame.spinner("decimalCount").valueProp(), editor.scheme::decimalCount.prop(), 4L),
            row("showTrailingZeroes", frame.checkBox("showTrailingZeroes").isSelectedProp(), editor.scheme::showTrailingZeroes.prop(), false),
            row("decimalSeparator", frame.comboBox("decimalSeparator").itemProp(), editor.scheme::decimalSeparator.prop(), "|"),
            row("groupingSeparatorEnabled", frame.checkBox("groupingSeparatorEnabled").isSelectedProp(), editor.scheme::groupingSeparatorEnabled.prop(), true),
            row("groupingSeparator", frame.comboBox("groupingSeparator").itemProp(), editor.scheme::groupingSeparator.prop(), "/"),
            row("affixDecorator", frame.comboBox("affixDescriptor").itemProp(), editor.scheme.affixDecorator::descriptor.prop(), "[@]"),
            row("arrayDecorator", frame.spinner("arrayMinCount").valueProp(), editor.scheme.arrayDecorator::minCount.prop(), 6),
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
