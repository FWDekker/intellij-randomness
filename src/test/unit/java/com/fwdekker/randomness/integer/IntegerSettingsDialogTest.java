package com.fwdekker.randomness.integer;

import com.intellij.openapi.ui.ValidationInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link IntegerSettingsDialog}.
 */
public final class IntegerSettingsDialogTest extends AssertJSwingJUnitTestCase {
    private static final long DEFAULT_MIN_VALUE = 235L;
    private static final long DEFAULT_MAX_VALUE = 834L;

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
    public void testDefaultIsValid() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> integerSettingsDialog.doValidate());

        assertThat(validationInfo).isNull();
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
    public void testMinValueString() {
        frame.spinner("minValue").enterText("qzH");

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> integerSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minValue").target());
        assertThat(validationInfo.message).isEqualTo("Minimum value must be an integer.");
    }

    @Test
    public void testMaxValueString() {
        frame.spinner("maxValue").enterText("NXt");

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> integerSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value must be an integer.");
    }

    @Test
    public void testMinValueFloat() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("minValue").target().setValue(285.21f);
            return integerSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minValue").target());
        assertThat(validationInfo.message).isEqualTo("Minimum value must be an integer.");
    }

    @Test
    public void testMaxValueFloat() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("maxValue").target().setValue(490.34f);
            return integerSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value must be an integer.");
    }

    @Test
    public void testMaxValueGreaterThanMinValue() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("maxValue").target().setValue(DEFAULT_MIN_VALUE - 1);
            return integerSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value cannot be smaller than minimum value.");
    }


    @Test
    public void testSaveSettingsInvalid() {
        frame.spinner("minValue").enterText("eWb");

        assertThatThrownBy(() -> GuiActionRunner.execute(() -> integerSettingsDialog.saveSettings()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Settings were committed, but input could not be parsed.");
    }

    @Test
    public void testSaveSettingsValid() {
        GuiActionRunner.execute(() -> {
            frame.spinner("minValue").target().setValue(239L);
            frame.spinner("maxValue").target().setValue(397L);

            integerSettingsDialog.saveSettings();
        });

        assertThat(integerSettings.getMinValue()).isEqualTo(239L);
        assertThat(integerSettings.getMaxValue()).isEqualTo(397L);
    }

    @Test
    public void testSaveLongs() {
        GuiActionRunner.execute(() -> {
            frame.spinner("minValue").target().setValue((long) Integer.MAX_VALUE + 1L);
            frame.spinner("maxValue").target().setValue((long) Integer.MAX_VALUE + 2L);

            integerSettingsDialog.saveSettings();
        });

        assertThat(integerSettings.getMinValue()).isEqualTo(2147483648L);
        assertThat(integerSettings.getMaxValue()).isEqualTo(2147483649L);
    }
}
