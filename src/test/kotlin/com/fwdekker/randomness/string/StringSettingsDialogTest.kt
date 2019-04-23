package com.fwdekker.randomness.string

import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase
import org.junit.Test


/**
 * GUI tests for [StringSettingsDialog].
 */
class StringSettingsDialogTest : AssertJSwingJUnitTestCase() {
    companion object {
        private const val DEFAULT_MIN_VALUE = 144
        private const val DEFAULT_MAX_VALUE = 719
        private const val DEFAULT_ENCLOSURE = "\""
        private val DEFAULT_ALPHABETS = setOf(Alphabet.ALPHABET, Alphabet.ALPHABET)
    }

    private lateinit var stringSettings: StringSettings
    private lateinit var stringSettingsDialog: StringSettingsDialog
    private lateinit var frame: FrameFixture


    override fun onSetUp() {
        stringSettings = StringSettings()
        stringSettings.minLength = DEFAULT_MIN_VALUE
        stringSettings.maxLength = DEFAULT_MAX_VALUE
        stringSettings.enclosure = DEFAULT_ENCLOSURE
        stringSettings.alphabets = DEFAULT_ALPHABETS.toMutableSet()

        stringSettingsDialog = GuiActionRunner.execute<StringSettingsDialog> { StringSettingsDialog(stringSettings) }
        frame = showInFrame(robot(), stringSettingsDialog.createCenterPanel())
    }


    @Test
    fun testDefaultIsValid() {
        val validationInfo = GuiActionRunner.execute<ValidationInfo> { stringSettingsDialog.doValidate() }

        assertThat(validationInfo).isNull()
    }


    @Test
    fun testLoadSettingsMinLength() {
        frame.spinner("minLength").requireValue(DEFAULT_MIN_VALUE.toLong())
    }

    @Test
    fun testLoadSettingsMaxLength() {
        frame.spinner("maxLength").requireValue(DEFAULT_MAX_VALUE.toLong())
    }

    @Test
    fun testLoadSettingsEnclosure() {
        frame.radioButton("enclosureNone").requireNotSelected()
        frame.radioButton("enclosureSingle").requireNotSelected()
        frame.radioButton("enclosureDouble").requireSelected()
        frame.radioButton("enclosureBacktick").requireNotSelected()
    }

    @Test
    fun testLoadSettingsAlphabets() {
        val expectedSelected = DEFAULT_ALPHABETS.map { it.toString() }.toTypedArray()

        frame.list("alphabets").requireSelectedItems(*expectedSelected)
    }


    @Test
    fun testValidateMinLengthFloat() {
        GuiActionRunner.execute { frame.spinner("minLength").target().value = 553.92f }

        frame.spinner("minLength").requireValue(553L)
    }

    @Test
    fun testValidateMinLengthNegative() {
        GuiActionRunner.execute { frame.spinner("minLength").target().value = -161 }

        val validationInfo = stringSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
    }

    @Test
    fun testValidateMaxLengthFloat() {
        GuiActionRunner.execute { frame.spinner("maxLength").target().value = 796.01f }

        frame.spinner("maxLength").requireValue(796L)
    }

    @Test
    fun testValidateMaxLengthOverflow() {
        GuiActionRunner.execute { frame.spinner("maxLength").target().value = Integer.MAX_VALUE.toLong() + 2L }

        val validationInfo = stringSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
    }

    @Test
    fun testValidateMaxLengthGreaterThanMinLength() {
        GuiActionRunner.execute { frame.spinner("maxLength").target().value = DEFAULT_MIN_VALUE - 1 }

        val validationInfo = stringSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
        assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
    }

    @Test
    fun testValidateEmptyAlphabetSelection() {
        GuiActionRunner.execute { frame.list("alphabets").target().clearSelection() }

        val validationInfo = stringSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.list("alphabets").target())
        assertThat(validationInfo?.message).isEqualTo("Please select at least one option.")
    }


    @Test
    fun testSaveSettingsWithoutParse() {
        val newAlphabets = setOf(Alphabet.DIGITS, Alphabet.ALPHABET, Alphabet.SPECIAL)
        val newAlphabetsOrdinals = newAlphabets.map { it.ordinal }

        GuiActionRunner.execute {
            frame.spinner("minLength").target().value = 445
            frame.spinner("maxLength").target().value = 803
            frame.radioButton("enclosureBacktick").target().isSelected = true
            frame.list("alphabets").target().setSelectedIndices(newAlphabetsOrdinals.toIntArray())
        }

        stringSettingsDialog.saveSettings()

        assertThat(stringSettings.minLength).isEqualTo(445)
        assertThat(stringSettings.maxLength).isEqualTo(803)
        assertThat(stringSettings.enclosure).isEqualTo("`")
        assertThat(stringSettings.alphabets).isEqualTo(newAlphabets)
    }
}
