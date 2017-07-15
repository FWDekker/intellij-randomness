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
    public void testDefaultMinValue() {
        frame.spinner("minValue").requireValue(DEFAULT_MIN_VALUE);
    }

    @Test
    public void testDefaultMaxValue() {
        frame.spinner("maxValue").requireValue(DEFAULT_MAX_VALUE);
    }

    @Test
    public void testDefaultDecimalCount() {
        frame.spinner("decimalCount").requireValue(DEFAULT_DECIMAL_COUNT);
    }

    @Test
    public void testDefaultIsValid() {
        assertThat(decimalSettingsDialog.doValidate()).isNull();
    }

    @Test
    public void testMinValueType() {
        GuiActionRunner.execute(() -> frame.spinner("minValue").target().setValue(396.28f));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minValue").target());
        assertThat(validationInfo.message).isEqualTo("Minimum value must be a decimal.");
    }

    @Test
    public void testMaxValueType() {
        GuiActionRunner.execute(() -> frame.spinner("maxValue").target().setValue(355.28f));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value must be a decimal.");
    }

    @Test
    public void testDecimalCountType() {
        GuiActionRunner.execute(() -> frame.spinner("decimalCount").target().setValue(166.98));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Decimal count must be an integer.");
    }

    @Test
    public void testMaxValueGreaterThanMinValue() {
        frame.spinner("maxValue").enterTextAndCommit(Integer.toString((int) DEFAULT_MIN_VALUE - 1));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxValue").target());
        assertThat(validationInfo.message).isEqualTo("Maximum value cannot be smaller than minimum value.");
    }

    @Test
    public void testNegativeDecimalCount() {
        frame.spinner("decimalCount").enterTextAndCommit(Integer.toString(-851));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("decimalCount").target());
        assertThat(validationInfo.message).isEqualTo("Decimal count must be at least 0.");
    }

    @Test
    public void testSaveSettings() {
        frame.spinner("minValue").enterText(doubleToString(418.63));
        frame.spinner("maxValue").enterText(doubleToString(858.59));
        frame.spinner("decimalCount").enterText("99");

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
