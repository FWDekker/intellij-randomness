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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random integer generation.
 */
@SuppressFBWarnings(
        value = {"UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD"},
        justification = "Initialized by UI framework"
)
final class StringSettingsDialog extends SettingsDialog {
    private final StringSettings stringSettings;

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
     * @param stringSettings the settings to manipulate with this dialog
     */
    StringSettingsDialog(final StringSettings stringSettings) {
        super();

        init();

        this.stringSettings = stringSettings;
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
        alphabetList = new JList<>(Alphabet.values());
        alphabetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        alphabetList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    }


    @Override
    protected void loadSettings() {
        minLength.setValue(stringSettings.getMinLength());
        maxLength.setValue(stringSettings.getMaxLength());
        setSelectedEnclosure(stringSettings.getEnclosure());

        for (int i = 0; i < Alphabet.values().length; i++) {
            if (stringSettings.getAlphabets().contains(Alphabet.values()[i])) {
                alphabetList.addSelectionInterval(i, i);
            }
        }
    }

    @Override
    protected void saveSettings() {
        try {
            minLength.commitEdit();
            maxLength.commitEdit();
        } catch (final ParseException e) {
            throw new IllegalStateException("Settings were committed, but input could not be parsed.", e);
        }

        stringSettings.setMinLength((Integer) minLength.getValue());
        stringSettings.setMaxLength((Integer) maxLength.getValue());
        stringSettings.setEnclosure(getSelectedEnclosure());
        stringSettings.setAlphabets(new HashSet<>(alphabetList.getSelectedValuesList()));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        if (!(minLength.getValue() instanceof Integer)) {
            return new ValidationInfo("Minimum length must be an integer.", minLength);
        }
        if (!(maxLength.getValue() instanceof Integer)) {
            return new ValidationInfo("Maximum length must be an integer.", maxLength);
        }

        final double newMinLength = (Integer) minLength.getValue();
        final double newMaxLength = (Integer) maxLength.getValue();
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
