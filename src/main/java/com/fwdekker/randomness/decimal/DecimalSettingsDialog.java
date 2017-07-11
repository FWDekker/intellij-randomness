package com.fwdekker.randomness.decimal;

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
import javax.swing.SpinnerNumberModel;


/**
 * Dialog for settings of random decimal generation.
 */
final class DecimalSettingsDialog extends JDialog {
    private static final double SPINNER_STEP_SIZE = 0.1;

    private final DecimalSettings decimalSettings = DecimalSettings.getInstance();

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner minValue;
    private JSpinner maxValue;
    private JSpinner decimalCount;


    /**
     * Constructs a new {@code DecimalSettingsDialog}.
     */
    DecimalSettingsDialog() {
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

        minValue.setValue(decimalSettings.getMinValue());
        maxValue.setValue(decimalSettings.getMaxValue());
        decimalCount.setValue(decimalSettings.getDecimalCount());
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

        double newMinValue = (Double) minValue.getValue();
        double newMaxValue = (Double) maxValue.getValue();
        int newDecimalCount = (Integer) decimalCount.getValue();

        if (newMaxValue < newMinValue) {
            newMaxValue = newMinValue;
        }
        if (newDecimalCount < 0) {
            newDecimalCount = 0;
        }

        decimalSettings.setMinValue(newMinValue);
        decimalSettings.setMaxValue(newMaxValue);
        decimalSettings.setDecimalCount(newDecimalCount);
    }

    /**
     * Initialises custom UI components.
     * <p>
     * This method is called by the scene builder at the start of the constructor.
     */
    private void createUIComponents() {
        minValue = new JSpinner(
                new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SPINNER_STEP_SIZE));
        maxValue = new JSpinner(
                new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SPINNER_STEP_SIZE));
    }
}
