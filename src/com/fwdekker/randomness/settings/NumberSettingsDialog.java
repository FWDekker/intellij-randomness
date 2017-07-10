package com.fwdekker.randomness.settings;

import com.fwdekker.randomness.insertion.InsertRandomNumber;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Dialog for settings of random number generation.
 */
public class NumberSettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField minValue;
    private JTextField maxValue;


    /**
     * Constructs a new {@code NumberSettingsDialog}.
     */
    public NumberSettingsDialog() {
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

        minValue.setText(Integer.toString(InsertRandomNumber.getMinValue()));
        maxValue.setText(Integer.toString(InsertRandomNumber.getMaxValue()));
    }


    private void onOK() {
        final int newMinValue = Integer.parseInt(minValue.getText());
        final int newMaxValue = Integer.parseInt(maxValue.getText());
        InsertRandomNumber.setMinValue(newMinValue);
        InsertRandomNumber.setMaxValue(newMaxValue);

        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
