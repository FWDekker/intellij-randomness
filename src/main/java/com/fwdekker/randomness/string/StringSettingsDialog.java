package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsDialog;
import com.intellij.openapi.ui.ValidationInfo;
import java.text.ParseException;
import java.util.HashSet;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;


/**
 * Dialog for settings of random integer generation.
 */
final class StringSettingsDialog extends SettingsDialog {
    private final StringSettings stringSettings = StringSettings.getInstance();

    private JPanel contentPane;
    private JSpinner minLength;
    private JSpinner maxLength;
    private ButtonGroup enclosureGroup;
    private JRadioButton enclosureNoneButton;
    private JRadioButton enclosureSingleButton;
    private JRadioButton enclosureDoubleButton;
    private JRadioButton enclosureBacktickButton;
    private JList<Alphabet> alphabetList;


    /**
     * Constructs a new {@code StringSettingsDialog}.
     */
    StringSettingsDialog() {
        super();

        init();
        loadSettings();
    }


    @Override
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
    protected ValidationInfo doValidate() {
        if (!(minLength.getValue() instanceof Integer)) {
            return new ValidationInfo("Minimum value must be an integer.", minLength);
        }
        if (!(maxLength.getValue() instanceof Integer)) {
            return new ValidationInfo("Maximum value must be an integer.", maxLength);
        }

        final double newMinLength = (Integer) minLength.getValue();
        final double newMaxLength = (Integer) maxLength.getValue();
        if (newMaxLength < newMinLength) {
            return new ValidationInfo("Maximum value cannot be smaller than minimum value.", maxLength);
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
        if (enclosureNoneButton.isSelected()) {
            return "";
        } else if (enclosureSingleButton.isSelected()) {
            return "'";
        } else if (enclosureDoubleButton.isSelected()) {
            return "\"";
        } else if (enclosureBacktickButton.isSelected()) {
            return "`";
        } else {
            return "";
        }
    }

    /**
     * Selects the {@code JRadioButton} in the {@code enclosureGroup} group with the given text, and deselects all
     * other {@code JRadioButton}s in that group.
     *
     * @param enclosure the text of the {@code JRadioButton} to select
     */
    private void setSelectedEnclosure(final String enclosure) {
        switch (enclosure) {
            case "":
                enclosureGroup.setSelected(enclosureNoneButton.getModel(), true);
                break;
            case "'":
                enclosureGroup.setSelected(enclosureSingleButton.getModel(), true);
                break;
            case "\"":
                enclosureGroup.setSelected(enclosureDoubleButton.getModel(), true);
                break;
            case "`":
                enclosureGroup.setSelected(enclosureBacktickButton.getModel(), true);
                break;
            default:
                enclosureGroup.setSelected(enclosureNoneButton.getModel(), true);
                break;
        }
    }
}
