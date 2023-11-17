package com.fwdekker.randomness.template

import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.matchBundle
import com.fwdekker.randomness.testhelpers.shouldContainExactly
import com.intellij.openapi.options.ConfigurationException
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * Unit tests for [TemplateListConfigurable].
 */
object TemplateListConfigurableTest : FunSpec({
    tags(Tags.IDEA_FIXTURE)


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var configurable: TemplateListConfigurable


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
        TemplateListEditor.useTestSplitter = true
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        configurable = TemplateListConfigurable()
        frame = Containers.showInFrame(guiGet { configurable.createComponent() })
    }

    afterNonContainer {
        frame.cleanUp()
        guiRun { configurable.disposeUIResources() }
        ideaFixture.tearDown()
    }


    context("templateToSelect") {
        test("selects the template with the given UUID") {
            frame.cleanUp()
            guiRun { configurable.disposeUIResources() }

            configurable = TemplateListConfigurable()
            configurable.schemeToSelect = Settings.DEFAULT.templates[2].uuid
            frame = Containers.showInFrame(guiGet { configurable.createComponent() })

            guiGet { frame.tree().target().selectionRows!! } shouldContainExactly arrayOf(4)
        }
    }


    context("isModified") {
        test("returns `false` if no modifications were made") {
            configurable.isModified shouldBe false
        }

        test("returns `true` if modifications were made") {
            guiRun { frame.textBox("templateName").target().text = "New Name" }

            configurable.isModified shouldBe true
        }

        test("returns `true` if no modifications were made but the template list is invalid") {
            Settings.DEFAULT.templates[0].name = ""
            guiRun { configurable.reset() }

            configurable.editor.isModified() shouldBe false
            configurable.isModified shouldBe true
        }
    }

    context("apply") {
        test("throws an exception if the template list is invalid") {
            guiRun { frame.textBox("templateName").target().text = "" }

            shouldThrow<ConfigurationException> { configurable.apply() }
                .title should matchBundle("template_list.error.failed_to_save_settings")
        }

        test("applies the changes") {
            guiRun { frame.textBox("templateName").target().text = "New Name" }

            configurable.apply()

            Settings.DEFAULT.templates[0].name shouldBe "New Name"
        }
    }

    context("reset") {
        test("resets the editor") {
            guiRun { frame.textBox("templateName").target().text = "Changed Name" }

            guiRun { configurable.reset() }

            guiGet { frame.textBox("templateName").target().text } shouldNotBe "Changed Name"
        }
    }
})
