package com.fwdekker.randomness.string;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;


/**
 * Dialog for settings of random number generation.
 */
final class StringSettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner minLength;
    private JSpinner maxLength;


    /**
     * Constructs a new {@code StringSettingsDialog}.
     */
    public StringSettingsDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // Call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // Call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        minLength.setValue(StringSettings.getMinLength());
        maxLength.setValue(StringSettings.getMaxLength());
    }


    /**
     * Commits settings and, if committing was successful, closes the dialog.
     */
    private void onOK() {
        try {
            commitSettings();
        } catch (final ParseException e) {
            e.printStackTrace();
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

        StringSettings.setMinLength((Integer) minLength.getValue());
        StringSettings.setMaxLength((Integer) maxLength.getValue());
    }
}
