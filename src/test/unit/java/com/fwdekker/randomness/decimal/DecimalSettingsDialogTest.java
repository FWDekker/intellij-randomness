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
        frame.spinner("decimalCount").requireValue(DEFAULT_DECIMAL_COUNT);
    }


    @Test
    public void testValidateMinValueString() {
        frame.spinner("minValue").enterText("UtG");

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> decimalSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minValue").target());
        assertThat(validationInfo.message).isEqualTo("Minimum value must be a number.");
    }

    @Test
    public void testValidateMaxValueString() {
        frame.spinner("maxValue").enterText("Odp");

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> decimalSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value must be a number.");
    }

    @Test
    public void testValidateMaxValueGreaterThanMinValue() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("maxValue").target().setValue(DEFAULT_MIN_VALUE - 1);
            return decimalSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value cannot be smaller than minimum value.");
    }

    @Test
    public void testValidateDecimalCountString() {
        frame.spinner("decimalCount").enterText("Ynf");

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> decimalSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Decimal count must be a number.");
    }

    @Test
    public void testValidateDecimalCountFloat() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("decimalCount").target().setValue(693.57f);
            return decimalSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Decimal count must be a whole number.");
    }

    @Test
    public void testValidateDecimalCountNegative() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("decimalCount").target().setValue(-851);
            return decimalSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Decimal count must not be a negative number.");
    }

    @Test
    public void testValidateDecimalCountOverflow() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("decimalCount").target().setValue((long) Integer.MAX_VALUE + 1L);
            return decimalSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Decimal count must not be greater than 2^31-1.");
    }


    @Test
    public void testSaveSettingsWithoutParse() {
        GuiActionRunner.execute(() -> {
            frame.spinner("minValue").target().setValue(112.54);
            frame.spinner("maxValue").target().setValue(644.74);
            frame.spinner("decimalCount").target().setValue(485);

            decimalSettingsDialog.saveSettings();
        });

        assertThat(decimalSettings.getMinValue()).isEqualTo(112.54);
        assertThat(decimalSettings.getMaxValue()).isEqualTo(644.74);
        assertThat(decimalSettings.getDecimalCount()).isEqualTo(485);
    }

    @Test
    public void testSaveSettingsWithParse() {
        frame.spinner("minValue").enterTextAndCommit(doubleToString(418.63));
        frame.spinner("maxValue").enterTextAndCommit(doubleToString(858.59));
        frame.spinner("decimalCount").enterTextAndCommit("99");

        GuiActionRunner.execute(() -> decimalSettingsDialog.saveSettings());

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
        final Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        final NumberFormat formatter = NumberFormat.getInstance(locale);

        return formatter.format(decimal);
    }
}
