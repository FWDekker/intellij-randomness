package com.fwdekker.randomness.integer;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;


/**
 * Dialog for settings of random integer generation.
 */
final class IntegerSettingsDialog extends JDialog {
    private final IntegerSettings integerSettings = IntegerSettings.getInstance();

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner minValue;
    private JSpinner maxValue;


    /**
     * Constructs a new {@code IntegerSettingsDialog}.
     */
    IntegerSettingsDialog() {
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

        minValue.setValue(integerSettings.getMinValue());
        maxValue.setValue(integerSettings.getMaxValue());
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
        minValue.commitEdit();
        maxValue.commitEdit();

        final int newMinValue = (Integer) minValue.getValue();
        final int newMaxValue = (Integer) maxValue.getValue();

        integerSettings.setMinValue(newMinValue);
        integerSettings.setMaxValue(newMaxValue < newMinValue ? newMinValue : newMaxValue);
    }
}
