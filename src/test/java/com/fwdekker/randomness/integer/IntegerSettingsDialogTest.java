package com.fwdekker.randomness.integer;

import com.intellij.openapi.ui.ValidationInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link IntegerSettingsDialog}.
 */
public final class IntegerSettingsDialogTest extends AssertJSwingJUnitTestCase {
    private static final int DEFAULT_MIN_VALUE = 235;
    private static final int DEFAULT_MAX_VALUE = 834;

    private IntegerSettings integerSettings;
    private IntegerSettingsDialog integerSettingsDialog;
    private FrameFixture frame;


    @Override
    protected void onSetUp() {
        integerSettings = new IntegerSettings();
        integerSettings.setMinValue(DEFAULT_MIN_VALUE);
        integerSettings.setMaxValue(DEFAULT_MAX_VALUE);

        integerSettingsDialog = GuiActionRunner.execute(() -> new IntegerSettingsDialog(integerSettings));
        frame = showInFrame(robot(), integerSettingsDialog.createCenterPanel());
    }


    @Test
    public void testDefaultMinValue() {
        frame.spinner("minValue").requireValue(DEFAULT_MIN_VALUE);
    }

    @Test
    public void testDefaultMaxValue() {
        frame.spinner("maxValue").requireValue(DEFAULT_MAX_VALUE);
    }

    @Test
    public void testDefaultIsValid() {
        assertThat(integerSettingsDialog.doValidate()).isNull();
    }

    @Test
    public void testMinValueType() {
        GuiActionRunner.execute(() -> frame.spinner("minValue").target().setValue(285.21f));

        final ValidationInfo validationInfo = integerSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minValue").target());
        assertThat(validationInfo.message).isEqualTo("Minimum value must be an integer.");
    }

    @Test
    public void testMaxValueType() {
        GuiActionRunner.execute(() -> frame.spinner("maxValue").target().setValue(490.34f));

        final ValidationInfo validationInfo = integerSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value must be an integer.");
    }

    @Test
    public void testMaxValueGreaterThanMinValue() {
        frame.spinner("maxValue").enterTextAndCommit(Integer.toString(DEFAULT_MIN_VALUE - 1));

        final ValidationInfo validationInfo = integerSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value cannot be smaller than minimum value.");
    }

    @Test
    public void testSaveSettings() {
        frame.spinner("minValue").enterText("239");
        frame.spinner("maxValue").enterText("397");

        GuiActionRunner.execute(() -> integerSettingsDialog.saveSettings());

        assertThat(integerSettings.getMinValue()).isEqualTo(239);
        assertThat(integerSettings.getMaxValue()).isEqualTo(397);
    }
}
