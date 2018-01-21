package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ValidationException;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import java.util.HashSet;


/**
 * Dialog for settings of random string generation.
 */
@SuppressFBWarnings(
        value = {"UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD"},
        justification = "Initialized by UI framework"
)
final class StringSettingsDialog extends SettingsDialog<StringSettings> {
    private JPanel contentPane;
    private JSpinnerRange lengthRange;
    private JLongSpinner minLength;
    private JLongSpinner maxLength;
    private ButtonGroup enclosureGroup;
    private JList<Alphabet> alphabetList;


    /**
     * Constructs a new {@code StringSettingsDialog} that uses the singleton {@code StringSettings} instance.
     */
    StringSettingsDialog() {
        this(StringSettings.getInstance());
    }

    /**
     * Constructs a new {@code StringSettingsDialog} that uses the given {@code StringSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    StringSettingsDialog(final @NotNull StringSettings settings) {
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

        alphabetList = new JList<>(Alphabet.values());
        alphabetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        alphabetList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    }


    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void loadSettings(final @NotNull StringSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.setValue(enclosureGroup, settings.getEnclosure());

        for (int i = 0; i < Alphabet.values().length; i++) {
            if (settings.getAlphabets().contains(Alphabet.values()[i])) {
                alphabetList.addSelectionInterval(i, i);
            }
        }
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void saveSettings(final @NotNull StringSettings settings) {
        settings.setMinLength(Math.toIntExact(minLength.getValue()));
        settings.setMaxLength(Math.toIntExact(maxLength.getValue()));
        settings.setEnclosure(ButtonGroupHelper.getValue(enclosureGroup));
        settings.setAlphabets(new HashSet<>(alphabetList.getSelectedValuesList()));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            minLength.validateValue();
            maxLength.validateValue();
            lengthRange.validate();
        } catch (final ValidationException e) {
            return new ValidationInfo(e.getMessage(), e.getComponent());
        }

        if (alphabetList.getSelectedValuesList().isEmpty()) {
            return new ValidationInfo("Please select at least one option.", alphabetList);
        }

        return null;
    }
}
