package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [UuidSchemeEditor].
 */
object UuidSettingsComponentTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var uuidSettings: UuidSettings
    lateinit var uuidSchemeEditor: UuidSchemeEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        uuidSettings = UuidSettings()
            .apply {
                currentScheme.version = 4
                currentScheme.enclosure = "'"
                currentScheme.capitalization = CapitalizationMode.UPPER
                currentScheme.addDashes = false
            }

        uuidSchemeEditor = GuiActionRunner.execute<UuidSchemeEditor> { UuidSchemeEditor(uuidSettings) }
        frame = showInFrame(uuidSchemeEditor.rootComponent)
    }

    afterEachTest {
        ideaFixture.tearDown()
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' version") {
            frame.radioButton("version1").requireSelected(false)
            frame.radioButton("version4").requireSelected(true)
        }

        it("loads the settings' enclosure") {
            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(true)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }

        it("loads the settings' capitalization mode") {
            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(true)
        }

        it("loads the settings' add dashes option") {
            frame.checkBox("addDashesCheckBox").requireSelected(false)
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute { frame.radioButton("version1").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }
            GuiActionRunner.execute { frame.radioButton("capitalizationUpper").target().isSelected = true }
            GuiActionRunner.execute { frame.checkBox("addDashesCheckBox").target().isSelected = true }

            uuidSchemeEditor.saveSettings()

            assertThat(uuidSettings.currentScheme.version).isEqualTo(1)
            assertThat(uuidSettings.currentScheme.enclosure).isEqualTo("`")
            assertThat(uuidSettings.currentScheme.capitalization).isEqualTo(CapitalizationMode.UPPER)
            assertThat(uuidSettings.currentScheme.addDashes).isEqualTo(true)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { uuidSchemeEditor.loadSettings(UuidSettings()) }

            assertThat(uuidSchemeEditor.doValidate()).isNull()
        }
    }
})
