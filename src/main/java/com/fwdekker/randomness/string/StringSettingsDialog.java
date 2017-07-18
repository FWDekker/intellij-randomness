package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsDialog;
import com.intellij.openapi.ui.ValidationInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random integer generation.
 */
@SuppressFBWarnings(
        value = {"UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD"},
        justification = "Initialized by UI framework"
)
final class StringSettingsDialog extends SettingsDialog<StringSettings> {
    private JPanel contentPane;
    private JSpinner minLength;
    private JSpinner maxLength;
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
    StringSettingsDialog(@NotNull final StringSettings settings) {
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
        minLength = new JSpinner(new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L));
        maxLength = new JSpinner(new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L));

        alphabetList = new JList<>(Alphabet.values());
        alphabetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        alphabetList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    }


    @Override
    public void loadSettings(@NotNull final StringSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        setSelectedEnclosure(settings.getEnclosure());

        for (int i = 0; i < Alphabet.values().length; i++) {
            if (settings.getAlphabets().contains(Alphabet.values()[i])) {
                alphabetList.addSelectionInterval(i, i);
            }
        }
    }

    @Override
    public void saveSettings(@NotNull final StringSettings settings) {
        final int newMinLength = ((Number) minLength.getValue()).intValue();
        final int newMaxLength = ((Number) maxLength.getValue()).intValue();

        settings.setMinLength(newMinLength);
        settings.setMaxLength(newMaxLength);
        settings.setEnclosure(getSelectedEnclosure());
        settings.setAlphabets(new HashSet<>(alphabetList.getSelectedValuesList()));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            minLength.commitEdit();
        } catch (final ParseException e) {
            return new ValidationInfo("Minimum length must be a number.", minLength);
        }
        try {
            maxLength.commitEdit();
        } catch (final ParseException e) {
            return new ValidationInfo("Maximum length must be a number.", maxLength);
        }

        final double newMinLength = ((Number) minLength.getValue()).doubleValue();
        if (newMinLength % 1 != 0) {
            return new ValidationInfo("Minimum length must be a whole number.", minLength);
        }
        if (newMinLength <= 0) {
            return new ValidationInfo("Minimum length must be a positive number.", minLength);
        }

        final double newMaxLength = ((Number) maxLength.getValue()).doubleValue();
        if (newMaxLength % 1 != 0) {
            return new ValidationInfo("Maximum length must be a whole number.", maxLength);
        }
        if (newMaxLength > Integer.MAX_VALUE) {
            return new ValidationInfo("Maximum length must not be greater than 2^31-1.", maxLength);
        }

        if (newMaxLength < newMinLength) {
            return new ValidationInfo("Maximum length cannot be smaller than minimum length.", maxLength);
        }

        if (alphabetList.getSelectedValuesList().isEmpty()) {
            return new ValidationInfo("Select at least one set of symbols.", alphabetList);
        }

        return null;
    }


    /**
     * Returns the text of the currently selected {@code JRadioButton} in the {@code enclosureGroup} group.
     *
     * @return the text of the currently selected {@code JRadioButton} in the {@code enclosureGroup} group
     */
    private String getSelectedEnclosure() {
        return Collections.list(enclosureGroup.getElements()).stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getActionCommand)
                .findFirst()
                .orElse("");
    }

    /**
     * Selects the {@code JRadioButton} in the {@code enclosureGroup} group with the given text, and deselects all
     * other {@code JRadioButton}s in that group.
     *
     * @param enclosure the text of the {@code JRadioButton} to select
     */
    private void setSelectedEnclosure(final String enclosure) {
        Collections.list(enclosureGroup.getElements()).stream()
                .filter(button -> button.getActionCommand().equals(enclosure))
                .findFirst()
                .ifPresent(button -> button.setSelected(true));
    }
}
