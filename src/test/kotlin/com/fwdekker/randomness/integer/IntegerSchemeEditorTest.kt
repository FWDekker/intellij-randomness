package com.fwdekker.randomness.integer

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
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [IntegerSchemeEditor].
 */
object IntegerSchemeEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: IntegerScheme
    lateinit var editor: IntegerSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = IntegerScheme()
        editor = guiGet { IntegerSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }



    test("input handling") {
        test("truncates decimals in the minimum value") {
            guiRun { frame.spinner("minValue").target().value = 285.21f }

            frame.spinner("minValue").requireValue(285L)
        }

        test("truncates decimals in the maximum value") {
            guiRun { frame.spinner("maxValue").target().value = 490.34f }

            frame.spinner("maxValue").requireValue(490L)
        }

        test("binds the minimum and maximum values") {
            guiRun { frame.spinner("minValue").target().value = 804L }

            frame.spinner("minValue").requireValue(804L)
            frame.spinner("maxValue").requireValue(804L)
        }

        test("truncates decimals in the base") {
            guiRun { frame.spinner("base").target().value = 22.62f }

            frame.spinner("base").requireValue(22)
        }

        test("toggles the grouping separator depending on base value and checkbox state") {
            @Suppress("BooleanLiteralArgument") // Argument labels given in line with `headers`
            forAll(
                table(
                    headers("base", "checkbox checked", "expected checkbox state", "expected input state"),
                    row(8, false, false, false),
                    row(8, true, false, false),
                    row(10, false, true, false),
                    row(10, true, true, true),
                    row(12, false, false, false),
                    row(12, true, false, false),
                )
            ) { base, checkBoxChecked, expectedCheckBoxEnabled, expectedInputEnabled ->
                guiRun {
                    frame.spinner("base").target().value = base
                    frame.checkBox("groupingSeparatorEnabled").target().isSelected = checkBoxChecked
                }

                frame.checkBox("groupingSeparatorEnabled")
                    .let { if (expectedCheckBoxEnabled) it.requireEnabled() else it.requireDisabled() }
                frame.comboBox("groupingSeparator")
                    .let { if (expectedInputEnabled) it.requireEnabled() else it.requireDisabled() }
            }
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
            row("minValue", frame.spinner("minValue").valueProp(), editor.scheme::minValue.prop(), 150L),
            row("maxValue", frame.spinner("maxValue").valueProp(), editor.scheme::maxValue.prop(), 578L),
            row("base", frame.spinner("base").valueProp(), editor.scheme::base.prop(), 14),
            row("isUppercase", frame.checkBox("isUppercase").isSelectedProp(), editor.scheme::isUppercase.prop(), true),
            row("groupingSeparatorEnabled", frame.checkBox("groupingSeparatorEnabled").isSelectedProp(), editor.scheme::groupingSeparatorEnabled.prop(), !IntegerScheme.DEFAULT_GROUPING_SEPARATOR_ENABLED),
            row("groupingSeparator", frame.checkBox("groupingSeparator").isSelectedProp(), editor.scheme::groupingSeparator.prop(), "!"),
            row("affixDecorator", frame.comboBox("affixDescriptor").itemProp(), editor.scheme.affixDecorator::descriptor.prop(), "[@]"),
            row("fixedLengthDecorator", frame.spinner("fixedLengthLength").valueProp(), editor.scheme.fixedLengthDecorator::length.prop(), 9),
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
