package com.fwdekker.randomness.array

import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.isSelectedProp
import com.fwdekker.randomness.itemProp
import com.fwdekker.randomness.matcher
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.valueProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.TitledSeparator
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import javax.swing.JCheckBox


/**
 * GUI tests for [ArrayDecoratorEditor].
 */
object ArrayDecoratorEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: ArrayDecorator
    lateinit var editor: ArrayDecoratorEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = ArrayDecorator(enabled = true)
        editor = guiGet { ArrayDecoratorEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    context("separator visibility") {
        test("shows the separator if the editor is not embedded") {
            frame.panel(matcher(TitledSeparator::class.java)).requireVisible()
        }

        test("hides the separator if the editor is embedded") {
            frame.cleanUp()
            editor = guiGet { ArrayDecoratorEditor(scheme, embedded = true) }
            frame = showInFrame(editor.rootComponent)

            frame.panel(matcher(TitledSeparator::class.java)).requireNotVisible()
        }
    }

    context("input handling") {
        test("truncates decimals in the minimum count") {
            guiRun { frame.spinner("arrayMinCount").target().value = 983.24f }

            frame.spinner("arrayMinCount").requireValue(983)
        }

        test("truncates decimals in the maximum count") {
            guiRun { frame.spinner("arrayMaxCount").target().value = 881.78f }

            frame.spinner("arrayMaxCount").requireValue(881)
        }

        test("binds the minimum and maximum counts") {
            guiRun { frame.spinner("arrayMinCount").target().value = 188L }

            frame.spinner("arrayMinCount").requireValue(188L)
            frame.spinner("arrayMaxCount").requireValue(188L)
        }

        context("panel enabled state") {
            test("disables components if enabled is deselected") {
                guiRun { frame.checkBox("arrayEnabled").target().isSelected = false }

                frame.spinner("arrayMinCount").requireDisabled()
            }

            test("enables components if enabled is reselected") {
                guiRun {
                    frame.checkBox("arrayEnabled").target().isSelected = false
                    frame.checkBox("arrayEnabled").target().isSelected = true
                }

                frame.spinner("arrayMinCount").requireEnabled()
            }

            test("keeps components visible if the editor is embedded") {
                frame.cleanUp()
                editor = guiGet { ArrayDecoratorEditor(scheme, embedded = true) }
                frame = showInFrame(editor.rootComponent)

                // This special matcher does not require the component to be visible
                frame.checkBox(matcher(JCheckBox::class.java) { it.name == "arrayEnabled" }).requireSelected()

                frame.spinner("arrayMinCount").requireEnabled()
            }

            test("disables space-after-separator if decorator is enabled but separator is a newline") {
                scheme.enabled = false
                scheme.separator = "\\n"
                guiRun { editor.reset() }

                frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
            }
        }

        context("space-after-separator enabled state") {
            context("embedded") {
                beforeEach {
                    frame.cleanUp()
                    editor = guiGet { ArrayDecoratorEditor(scheme, embedded = true) }
                    frame = showInFrame(editor.rootComponent)
                }


                test("disables space-after-separator if separator is a newline") {
                    guiRun { frame.comboBox("arraySeparator").target().selectedItem = "\\n" }

                    frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
                }

                test("enables space-after-separator if separator is not a newline") {
                    guiRun { frame.comboBox("arraySeparator").target().selectedItem = "," }

                    frame.checkBox("arraySpaceAfterSeparator").requireEnabled()
                }
            }

            context("not embedded") {
                test("disables space-after-separator if separator is a newline") {
                    guiRun { frame.comboBox("arraySeparator").target().selectedItem = "\\n" }

                    frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
                }

                test("enables space-after-separator if separator is not a newline") {
                    guiRun { frame.comboBox("arraySeparator").target().selectedItem = "," }

                    frame.checkBox("arraySpaceAfterSeparator").requireEnabled()
                }

                test("disables space-after-separator if separator is not a newline, but decorator is disabled") {
                    scheme.enabled = false
                    scheme.separator = ","
                    guiRun { editor.reset() }

                    frame.checkBox("arraySpaceAfterSeparator").requireDisabled()
                }
            }
        }
    }

    context("'apply' makes no changes by default") {
        val before = editor.scheme.deepCopy(retainUuid = true)

        guiRun { editor.apply() }

        before shouldBe editor.scheme
    }

    context("fields") {
        forAll(
            //@formatter:off
            row("minCount", frame.spinner("minCount").valueProp(), editor.scheme::minCount.prop(), 275L),
            row("maxCount", frame.spinner("maxCount").valueProp(), editor.scheme::maxCount.prop(), 483L),
            row("separatorEnabled", frame.checkBox("arraySeparatorEnabled").isSelectedProp(), editor.scheme::separatorEnabled.prop(), false),
            row("separator", frame.comboBox("arraySeparator").itemProp(), editor.scheme::separator.prop(), " - "),
            row("affixDecorator", frame.comboBox("affixDescriptor").itemProp(), editor.scheme.affixDecorator::descriptor.prop(), "{@}"),
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
