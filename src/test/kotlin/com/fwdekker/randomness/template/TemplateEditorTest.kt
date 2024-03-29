package com.fwdekker.randomness.template

import com.fwdekker.randomness.editorFieldsTestFactory
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.prop
import com.fwdekker.randomness.testhelpers.textProp
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [TemplateEditor].
 */
object TemplateEditorTest : FunSpec({
    tags(Tags.EDITOR, Tags.IDEA_FIXTURE, Tags.SWING)


    lateinit var frame: FrameFixture

    lateinit var template: Template
    lateinit var editor: TemplateEditor


    beforeSpec {
        FailOnThreadViolationRepaintManager.install()
    }

    afterSpec {
        FailOnThreadViolationRepaintManager.uninstall()
    }

    beforeNonContainer {
        template = Template()
        editor = guiGet { TemplateEditor(template) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
    }


    test("'apply' makes no changes by default") {
        val before = editor.scheme.deepCopy(retainUuid = true)

        editor.apply()

        before shouldBe editor.scheme
    }

    include(
        editorFieldsTestFactory(
            { editor },
            mapOf("name" to { row(frame.textBox("templateName").textProp(), editor.scheme::name.prop(), "New Name") })
        )
    )
})
