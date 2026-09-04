package com.fwdekker.randomness.template

import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.testhelpers.runEdt
import com.fwdekker.randomness.testhelpers.shouldContainExactly
import com.fwdekker.randomness.testhelpers.shouldMatchBundle
import com.fwdekker.randomness.testhelpers.useSharedBareIdeaFixture
import com.intellij.openapi.options.ConfigurationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [TemplateListConfigurable].
 */
object TemplateListConfigurableTest : FunSpec({
    lateinit var frame: FrameFixture

    lateinit var configurable: TemplateListConfigurable


    useSharedBareIdeaFixture()

    beforeSpec {
        TemplateListEditor.useTestSplitter = true
    }

    afterSpec {
        TemplateListEditor.useTestSplitter = false
    }

    beforeEach {
        configurable = TemplateListConfigurable()
        frame = Containers.showInFrame(runEdt { configurable.createComponent() })
    }

    afterEach {
        frame.cleanUp()
        runEdt { configurable.disposeUIResources() }
    }


    context("templateToSelect") {
        test("selects the template with the given UUID") {
            frame.cleanUp()
            runEdt { configurable.disposeUIResources() }

            configurable = TemplateListConfigurable()
            configurable.schemeToSelect = Settings.DEFAULT.templates[2].uuid
            frame = Containers.showInFrame(runEdt { configurable.createComponent() })

            runEdt { frame.tree().target().selectionRows!! } shouldContainExactly arrayOf(4)
        }
    }


    context("isModified") {
        test("returns `false` if no modifications were made") {
            configurable.isModified shouldBe false
        }

        test("returns `true` if modifications were made") {
            runEdt { frame.textBox("templateName").target().text = "New Name" }

            configurable.isModified shouldBe true
        }

        test("returns `true` if no modifications were made but the template list is invalid") {
            Settings.DEFAULT.templates[0].name = ""
            runEdt { configurable.reset() }

            configurable.editor.isModified() shouldBe false
            configurable.isModified shouldBe true
        }
    }

    context("apply") {
        test("throws an exception if the template list is invalid") {
            runEdt { frame.textBox("templateName").target().text = "" }

            shouldThrow<ConfigurationException> { configurable.apply() }
                .title shouldMatchBundle "template_list.error.failed_to_save_settings"
        }

        test("applies the changes") {
            runEdt { frame.textBox("templateName").target().text = "New Name" }

            configurable.apply()

            Settings.DEFAULT.templates[0].name shouldBe "New Name"
        }
    }

    context("reset") {
        test("resets the editor") {
            runEdt { frame.textBox("templateName").target().text = "Changed Name" }

            runEdt { configurable.reset() }

            runEdt { frame.textBox("templateName").target().text } shouldNotBe "Changed Name"
        }
    }
})
