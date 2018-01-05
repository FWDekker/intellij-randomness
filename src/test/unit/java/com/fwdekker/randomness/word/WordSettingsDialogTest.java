package com.fwdekker.randomness.word;

import com.fwdekker.randomness.ui.JEditableList;
import com.intellij.openapi.ui.ValidationInfo;
import java.io.File;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JFileChooserFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link WordSettingsDialog}.
 */
public final class WordSettingsDialogTest extends AssertJSwingJUnitTestCase {
    /**
     * An untouched {@code WordSettings} instance, thus having the default settings.
     */
    private static final WordSettings DEFAULT_SETTINGS = new WordSettings();

    private WordSettings wordSettings;
    private WordSettingsDialog wordSettingsDialog;
    private JEditableList<Dictionary> dialogDictionaries;
    private FrameFixture frame;


    @Override
    @SuppressWarnings("unchecked")
    protected void onSetUp() {
        wordSettings = new WordSettings();
        wordSettingsDialog = GuiActionRunner.execute(() -> new WordSettingsDialog(wordSettings));
        frame = showInFrame(robot(), wordSettingsDialog.createCenterPanel());

        dialogDictionaries = (JEditableList<Dictionary>) frame.table("dictionaries").target();
    }


    @Test
    public void testDefaultIsValid() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> wordSettingsDialog.doValidate());

        assertThat(validationInfo).isNull();
    }


    @Test
    public void testLoadSettingsMinLength() {
        frame.spinner("minLength").requireValue((long) DEFAULT_SETTINGS.getMinLength());
    }

    @Test
    public void testLoadSettingsMaxLength() {
        frame.spinner("maxLength").requireValue((long) DEFAULT_SETTINGS.getMaxLength());
    }

    @Test
    public void testLoadSettingsEnclosure() {
        frame.radioButton("enclosureNone").requireNotSelected();
        frame.radioButton("enclosureSingle").requireNotSelected();
        frame.radioButton("enclosureDouble").requireSelected();
        frame.radioButton("enclosureBacktick").requireNotSelected();
    }

    @Test
    public void testLoadSettingsCapitalization() {
        frame.radioButton("capitalizationNormal").requireSelected();
        frame.radioButton("capitalizationUpper").requireNotSelected();
        frame.radioButton("capitalizationLower").requireNotSelected();
    }


    @Test
    @Ignore("Doesn't work with IntelliJ file chooser")
    public void testAddDictionary() {
        frame.button("dictionaryAdd").click();
        JFileChooserFinder.findFileChooser().using(robot())
                .selectFile(getDictionaryFile("dictionaries/simple.dic"))
                .approve();

        assertThat(dialogDictionaries.getEntry(1).toString().replaceAll("\\\\", "/"))
                .endsWith("dictionaries/simple.dic");
    }

    @Test
    @Ignore("Doesn't work with IntelliJ file chooser")
    public void testAddDictionaryDuplicate() {
        GuiActionRunner.execute(() -> dialogDictionaries
                .addEntry(Dictionary.UserDictionary.get(getDictionaryFile("dictionaries/simple.dic")
                                                                .getCanonicalPath())));

        frame.button("dictionaryAdd").click();
        JFileChooserFinder.findFileChooser().using(robot())
                .selectFile(getDictionaryFile("dictionaries/simple.dic"))
                .approve();

        assertThat(dialogDictionaries.getEntryCount())
                .isEqualTo(2);
    }

    @Test
    public void testRemoveBundledDictionary() {
        GuiActionRunner.execute(() -> {
            frame.table("dictionaries").target().clearSelection();
            frame.table("dictionaries").target().addRowSelectionInterval(0, 0);
            frame.button("dictionaryRemove").target().doClick();
        });

        assertThat(frame.table("dictionaries").target().getRowCount()).isEqualTo(1);
    }

    @Test
    public void testRemoveUserDictionary() {
        GuiActionRunner.execute(() -> {
            dialogDictionaries.addEntry(Dictionary.UserDictionary.get(getDictionaryFile("dictionaries/simple.dic")
                                                                              .getCanonicalPath()));
            frame.table("dictionaries").target().clearSelection();
            frame.table("dictionaries").target().addRowSelectionInterval(1, 1);
            frame.button("dictionaryRemove").target().doClick();
        });

        assertThat(frame.table("dictionaries").target().getRowCount()).isEqualTo(1);
    }


    @Test
    public void testValidateMinLengthFloat() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(553.92f));

        frame.spinner("minLength").requireValue(553L);
    }

    @Test
    public void testValidateMinLengthNegative() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(-780));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value greater than or equal to 1.");
    }

    @Test
    public void testValidateMaxLengthFloat() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(796.01f));

        frame.spinner("maxLength").requireValue(796L);
    }

    @Test
    public void testValidateMaxLengthOverflow() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue((long) Integer.MAX_VALUE + 2L));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value less than or equal to 31.");
    }

    @Test
    public void testValidateMaxLengthGreaterThanMinLength() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target()
                .setValue(DEFAULT_SETTINGS.getMinLength() - 1));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("The maximum should be no smaller than the minimum.");
    }

    @Test
    public void testValidateLengthOvershort() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(0));
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(0));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value greater than or equal to 1.");
    }

    @Test
    public void testValidateLengthOverlong() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(1000));
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(1000));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value less than or equal to 31.");
    }

    @Test
    public void testValidateNoDictionaries() {
        GuiActionRunner.execute(() -> frame.table("dictionaries").target().setValueAt(false, 0, 0));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.table("dictionaries").target());
        assertThat(validationInfo.message).isEqualTo("Select at least one dictionary.");
    }


    @Test
    public void testSaveSettingsWithoutParse() {
        GuiActionRunner.execute(() -> {
            frame.spinner("minLength").target().setValue(840);
            frame.spinner("maxLength").target().setValue(861);
            frame.radioButton("enclosureSingle").target().setSelected(true);
            frame.radioButton("capitalizationLower").target().setSelected(true);
        });

        wordSettingsDialog.saveSettings();

        assertThat(wordSettings.getMinLength()).isEqualTo(840);
        assertThat(wordSettings.getMaxLength()).isEqualTo(861);
        assertThat(wordSettings.getEnclosure()).isEqualTo("'");
        assertThat(wordSettings.getCapitalization()).isEqualTo(CapitalizationMode.LOWER);
    }

    @Test
    public void testSaveSettingsWithParse() {
        frame.spinner("minLength").enterTextAndCommit("68");
        frame.spinner("maxLength").enterTextAndCommit("161");
        frame.radioButton("enclosureBacktick").check();
        frame.radioButton("capitalizationUpper").check();

        wordSettingsDialog.saveSettings();

        assertThat(wordSettings.getMinLength()).isEqualTo(68);
        assertThat(wordSettings.getMaxLength()).isEqualTo(161);
        assertThat(wordSettings.getEnclosure()).isEqualTo("`");
        assertThat(wordSettings.getCapitalization()).isEqualTo(CapitalizationMode.UPPER);
    }


    private File getDictionaryFile(final String path) {
        return new File(getClass().getClassLoader().getResource(path).getPath());
    }
}
