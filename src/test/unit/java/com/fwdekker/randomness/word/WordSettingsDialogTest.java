package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link WordSettingsDialog}.
 */
public final class WordSettingsDialogTest extends AssertJSwingJUnitTestCase {
    private static final int DEFAULT_MIN_VALUE = 13;
    private static final int DEFAULT_MAX_VALUE = 17;
    private static final String DEFAULT_ENCLOSURE = "\"";
    private static final CapitalizationMode DEFAULT_CAPITALIZATION = CapitalizationMode.UPPER;

    private WordSettings wordSettings;
    private WordSettingsDialog wordSettingsDialog;
    private FrameFixture frame;


    @Override
    protected void onSetUp() {
        wordSettings = new WordSettings();
        wordSettings.setMinLength(DEFAULT_MIN_VALUE);
        wordSettings.setMaxLength(DEFAULT_MAX_VALUE);
        wordSettings.setEnclosure(DEFAULT_ENCLOSURE);
        wordSettings.setCapitalization(DEFAULT_CAPITALIZATION);

        wordSettingsDialog = GuiActionRunner.execute(() -> new WordSettingsDialog(wordSettings));
        frame = showInFrame(robot(), wordSettingsDialog.createCenterPanel());
    }


    @Test
    public void testDefaultIsValid() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> wordSettingsDialog.doValidate());

        assertThat(validationInfo).isNull();
    }


    @Test
    public void testLoadSettingsMinLength() {
        frame.spinner("minLength").requireValue((long) DEFAULT_MIN_VALUE);
    }

    @Test
    public void testLoadSettingsMaxLength() {
        frame.spinner("maxLength").requireValue((long) DEFAULT_MAX_VALUE);
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
        frame.radioButton("capitalizationNormal").requireNotSelected();
        frame.radioButton("capitalizationUpper").requireSelected();
        frame.radioButton("capitalizationLower").requireNotSelected();
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
        assertThat(validationInfo.message).isEqualTo("Please enter a value less than or equal to 2147483647.");
    }

    @Test
    public void testValidateMaxLengthGreaterThanMinLength() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(DEFAULT_MIN_VALUE - 1));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("The maximum should be no smaller than the minimum.");
    }

    @Test
    public void testValidateLengthRangeNoWords() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(1000));
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(1000));

        final ValidationInfo validationInfo = wordSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("No words within that length range could be found.");
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
        frame.radioButton("capitalizationNormal").check();

        wordSettingsDialog.saveSettings();

        assertThat(wordSettings.getMinLength()).isEqualTo(68);
        assertThat(wordSettings.getMaxLength()).isEqualTo(161);
        assertThat(wordSettings.getEnclosure()).isEqualTo("`");
        assertThat(wordSettings.getCapitalization()).isEqualTo(CapitalizationMode.NORMAL);
    }
}
