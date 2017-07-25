package com.fwdekker.randomness.decimal;

import com.intellij.openapi.ui.ValidationInfo;
import java.text.NumberFormat;
import java.util.Locale;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link DecimalSettingsDialog}.
 */
public final class DecimalSettingsDialogTest extends AssertJSwingJUnitTestCase {
    private static final double DEFAULT_MIN_VALUE = 157.61;
    private static final double DEFAULT_MAX_VALUE = 408.68;
    private static final int DEFAULT_DECIMAL_COUNT = 5;

    private DecimalSettings decimalSettings;
    private DecimalSettingsDialog decimalSettingsDialog;
    private FrameFixture frame;


    @Override
    protected void onSetUp() {
        decimalSettings = new DecimalSettings();
        decimalSettings.setMinValue(DEFAULT_MIN_VALUE);
        decimalSettings.setMaxValue(DEFAULT_MAX_VALUE);
        decimalSettings.setDecimalCount(DEFAULT_DECIMAL_COUNT);

        decimalSettingsDialog = GuiActionRunner.execute(() -> new DecimalSettingsDialog(decimalSettings));
        frame = showInFrame(robot(), decimalSettingsDialog.createCenterPanel());
    }


    @Test
    public void testDefaultIsValid() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> decimalSettingsDialog.doValidate());

        assertThat(validationInfo).isNull();
    }


    @Test
    public void testLoadSettingsMinValue() {
        frame.spinner("minValue").requireValue(DEFAULT_MIN_VALUE);
    }

    @Test
    public void testLoadSettingsMaxValue() {
        frame.spinner("maxValue").requireValue(DEFAULT_MAX_VALUE);
    }

    @Test
    public void testLoadSettingsDecimalCount() {
        frame.spinner("decimalCount").requireValue((long) DEFAULT_DECIMAL_COUNT);
    }


    @Test
    public void testValidateMinValueUnderflow() {
        GuiActionRunner.execute(() -> {
            frame.spinner("minValue").target().setValue(-1E54);
            frame.spinner("maxValue").target().setValue(-1E53);
        });

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minValue").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value greater than or equal to -1.0E53.");
    }

    @Test
    public void testValidateMaxValueOverflow() {
        GuiActionRunner.execute(() -> frame.spinner("maxValue").target().setValue(1E54));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value less than or equal to 1.0E53.");
    }

    @Test
    public void testValidateMaxValueGreaterThanMinValue() {
        GuiActionRunner.execute(() -> frame.spinner("maxValue").target().setValue(DEFAULT_MIN_VALUE - 1));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("The maximum should be no smaller than the minimum.");
    }

    @Test
    public void testValidateValueRange() {
        GuiActionRunner.execute(() -> {
            frame.spinner("minValue").target().setValue(-1E53);
            frame.spinner("maxValue").target().setValue(1E53);
        });

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("The range should not exceed 1.0E53.");
    }

    @Test
    public void testValidateDecimalCountFloat() {
        GuiActionRunner.execute(() -> frame.spinner("decimalCount").target().setValue(693.57f));

        frame.spinner("decimalCount").requireValue(693L);
    }

    @Test
    public void testValidateDecimalCountNegative() {
        GuiActionRunner.execute(() -> frame.spinner("decimalCount").target().setValue(-851));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value greater than or equal to 0.");
    }

    @Test
    public void testValidateDecimalCountOverflow() {
        GuiActionRunner.execute(() -> frame.spinner("decimalCount").target().setValue((long) Integer.MAX_VALUE + 1L));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value less than or equal to 2147483647.");
    }


    @Test
    public void testSaveSettingsWithoutParse() {
        GuiActionRunner.execute(() -> {
            frame.spinner("minValue").target().setValue(112.54);
            frame.spinner("maxValue").target().setValue(644.74);
            frame.spinner("decimalCount").target().setValue(485);
        });

        decimalSettingsDialog.saveSettings();

        assertThat(decimalSettings.getMinValue()).isEqualTo(112.54);
        assertThat(decimalSettings.getMaxValue()).isEqualTo(644.74);
        assertThat(decimalSettings.getDecimalCount()).isEqualTo(485);
    }

    @Test
    public void testSaveSettingsWithParse() {
        frame.spinner("minValue").enterTextAndCommit(doubleToString(418.63));
        frame.spinner("maxValue").enterTextAndCommit(doubleToString(858.59));
        frame.spinner("decimalCount").enterTextAndCommit("99");

        decimalSettingsDialog.saveSettings();

        assertThat(decimalSettings.getMinValue()).isEqualTo(418.63);
        assertThat(decimalSettings.getMaxValue()).isEqualTo(858.59);
        assertThat(decimalSettings.getDecimalCount()).isEqualTo(99);
    }


    /**
     * Locale-dependently converts a double into a string.
     *
     * @param decimal a double
     * @return the user's locale's representation of the given double
     */
    private static String doubleToString(final double decimal) {
        final Locale locale = Locale.US;
        final NumberFormat formatter = NumberFormat.getInstance(locale);

        return formatter.format(decimal);
    }
}
