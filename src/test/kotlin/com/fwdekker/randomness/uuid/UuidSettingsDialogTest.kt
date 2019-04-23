package com.fwdekker.randomness.uuid

import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase
import org.junit.Test


/**
 * GUI tests for [UuidSettingsDialog].
 */
class UuidSettingsDialogTest : AssertJSwingJUnitTestCase() {
    companion object {
        private const val DEFAULT_ENCLOSURE = "'"
    }

    private lateinit var uuidSettings: UuidSettings
    private lateinit var uuidSettingsDialog: UuidSettingsDialog
    private lateinit var frame: FrameFixture


    override fun onSetUp() {
        uuidSettings = UuidSettings()
        uuidSettings.enclosure = DEFAULT_ENCLOSURE

        uuidSettingsDialog = GuiActionRunner.execute<UuidSettingsDialog> { UuidSettingsDialog(uuidSettings) }
        frame = showInFrame(robot(), uuidSettingsDialog.createCenterPanel())
    }


    @Test
    fun testDefaultIsValid() {
        val validationInfo = GuiActionRunner.execute<ValidationInfo> { uuidSettingsDialog.doValidate() }

        assertThat(validationInfo).isNull()
    }


    @Test
    fun testLoadSettingsEnclosure() {
        frame.radioButton("enclosureNone").requireNotSelected()
        frame.radioButton("enclosureSingle").requireSelected()
        frame.radioButton("enclosureDouble").requireNotSelected()
        frame.radioButton("enclosureBacktick").requireNotSelected()
    }


    @Test
    fun testSaveSettingsWithoutParse() {
        GuiActionRunner.execute { frame.radioButton("enclosureBacktick").target().isSelected = true }

        uuidSettingsDialog.saveSettings()

        assertThat(uuidSettings.enclosure).isEqualTo("`")
    }
}
