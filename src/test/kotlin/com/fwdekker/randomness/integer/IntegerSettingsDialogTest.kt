package com.fwdekker.randomness.integer

import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase
import org.junit.Test


/**
 * GUI tests for [IntegerSettingsDialog].
 */
class IntegerSettingsDialogTest : AssertJSwingJUnitTestCase() {
    companion object {
        private const val DEFAULT_MIN_VALUE = 2_147_483_883L
        private const val DEFAULT_MAX_VALUE = 6_442_451_778L
        private const val DEFAULT_BASE: Long = 10
    }

    private lateinit var integerSettings: IntegerSettings
    private lateinit var integerSettingsDialog: IntegerSettingsDialog
    private lateinit var frame: FrameFixture


    override fun onSetUp() {
        integerSettings = IntegerSettings()
        integerSettings.minValue = DEFAULT_MIN_VALUE
        integerSettings.maxValue = DEFAULT_MAX_VALUE
        integerSettings.base = DEFAULT_BASE.toInt()

        integerSettingsDialog =
            GuiActionRunner.execute<IntegerSettingsDialog> { IntegerSettingsDialog(integerSettings) }
        frame = showInFrame(robot(), integerSettingsDialog.createCenterPanel())
    }


    @Test
    fun testDefaultIsValid() {
        val validationInfo = GuiActionRunner.execute<ValidationInfo> { integerSettingsDialog.doValidate() }

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
    fun testLoadSettingsBase() {
        frame.spinner("base").requireValue(DEFAULT_BASE)
    }


    @Test
    fun testValidateMinValueFloat() {
        GuiActionRunner.execute { frame.spinner("minValue").target().value = 285.21f }

        frame.spinner("minValue").requireValue(285L)
    }

    @Test
    fun testValidateMaxValueFloat() {
        GuiActionRunner.execute { frame.spinner("maxValue").target().value = 490.34f }

        frame.spinner("maxValue").requireValue(490L)
    }

    @Test
    fun testValidateMaxValueGreaterThanMinValue() {
        GuiActionRunner.execute { frame.spinner("maxValue").target().value = DEFAULT_MIN_VALUE - 1 }

        val validationInfo = integerSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
        assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
    }

    @Test
    fun testValidateValueRange() {
        GuiActionRunner.execute {
            frame.spinner("minValue").target().value = Long.MIN_VALUE
            frame.spinner("maxValue").target().value = Long.MAX_VALUE
        }

        val validationInfo = integerSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
        assertThat(validationInfo?.message).isEqualTo("The range should not exceed 9.223372036854776E18.")
    }

    @Test
    fun testValidateBaseFloat() {
        GuiActionRunner.execute { frame.spinner("base").target().value = 22.62f }

        frame.spinner("base").requireValue(22L)
    }


    @Test
    fun testSaveSettingsWithoutParse() {
        GuiActionRunner.execute {
            frame.spinner("minValue").target().value = Integer.MAX_VALUE.toLong() + 1L
            frame.spinner("maxValue").target().value = Integer.MAX_VALUE.toLong() + 2L
            frame.spinner("base").target().value = 14L

            integerSettingsDialog.saveSettings()
        }

        assertThat(integerSettings.minValue).isEqualTo(2_147_483_648L)
        assertThat(integerSettings.maxValue).isEqualTo(2_147_483_649L)
        assertThat(integerSettings.base).isEqualTo(14)
    }
}
