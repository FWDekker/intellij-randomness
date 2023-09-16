package com.fwdekker.randomness.affix

import com.fwdekker.randomness.afterNonContainer
import com.fwdekker.randomness.beforeNonContainer
import com.fwdekker.randomness.editorApplyTestFactory
import com.fwdekker.randomness.editorFieldsTestFactory
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.requireEnabledIs
import com.fwdekker.randomness.textProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.layout.selected
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import javax.swing.JCheckBox


/**
 * Unit tests for [AffixDecoratorEditor].
 */
object AffixDecoratorEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: AffixDecorator
    lateinit var editor: AffixDecoratorEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = AffixDecorator(enabled = true)
        editor = guiGet { AffixDecoratorEditor(scheme, presets = listOf("a", "b", "c")) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    context("constructor") {
        context("enableMnemonic") {
            test("enables mnemonics if true") {
                frame.checkBox("affixEnabled").target().mnemonic shouldNotBe 0
            }

            test("disables mnemonics if false") {
                frame.cleanUp()
                editor = guiGet { AffixDecoratorEditor(scheme, presets = listOf("."), enableMnemonic = false) }
                frame = Containers.showInFrame(editor.rootComponent)

                frame.checkBox("affixEnabled").target().mnemonic shouldBe 0
            }
        }

        context("namePrefix") {
            test("does not prefix component names if the prefix is an empty string") {
                shouldNotThrowAny { frame.checkBox("affixEnabled") }
                shouldNotThrowAny { frame.comboBox("affixDescriptor") }
            }

            test("prefixes component names by the given prefix and retains camel case") {
                frame.cleanUp()
                editor = guiGet { AffixDecoratorEditor(scheme, presets = listOf("."), namePrefix = "prefix") }
                frame = Containers.showInFrame(editor.rootComponent)

                shouldNotThrowAny { frame.checkBox("prefixAffixEnabled") }
                shouldNotThrowAny { frame.comboBox("prefixAffixDescriptor") }
            }
        }
    }


    context("input handling") {
        context("ifEnabled and affixEnabled") {
            lateinit var toggle: JCheckBox


            beforeNonContainer {
                toggle = guiGet { JCheckBox().also { it.isSelected = false } }
            }


            @Suppress("BooleanLiteralArgument") // Argument names are clear from lambda later on
            withData(
                nameFn = { "enabledIf=${it.a}, checkboxChecked=${it.b}" },
                listOf(
                    row(null, false, true, false),
                    row(null, true, true, true),
                    row(false, false, false, false),
                    row(false, true, false, false),
                    row(true, false, true, false),
                    row(true, true, true, true),
                )
            ) { (predicateState, checkboxState, expectedCheckboxEnabled, expectedDescriptorEnabled) ->
                val predicate = if (predicateState == null) null else toggle.selected

                frame.cleanUp()
                editor = guiGet { AffixDecoratorEditor(scheme, presets = listOf("."), enabledIf = predicate) }
                frame = Containers.showInFrame(editor.rootComponent)

                guiRun {
                    if (predicateState != null) toggle.isSelected = predicateState
                    frame.checkBox().target().isSelected = checkboxState
                }

                frame.checkBox().requireEnabledIs(expectedCheckboxEnabled)
                frame.comboBox().requireEnabledIs(expectedDescriptorEnabled)
            }
        }
    }


    include(editorApplyTestFactory { editor })

    include(
        editorFieldsTestFactory(
            { editor },
            mapOf(
                "descriptor" to
                    row(
                        { frame.comboBox("affixDescriptor").textProp() },
                        { editor.scheme::descriptor.prop() },
                        "non-preset string",
                    ),
            )
        )
    )
})
