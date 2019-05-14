package com.fwdekker.randomness.uuid

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [UuidSettingsDialog].
 */
object UuidSettingsDialogTest : Spek({
    lateinit var uuidSettings: UuidSettings
    lateinit var uuidSettingsDialog: UuidSettingsDialog
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        uuidSettings = UuidSettings()
        uuidSettings.enclosure = "'"

        uuidSettingsDialog = GuiActionRunner.execute<UuidSettingsDialog> { UuidSettingsDialog(uuidSettings) }
        frame = showInFrame(uuidSettingsDialog.createCenterPanel())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' enclosure") {
            frame.radioButton("enclosureNone").requireSelected(false)
            frame.radioButton("enclosureSingle").requireSelected(true)
            frame.radioButton("enclosureDouble").requireSelected(false)
            frame.radioButton("enclosureBacktick").requireSelected(false)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { uuidSettingsDialog.loadSettings(UuidSettings()) }

            assertThat(uuidSettingsDialog.doValidate()).isNull()
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }

            uuidSettingsDialog.saveSettings()

            assertThat(uuidSettings.enclosure).isEqualTo("`")
        }
    }
})
