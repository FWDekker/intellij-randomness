package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.dateTimeProp
import com.fwdekker.randomness.testhelpers.editorApplyTests
import com.fwdekker.randomness.testhelpers.editorFieldsTests
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.prop
import com.fwdekker.randomness.testhelpers.textProp
import com.fwdekker.randomness.testhelpers.useEdtViolationDetection
import com.fwdekker.randomness.testhelpers.valueProp
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [DateTimeSchemeEditor].
 */
object DateTimeSchemeEditorTest : FunSpec({
    tags(Tags.EDITOR)


    lateinit var frame: FrameFixture
    lateinit var scheme: DateTimeScheme
    lateinit var editor: DateTimeSchemeEditor


    useEdtViolationDetection()

    beforeNonContainer {
        scheme = DateTimeScheme()
        editor = guiGet { DateTimeSchemeEditor(scheme) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
    }


    context("input handling") {
        test("binds the minimum and maximum times") {
            guiRun { frame.textBox("maxDateTime").dateTimeProp().set(-368L) }

            guiGet { frame.textBox("minDateTime").dateTimeProp().get() } shouldBe -368L
            guiGet { frame.textBox("maxDateTime").dateTimeProp().get() } shouldBe -368L
        }
    }


    include(editorApplyTests { editor })

    include(
        editorFieldsTests(
            { editor },
            mapOf(
                "minDateTime" to {
                    row(frame.textBox("minDateTime").dateTimeProp(), editor.scheme::minDateTime.prop(), 369L)
                },
                "maxDateTime" to {
                    row(frame.textBox("maxDateTime").dateTimeProp(), editor.scheme::maxDateTime.prop(), 822L)
                },
                "pattern" to {
                    row(frame.textBox("pattern").textProp(), editor.scheme::pattern.prop(), "dd/MM/yyyy")
                },
                "arrayDecorator" to {
                    row(frame.spinner("arrayMaxCount").valueProp(), editor.scheme.arrayDecorator::maxCount.prop(), 7)
                },
            )
        )
    )
})
