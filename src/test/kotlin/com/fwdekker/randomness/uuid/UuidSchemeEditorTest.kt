package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Timestamp
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.editorApplyTests
import com.fwdekker.randomness.testhelpers.editorFieldsTests
import com.fwdekker.randomness.testhelpers.find
import com.fwdekker.randomness.testhelpers.isSelectedProp
import com.fwdekker.randomness.testhelpers.itemProp
import com.fwdekker.randomness.testhelpers.matcher
import com.fwdekker.randomness.testhelpers.prop
import com.fwdekker.randomness.testhelpers.runEdt
import com.fwdekker.randomness.testhelpers.textProp
import com.fwdekker.randomness.testhelpers.timestampProp
import com.fwdekker.randomness.testhelpers.useSharedBareIdeaFixture
import com.fwdekker.randomness.testhelpers.valueProp
import com.fwdekker.randomness.ui.JDateTimeField
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.tuple
import io.kotest.matchers.shouldBe
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [UuidSchemeEditor].
 */
object UuidSchemeEditorTest : FunSpec({
    tags(Tags.EDITOR)


    lateinit var frame: FrameFixture

    lateinit var scheme: UuidScheme
    lateinit var editor: UuidSchemeEditor


    useSharedBareIdeaFixture()

    beforeEach {
        scheme = UuidScheme()
        editor = runEdt { UuidSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
    }


    context("input handling") {
        test("expands entered date-times") {
            val min = runEdt { frame.find(matcher(JDateTimeField::class.java, matcher = { it.name == "minDateTime" })) }

            runEdt {
                min.text = "1982"
                min.commitEdit()
            }

            runEdt { min.value.value } shouldBe "1982-01-01 00:00:00.000"
        }

        test("binds the minimum and maximum times") {
            runEdt { frame.textBox("minDateTime").timestampProp().set(Timestamp("4970")) }

            runEdt { frame.textBox("maxDateTime").timestampProp().set(Timestamp("3972")) }

            runEdt { frame.textBox("minDateTime").timestampProp().get() } shouldBe Timestamp("3972")
            runEdt { frame.textBox("maxDateTime").timestampProp().get() } shouldBe Timestamp("3972")
        }
    }


    include(editorApplyTests { editor })

    include(
        editorFieldsTests(
            { editor },
            mapOf(
                "type" to {
                    tuple(
                        frame.comboBox("version").itemProp(),
                        editor.scheme::version.prop(),
                        8,
                    )
                },
                "isUppercase" to {
                    tuple(
                        frame.checkBox("isUppercase").isSelectedProp(),
                        editor.scheme::isUppercase.prop(),
                        true,
                    )
                },
                "addDashes" to {
                    tuple(
                        frame.checkBox("addDashes").isSelectedProp(),
                        editor.scheme::addDashes.prop(),
                        false,
                    )
                },
                "minDateTime" to {
                    tuple(
                        frame.textBox("minDateTime").timestampProp(),
                        editor.scheme::minDateTime.prop(),
                        Timestamp("1989-03-30 13:36:32"),
                    )
                },
                "maxDateTime" to {
                    tuple(
                        frame.textBox("maxDateTime").timestampProp(),
                        editor.scheme::maxDateTime.prop(),
                        Timestamp("3656-11-05 20:58:41"),
                    )
                },
                "affixDecorator" to {
                    tuple(
                        frame.comboBox("affixDescriptor").textProp(),
                        editor.scheme.affixDecorator::descriptor.prop(),
                        "[@]",
                    )
                },
                "arrayDecorator" to {
                    tuple(
                        frame.spinner("arrayMaxCount").valueProp(),
                        editor.scheme.arrayDecorator::maxCount.prop(),
                        7,
                    )
                },
            )
        )
    )
})
