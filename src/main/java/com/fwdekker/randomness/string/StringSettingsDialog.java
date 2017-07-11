package com.fwdekker.randomness.string;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;


/**
 * Dialog for settings of random integer generation.
 */
final class StringSettingsDialog extends JDialog {
    private final StringSettings stringSettings = StringSettings.getInstance();

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner minLength;
    private JSpinner maxLength;
    private ButtonGroup enclosureGroup;
    private JRadioButton enclosureNoneButton;
    private JRadioButton enclosureSingleButton;
    private JRadioButton enclosureDoubleButton;
    private JRadioButton enclosureBacktickButton;


    /**
     * Constructs a new {@code StringSettingsDialog}.
     */
    StringSettingsDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());

        // Call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // Call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Load settings
        minLength.setValue(stringSettings.getMinLength());
        maxLength.setValue(stringSettings.getMaxLength());
        setSelectedEnclosure(stringSettings.getEnclosure());
    }


    /**
     * Commits settings and, if committing was successful, closes the dialog.
     */
    private void onOK() {
        try {
            commitSettings();
        } catch (final ParseException e) {
            return;
        }

        dispose();
    }

    /**
     * Closes the dialog without committing settings.
     */
    private void onCancel() {
        dispose();
    }

    /**
     * Commits the values entered by the user to the model.
     *
     * @throws ParseException if the values entered by the user could not be parsed
     */
    private void commitSettings() throws ParseException {
        minLength.commitEdit();
        maxLength.commitEdit();

        int newMinLength = (Integer) minLength.getValue();
        int newMaxLength = (Integer) maxLength.getValue();
        if (newMaxLength < newMinLength) {
            newMaxLength = newMinLength;
        }

        stringSettings.setMinLength(newMinLength);
        stringSettings.setMaxLength(newMaxLength);
        stringSettings.setEnclosure(getSelectedEnclosure());
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
