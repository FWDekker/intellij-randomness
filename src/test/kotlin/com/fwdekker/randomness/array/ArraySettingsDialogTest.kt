package com.fwdekker.randomness.array

import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase
import org.junit.Test


/**
 * GUI tests for [ArraySettingsDialog].
 */
class ArraySettingsDialogTest : AssertJSwingJUnitTestCase() {
    companion object {
        /**
         * An untouched `WordSettings` instance, thus having the default settings.
         */
        private val DEFAULT_SETTINGS = ArraySettings()
    }

    private lateinit var arraySettings: ArraySettings
    private lateinit var arraySettingsDialog: ArraySettingsDialog
    private lateinit var frame: FrameFixture


    override fun onSetUp() {
        arraySettings = ArraySettings()
        arraySettingsDialog = GuiActionRunner.execute<ArraySettingsDialog> { ArraySettingsDialog(arraySettings) }
        frame = showInFrame(robot(), arraySettingsDialog.createCenterPanel())
    }


    @Test
    fun testDefaultIsValid() {
        val validationInfo = GuiActionRunner.execute<ValidationInfo> { arraySettingsDialog.doValidate() }

        assertThat(validationInfo).isNull()
    }


    @Test
    fun testLoadSettingsCount() {
        frame.spinner("count").requireValue(DEFAULT_SETTINGS.count.toLong())
    }

    @Test
    fun testLoadSettingsBrackets() {
        frame.radioButton("bracketsSquare").requireSelected()
        frame.radioButton("bracketsCurly").requireNotSelected()
        frame.radioButton("bracketsRound").requireNotSelected()
    }

    @Test
    fun testLoadSettingsSeparator() {
        frame.radioButton("separatorComma").requireSelected()
        frame.radioButton("separatorSemicolon").requireNotSelected()
        frame.radioButton("separatorNewline").requireNotSelected()
    }

    @Test
    fun testLoadSettingsSpaceAfterSeparator() {
        frame.checkBox("spaceAfterSeparator").requireSelected()
    }


    @Test
    fun testValidateCount() {
        GuiActionRunner.execute { frame.spinner("count").target().value = 983.24f }

        frame.spinner("count").requireValue(983L)
    }

    @Test
    fun testValidateCountNegative() {
        GuiActionRunner.execute { frame.spinner("count").target().value = -172 }

        val validationInfo = arraySettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
    }

    @Test
    fun testValidateCountOverflow() {
        GuiActionRunner.execute { frame.spinner("count").target().value = Integer.MAX_VALUE.toLong() + 2L }

        val validationInfo = arraySettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
    }


    @Test
    fun testSaveSettingsWithoutParse() {
        GuiActionRunner.execute {
            frame.spinner("count").target().value = 642
            frame.radioButton("bracketsCurly").target().isSelected = true
            frame.radioButton("separatorSemicolon").target().isSelected = true
            frame.checkBox("spaceAfterSeparator").target().isSelected = false
        }

        arraySettingsDialog.saveSettings()

        assertThat(arraySettings.count).isEqualTo(642)
        assertThat(arraySettings.brackets).isEqualTo("{}")
        assertThat(arraySettings.separator).isEqualTo(";")
        assertThat(arraySettings.isSpaceAfterSeparator).isEqualTo(false)
    }
}
