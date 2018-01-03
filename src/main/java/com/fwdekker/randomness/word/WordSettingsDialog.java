package com.fwdekker.randomness.word;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.common.ValidationException;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random word generation.
 */
@SuppressFBWarnings(
        value = {"UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD"},
        justification = "Initialized by UI framework"
)
final class WordSettingsDialog extends SettingsDialog<WordSettings> {
    private JPanel contentPane;
    private JSpinnerRange lengthRange;
    private JLongSpinner minLength;
    private JLongSpinner maxLength;
    private ButtonGroup enclosureGroup;


    /**
     * Constructs a new {@code WordSettingsDialog} that uses the singleton {@code WordSettings} instance.
     */
    WordSettingsDialog() {
        this(WordSettings.getInstance());
    }

    /**
     * Constructs a new {@code WordSettingsDialog} that uses the given {@code WordSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    WordSettingsDialog(@NotNull final WordSettings settings) {
        super(settings);

        init();
        loadSettings();
    }


    @Override
    @NotNull
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    /**
     * Initialises custom UI components.
     * <p>
     * This method is called by the scene builder at the start of the constructor.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Method used by scene builder
    private void createUIComponents() {
        minLength = new JLongSpinner(1, Integer.MAX_VALUE);
        maxLength = new JLongSpinner(1, Integer.MAX_VALUE);
        lengthRange = new JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE);
    }


    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void loadSettings(@NotNull final WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.setValue(enclosureGroup, settings.getEnclosure());
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void saveSettings(@NotNull final WordSettings settings) {
        settings.setMinLength(Math.toIntExact(minLength.getValue()));
        settings.setMaxLength(Math.toIntExact(maxLength.getValue()));
        settings.setEnclosure(ButtonGroupHelper.getValue(enclosureGroup));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            minLength.validateValue();
            maxLength.validateValue();
            lengthRange.validate();

            if (!Dictionary.wordWithLengthInRangeExists(minLength.getValue().intValue(),
                                                        maxLength.getValue().intValue())) {
                throw new ValidationException("No words within that length range could be found.", minLength);
            }
        } catch (ValidationException e) {
            return new ValidationInfo(e.getMessage(), e.getComponent());
        }

        return null;
    }
}
