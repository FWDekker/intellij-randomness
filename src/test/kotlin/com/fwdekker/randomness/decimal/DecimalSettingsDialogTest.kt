package com.fwdekker.randomness.decimal

import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase
import org.junit.Test


/**
 * GUI tests for [DecimalSettingsDialog].
 */
class DecimalSettingsDialogTest : AssertJSwingJUnitTestCase() {
    companion object {
        private const val DEFAULT_MIN_VALUE = 157.61
        private const val DEFAULT_MAX_VALUE = 408.68
        private const val DEFAULT_DECIMAL_COUNT = 5
    }

    private lateinit var decimalSettings: DecimalSettings
    private lateinit var decimalSettingsDialog: DecimalSettingsDialog
    private lateinit var frame: FrameFixture


    override fun onSetUp() {
        decimalSettings = DecimalSettings()
        decimalSettings.minValue = DEFAULT_MIN_VALUE
        decimalSettings.maxValue = DEFAULT_MAX_VALUE
        decimalSettings.decimalCount = DEFAULT_DECIMAL_COUNT

        decimalSettingsDialog =
            GuiActionRunner.execute<DecimalSettingsDialog> { DecimalSettingsDialog(decimalSettings) }
        frame = showInFrame(robot(), decimalSettingsDialog.createCenterPanel())
    }


    @Test
    fun testDefaultIsValid() {
        val validationInfo = GuiActionRunner.execute<ValidationInfo> { decimalSettingsDialog.doValidate() }

        assertThat(validationInfo).isNull()
    }


    @Test
    fun testLoadSettingsMinValue() {
        frame.spinner("minValue").requireValue(DEFAULT_MIN_VALUE)
    }

    @Test
    fun testLoadSettingsMaxValue() {
        frame.spinner("maxValue").requireValue(DEFAULT_MAX_VALUE)
    }

    @Test
    fun testLoadSettingsDecimalCount() {
        frame.spinner("decimalCount").requireValue(DEFAULT_DECIMAL_COUNT.toLong())
    }


    @Test
    fun testValidateMinValueUnderflow() {
        GuiActionRunner.execute {
            frame.spinner("minValue").target().value = -1E54
            frame.spinner("maxValue").target().value = -1E53
        }

        val validationInfo = decimalSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("minValue").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to -1.0E53.")
    }

    @Test
    fun testValidateMaxValueOverflow() {
        GuiActionRunner.execute { frame.spinner("maxValue").target().value = 1E54 }

        val validationInfo = decimalSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 1.0E53.")
    }

    @Test
    fun testValidateMaxValueGreaterThanMinValue() {
        GuiActionRunner.execute { frame.spinner("maxValue").target().value = DEFAULT_MIN_VALUE - 1 }

        val validationInfo = decimalSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
        assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
    }

    @Test
    fun testValidateValueRange() {
        GuiActionRunner.execute {
            frame.spinner("minValue").target().value = -1E53
            frame.spinner("maxValue").target().value = 1E53
        }

        val validationInfo = decimalSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
        assertThat(validationInfo?.message).isEqualTo("The range should not exceed 1.0E53.")
    }

    @Test
    fun testValidateDecimalCountFloat() {
        GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 693.57f }

        frame.spinner("decimalCount").requireValue(693L)
    }

    @Test
    fun testValidateDecimalCountNegative() {
        GuiActionRunner.execute { frame.spinner("decimalCount").target().value = -851 }

        val validationInfo = decimalSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("decimalCount").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 0.")
    }

    @Test
    fun testValidateDecimalCountOverflow() {
        GuiActionRunner.execute { frame.spinner("decimalCount").target().value = Integer.MAX_VALUE.toLong() + 1L }

        val validationInfo = decimalSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("decimalCount").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
    }


    @Test
    fun testSaveSettingsWithoutParse() {
        GuiActionRunner.execute {
            frame.spinner("minValue").target().value = 112.54
            frame.spinner("maxValue").target().value = 644.74
            frame.spinner("decimalCount").target().value = 485
            frame.radioButton("groupingSeparatorUnderscore").target().isSelected = true
            frame.radioButton("decimalSeparatorComma").target().isSelected = true
        }

        decimalSettingsDialog.saveSettings()

        assertThat(decimalSettings.minValue).isEqualTo(112.54)
        assertThat(decimalSettings.maxValue).isEqualTo(644.74)
        assertThat(decimalSettings.decimalCount).isEqualTo(485)
        assertThat(decimalSettings.groupingSeparator).isEqualTo('_')
        assertThat(decimalSettings.decimalSeparator).isEqualTo(',')
    }
}
