package com.fwdekker.randomness.string;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;


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
    private JCheckBox quotationMarksCheckbox;


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
        quotationMarksCheckbox.setSelected(stringSettings.isQuotationMarksEnabled());
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
        stringSettings.setQuotationMarksEnabled(quotationMarksCheckbox.isSelected());
    }
}
