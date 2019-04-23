package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.ui.JEditableList
import com.intellij.openapi.ui.ValidationInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Fail.fail
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.finder.JFileChooserFinder
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files


/**
 * GUI tests for [WordSettingsDialog].
 */
class WordSettingsDialogTest : AssertJSwingJUnitTestCase() {
    companion object {
        /**
         * An untouched [WordSettings] instance, thus having the default settings.
         */
        private val DEFAULT_SETTINGS = WordSettings()
    }

    private lateinit var wordSettings: WordSettings
    private lateinit var wordSettingsDialog: WordSettingsDialog
    private lateinit var dialogDictionaries: JEditableList<Dictionary>
    private lateinit var frame: FrameFixture


    @Suppress("UNCHECKED_CAST")
    override fun onSetUp() {
        wordSettings = WordSettings()
        wordSettingsDialog = GuiActionRunner.execute<WordSettingsDialog> { WordSettingsDialog(wordSettings) }
        frame = showInFrame(robot(), wordSettingsDialog.createCenterPanel())

        dialogDictionaries = frame.table("dictionaries").target() as JEditableList<Dictionary>
    }


    @Test
    fun testDefaultIsValid() {
        val validationInfo = GuiActionRunner.execute<ValidationInfo> { wordSettingsDialog.doValidate() }

        assertThat(validationInfo).isNull()
    }


    @Test
    fun testLoadSettingsMinLength() {
        frame.spinner("minLength").requireValue(DEFAULT_SETTINGS.minLength.toLong())
    }

    @Test
    fun testLoadSettingsMaxLength() {
        frame.spinner("maxLength").requireValue(DEFAULT_SETTINGS.maxLength.toLong())
    }

    @Test
    fun testLoadSettingsEnclosure() {
        frame.radioButton("enclosureNone").requireNotSelected()
        frame.radioButton("enclosureSingle").requireNotSelected()
        frame.radioButton("enclosureDouble").requireSelected()
        frame.radioButton("enclosureBacktick").requireNotSelected()
    }

    @Test
    fun testLoadSettingsCapitalization() {
        frame.radioButton("capitalizationRetain").requireSelected()
        frame.radioButton("capitalizationSentence").requireNotSelected()
        frame.radioButton("capitalizationFirstLetter").requireNotSelected()
        frame.radioButton("capitalizationUpper").requireNotSelected()
        frame.radioButton("capitalizationLower").requireNotSelected()
    }


    @Test
    @Ignore("Doesn't work with IntelliJ file chooser")
    fun testAddDictionary() {
        frame.button("dictionaryAdd").click()
        JFileChooserFinder.findFileChooser().using(robot())
            .selectFile(getDictionaryFile("dictionaries/simple.dic"))
            .approve()

        assertThat(dialogDictionaries.getEntry(1).toString().replace("\\\\".toRegex(), "/"))
            .endsWith("dictionaries/simple.dic")
    }

    @Test
    @Ignore("Doesn't work with IntelliJ file chooser")
    fun testAddDictionaryDuplicate() {
        GuiActionRunner.execute {
            val dictionary = UserDictionary.cache.get(getDictionaryFile("dictionaries/simple.dic").canonicalPath, true)
            dialogDictionaries.addEntry(dictionary)
        }

        frame.button("dictionaryAdd").click()
        JFileChooserFinder.findFileChooser().using(robot())
            .selectFile(getDictionaryFile("dictionaries/simple.dic"))
            .approve()

        assertThat(dialogDictionaries.entryCount).isEqualTo(2)
    }

    @Test
    fun testRemoveBundledDictionary() {
        GuiActionRunner.execute {
            frame.table("dictionaries").target().clearSelection()
            frame.table("dictionaries").target().addRowSelectionInterval(0, 0)
            frame.button("dictionaryRemove").target().doClick()
        }

        assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(1)
    }

    @Test
    fun testRemoveUserDictionary() {
        GuiActionRunner.execute {
            dialogDictionaries.addEntry(UserDictionary.cache.get(
                getDictionaryFile("dictionaries/simple.dic").canonicalPath, true)
            )
            frame.table("dictionaries").target().clearSelection()
            frame.table("dictionaries").target().addRowSelectionInterval(1, 1)
            frame.button("dictionaryRemove").target().doClick()
        }

        assertThat(frame.table("dictionaries").target().rowCount).isEqualTo(1)
    }


    @Test
    @Suppress("UNCHECKED_CAST")
    fun testValidateInvalidDictionary() {
        val dictionaries = frame.table("dictionaries").target() as JEditableList<Dictionary>

        val dictionaryFile = File.createTempFile("test", "dic")
        Files.write(dictionaryFile.toPath(), "Limbas\nOstiary\nHackee".toByteArray(StandardCharsets.UTF_8))
        val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, true)

        if (!dictionaryFile.delete()) {
            fail("Failed to delete test file as part of test.")
        }

        GuiActionRunner.execute {
            dictionaries.addEntry(dictionary)
            dictionaries.setActiveEntries(listOf<Dictionary>(dictionary))
        }

        val validationInfo = wordSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
    }

    @Test
    fun testValidateMinLengthFloat() {
        GuiActionRunner.execute { frame.spinner("minLength").target().value = 553.92f }

        frame.spinner("minLength").requireValue(553L)
    }

    @Test
    fun testValidateMinLengthNegative() {
        GuiActionRunner.execute { frame.spinner("minLength").target().value = -780 }

        val validationInfo = wordSettingsDialog.doValidate()

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

        val validationInfo = wordSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 2147483647.")
    }

    @Test
    fun testValidateMaxLengthGreaterThanMinLength() {
        GuiActionRunner.execute {
            frame.spinner("maxLength").target().value = DEFAULT_SETTINGS.minLength - 1
        }

        val validationInfo = wordSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxLength").target())
        assertThat(validationInfo?.message).isEqualTo("The maximum should be no smaller than the minimum.")
    }

    @Test
    fun testValidateLengthOvershort() {
        GuiActionRunner.execute { frame.spinner("minLength").target().value = 0 }
        GuiActionRunner.execute { frame.spinner("maxLength").target().value = 0 }

        val validationInfo = wordSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 1.")
    }

    @Test
    fun testValidateLengthOverlong() {
        GuiActionRunner.execute { frame.spinner("minLength").target().value = 1000 }
        GuiActionRunner.execute { frame.spinner("maxLength").target().value = 1000 }

        val validationInfo = wordSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.spinner("minLength").target())
        assertThat(validationInfo?.message).isEqualTo("Please enter a value less than or equal to 31.")
    }

    @Test
    fun testValidateNoDictionaries() {
        GuiActionRunner.execute { frame.table("dictionaries").target().setValueAt(false, 0, 0) }

        val validationInfo = wordSettingsDialog.doValidate()

        assertThat(validationInfo).isNotNull()
        assertThat(validationInfo?.component).isEqualTo(frame.table("dictionaries").target())
        assertThat(validationInfo?.message).isEqualTo("Select at least one dictionary.")
    }


    @Test
    fun testSaveSettingsWithoutParse() {
        GuiActionRunner.execute {
            frame.spinner("minLength").target().value = 840
            frame.spinner("maxLength").target().value = 861
            frame.radioButton("enclosureSingle").target().isSelected = true
            frame.radioButton("capitalizationLower").target().isSelected = true
        }

        wordSettingsDialog.saveSettings()

        assertThat(wordSettings.minLength).isEqualTo(840)
        assertThat(wordSettings.maxLength).isEqualTo(861)
        assertThat(wordSettings.enclosure).isEqualTo("'")
        assertThat(wordSettings.capitalization).isEqualTo(CapitalizationMode.LOWER)
    }


    private fun getDictionaryFile(path: String) = File(javaClass.classLoader.getResource(path).path)
}
