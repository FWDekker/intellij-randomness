package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.afterNonContainer
import com.fwdekker.randomness.beforeNonContainer
import com.fwdekker.randomness.editorApplyTestFactory
import com.fwdekker.randomness.editorFieldsTestFactory
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
import io.kotest.data.row
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

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = DecimalScheme()
        editor = guiGet { DecimalSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
        ideaFixture.tearDown()
    }



    context("input handling") {
        context("*Value") {
            test("binds the minimum and maximum values") {
                guiRun { frame.spinner("minValue").target().value = 670L }

                frame.spinner("minValue").requireValue(670L)
                frame.spinner("maxValue").requireValue(670L)
            }
        }

        context("decimalCount") {
            test("truncates decimals in the decimal count") {
                guiRun { frame.spinner("decimalCount").target().value = 693.57f }

                frame.spinner("decimalCount").requireValue(693)
            }
        }

        context("groupingSeparator") {
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


    include(editorApplyTestFactory { editor })

    include(
        editorFieldsTestFactory(
            { editor },
            mapOf(
                "minValue" to
                    row(
                        { frame.spinner("minValue").valueProp() },
                        { editor.scheme::minValue.prop() },
                        189L,
                    ),
                "maxValue" to
                    row(
                        { frame.spinner("maxValue").valueProp() },
                        { editor.scheme::maxValue.prop() },
                        200L,
                    ),
                "decimalCount" to
                    row(
                        { frame.spinner("decimalCount").valueProp() },
                        { editor.scheme::decimalCount.prop() },
                        4L,
                    ),
                "showTrailingZeroes" to
                    row(
                        { frame.checkBox("showTrailingZeroes").isSelectedProp() },
                        { editor.scheme::showTrailingZeroes.prop() },
                        false,
                    ),
                "decimalSeparator" to
                    row(
                        { frame.comboBox("decimalSeparator").itemProp() },
                        { editor.scheme::decimalSeparator.prop() },
                        "|",
                    ),
                "groupingSeparatorEnabled" to
                    row(
                        { frame.checkBox("groupingSeparatorEnabled").isSelectedProp() },
                        { editor.scheme::groupingSeparatorEnabled.prop() },
                        true,
                    ),
                "groupingSeparator" to
                    row(
                        { frame.comboBox("groupingSeparator").itemProp() },
                        { editor.scheme::groupingSeparator.prop() },
                        "/",
                    ),
                "affixDecorator" to
                    row(
                        { frame.comboBox("affixDescriptor").itemProp() },
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
