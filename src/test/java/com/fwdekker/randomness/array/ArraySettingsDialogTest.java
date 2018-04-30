package com.fwdekker.randomness.array;

import com.intellij.openapi.ui.ValidationInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link ArraySettingsDialog}.
 */
public final class ArraySettingsDialogTest extends AssertJSwingJUnitTestCase {
    /**
     * An untouched {@code WordSettings} instance, thus having the default settings.
     */
    private static final ArraySettings DEFAULT_SETTINGS = new ArraySettings();

    private ArraySettings arraySettings;
    private ArraySettingsDialog arraySettingsDialog;
    private FrameFixture frame;


    @Override
    @SuppressWarnings("unchecked")
    protected void onSetUp() {
        arraySettings = new ArraySettings();
        arraySettingsDialog = GuiActionRunner.execute(() -> new ArraySettingsDialog(arraySettings));
        frame = showInFrame(robot(), arraySettingsDialog.createCenterPanel());
    }


    @Test
    public void testDefaultIsValid() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> arraySettingsDialog.doValidate());

        assertThat(validationInfo).isNull();
    }


    @Test
    public void testLoadSettingsCount() {
        frame.spinner("count").requireValue((long) DEFAULT_SETTINGS.getCount());
    }

    @Test
    public void testLoadSettingsBrackets() {
        frame.radioButton("bracketsSquare").requireSelected();
        frame.radioButton("bracketsCurly").requireNotSelected();
        frame.radioButton("bracketsRound").requireNotSelected();
    }

    @Test
    public void testLoadSettingsSeparator() {
        frame.radioButton("separatorComma").requireSelected();
        frame.radioButton("separatorSemicolon").requireNotSelected();
    }

    @Test
    public void testLoadSettingsSpaceAfterSeparator() {
        frame.checkBox("spaceAfterSeparator").requireSelected();
    }


    @Test
    public void testValidateCount() {
        GuiActionRunner.execute(() -> frame.spinner("count").target().setValue(983.24f));

        frame.spinner("count").requireValue(983L);
    }

    @Test
    public void testValidateCountNegative() {
        GuiActionRunner.execute(() -> frame.spinner("count").target().setValue(-172));

        final ValidationInfo validationInfo = arraySettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("count").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value greater than or equal to 1.");
    }

    @Test
    public void testValidateCountOverflow() {
        GuiActionRunner.execute(() -> frame.spinner("count").target().setValue((long) Integer.MAX_VALUE + 2L));

        final ValidationInfo validationInfo = arraySettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("count").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value less than or equal to 2147483647.");
    }


    @Test
    public void testSaveSettingsWithoutParse() {
        GuiActionRunner.execute(() -> {
            frame.spinner("count").target().setValue(642);
            frame.radioButton("bracketsCurly").target().setSelected(true);
            frame.radioButton("separatorSemicolon").target().setSelected(true);
            frame.checkBox("spaceAfterSeparator").target().setSelected(false);
        });

        arraySettingsDialog.saveSettings();

        assertThat(arraySettings.getCount()).isEqualTo(642);
        assertThat(arraySettings.getBrackets()).isEqualTo("{}");
        assertThat(arraySettings.getSeparator()).isEqualTo(";");
        assertThat(arraySettings.isSpaceAfterSeparator()).isEqualTo(false);
    }

    @Test
    public void testSaveSettingsWithParse() {
        frame.spinner("count").enterTextAndCommit("257");
        frame.radioButton("bracketsRound").check();
        frame.radioButton("separatorSemicolon").check();
        frame.checkBox("spaceAfterSeparator").uncheck();

        arraySettingsDialog.saveSettings();

        assertThat(arraySettings.getCount()).isEqualTo(257);
        assertThat(arraySettings.getBrackets()).isEqualTo("()");
        assertThat(arraySettings.getSeparator()).isEqualTo(";");
        assertThat(arraySettings.isSpaceAfterSeparator()).isEqualTo(false);
    }
}
