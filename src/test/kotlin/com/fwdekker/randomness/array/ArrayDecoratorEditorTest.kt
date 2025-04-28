package com.fwdekker.randomness.array

import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.editorApplyTests
import com.fwdekker.randomness.testhelpers.editorFieldsTests
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.installEdtViolationDetection
import com.fwdekker.randomness.testhelpers.isSelectedProp
import com.fwdekker.randomness.testhelpers.matcher
import com.fwdekker.randomness.testhelpers.prop
import com.fwdekker.randomness.testhelpers.textProp
import com.fwdekker.randomness.testhelpers.valueProp
import com.intellij.ui.TitledSeparator
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import javax.swing.JCheckBox


/**
 * Unit tests for [ArrayDecoratorEditor].
 */
object ArrayDecoratorEditorTest : FunSpec({
    tags(Tags.EDITOR)


    lateinit var frame: FrameFixture
    lateinit var scheme: ArrayDecorator
    lateinit var editor: ArrayDecoratorEditor


    installEdtViolationDetection()

    beforeNonContainer {
        scheme = ArrayDecorator(enabled = true)
        editor = guiGet { ArrayDecoratorEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
    }


    context("constructor") {
        context("embedded") {
            test("shows the titled separator if the editor is not embedded") {
                frame.panel(matcher(TitledSeparator::class.java)).requireVisible()
            }

            test("hides the titled separator if the editor is embedded") {
                frame.cleanUp()
                editor = guiGet { ArrayDecoratorEditor(scheme, embedded = true) }
                frame = showInFrame(editor.rootComponent)

                frame.robot().finder().findAll(matcher(TitledSeparator::class.java)) should beEmpty()
            }
        }
    }


    context("input handling") {
        context("arrayEnabled") {
            test("disables inputs if deselected") {
                guiRun { frame.checkBox("arrayEnabled").target().isSelected = false }

                frame.spinner("arrayMinCount").requireDisabled()
            }

            test("enables inputs if (re)selected") {
                guiRun { frame.checkBox("arrayEnabled").target().isSelected = false }
                guiRun { frame.checkBox("arrayEnabled").target().isSelected = true }

                frame.spinner("arrayMinCount").requireEnabled()
            }

            test("remains disabled and is disconnected from inputs if the editor is embedded") {
                frame.cleanUp()
                scheme.enabled = false
                editor = guiGet { ArrayDecoratorEditor(scheme, embedded = true) }
                frame = showInFrame(editor.rootComponent)

                // This special matcher does not require the component to be visible
                frame.checkBox(matcher(JCheckBox::class.java) { it.name == "arrayEnabled" }).requireNotSelected()
                frame.spinner("arrayMinCount").requireEnabled()
            }
        }

        context("*Count") {
            test("truncates decimals in the minimum count") {
                guiRun { frame.spinner("arrayMinCount").target().value = 983.24f }

                frame.spinner("arrayMinCount").requireValue(983)
            }

            test("truncates decimals in the maximum count") {
                guiRun { frame.spinner("arrayMaxCount").target().value = 881.78f }

                frame.spinner("arrayMaxCount").requireValue(881)
            }

            test("binds the minimum and maximum counts") {
                guiRun { frame.spinner("arrayMinCount").target().value = 188 }

                frame.spinner("arrayMinCount").requireValue(188)
                frame.spinner("arrayMaxCount").requireValue(188)
            }
        }

        context("separator") {
            context("embedded") {
                beforeNonContainer {
                    frame.cleanUp()
                    editor = guiGet { ArrayDecoratorEditor(scheme, embedded = true) }
                    frame = showInFrame(editor.rootComponent)
                }


                test("disables separator if checkbox is disabled") {
                    guiRun { frame.checkBox("arraySeparatorEnabled").target().isSelected = false }

                    frame.comboBox("arraySeparator").requireDisabled()
                }

                test("enables separator if checkbox is enabled") {
                    guiRun { frame.checkBox("arraySeparatorEnabled").target().isSelected = true }

                    frame.comboBox("arraySeparator").requireEnabled()
                }
            }

            context("not embedded") {
                test("disables separator if panel is disabled and checkbox is disabled") {
                    guiRun { frame.checkBox("arraySeparatorEnabled").target().isSelected = false }

                    frame.comboBox("arraySeparator").requireDisabled()
                }

                test("disables separator if panel is disabled and checkbox is enabled") {
                    guiRun { frame.checkBox("arrayEnabled").target().isSelected = false }
                    guiRun { frame.checkBox("arraySeparatorEnabled").target().isSelected = true }

                    frame.comboBox("arraySeparator").requireDisabled()
                }

                test("disables separator if panel is enabled and checkbox is disabled") {
                    guiRun { frame.checkBox("arrayEnabled").target().isSelected = true }
                    guiRun { frame.checkBox("arraySeparatorEnabled").target().isSelected = false }

                    frame.comboBox("arraySeparator").requireDisabled()
                }

                test("enables separator if panel is enabled and checkbox is enabled") {
                    guiRun { frame.checkBox("arrayEnabled").target().isSelected = true }
                    guiRun { frame.checkBox("arraySeparatorEnabled").target().isSelected = true }

                    frame.comboBox("arraySeparator").requireEnabled()
                }
            }
        }
    }


    include(editorApplyTests { editor })

    include(
        editorFieldsTests(
            { editor },
            mapOf(
                "enabled" to {
                    row(
                        frame.checkBox("arrayEnabled").isSelectedProp(),
                        editor.scheme::enabled.prop(),
                        false,
                    )
                },
                "minCount" to {
                    row(
                        frame.spinner("arrayMinCount").valueProp(),
                        editor.scheme::minCount.prop(),
                        1,
                    )
                },
                "maxCount" to {
                    row(
                        frame.spinner("arrayMaxCount").valueProp(),
                        editor.scheme::maxCount.prop(),
                        483,
                    )
                },
                "separatorEnabled" to {
                    row(
                        frame.checkBox("arraySeparatorEnabled").isSelectedProp(),
                        editor.scheme::separatorEnabled.prop(),
                        false,
                    )
                },
                "separator" to {
                    row(
                        frame.comboBox("arraySeparator").textProp(),
                        editor.scheme::separator.prop(),
                        " - ",
                    )
                },
                "affixDecorator" to {
                    row(
                        frame.comboBox("arrayAffixDescriptor").textProp(),
                        editor.scheme.affixDecorator::descriptor.prop(),
                        "{@}",
                    )
                },
            )
        )
    )
})
