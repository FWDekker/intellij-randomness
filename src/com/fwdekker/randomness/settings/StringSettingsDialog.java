package com.fwdekker.randomness.settings;

import com.fwdekker.randomness.insertion.InsertRandomString;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Dialog for settings of random number generation.
 */
public class StringSettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField minLength;
    private JTextField maxLength;


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

        minLength.setText(Integer.toString(InsertRandomString.getMinLength()));
        maxLength.setText(Integer.toString(InsertRandomString.getMaxLength()));
    }


    private void onOK() {
        final int newMinLength = Integer.parseInt(minLength.getText());
        final int newMaxLength = Integer.parseInt(maxLength.getText());
        InsertRandomString.setMinLength(newMinLength);
        InsertRandomString.setMaxLength(newMaxLength);
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
