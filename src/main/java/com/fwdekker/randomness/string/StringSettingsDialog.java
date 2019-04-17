package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.fwdekker.randomness.CapitalizationMode;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import java.util.HashSet;
import java.util.Optional;


/**
 * Dialog for settings of random string generation.
 */
public final class StringSettingsDialog extends SettingsDialog<StringSettings> {
    private JPanel contentPane;
    private JSpinnerRange lengthRange;
    private JLongSpinner minLength;
    private JLongSpinner maxLength;
    private ButtonGroup enclosureGroup;
    private ButtonGroup capitalizationGroup;
    private JList<Alphabet> alphabetList;


    /**
     * Constructs a new {@code StringSettingsDialog} that uses the singleton {@code StringSettings} instance.
     */
    /* default */ StringSettingsDialog() {
        this(StringSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code StringSettingsDialog} that uses the given {@code StringSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    /* default */ StringSettingsDialog(final @NotNull StringSettings settings) {
        super(settings);

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
    private void createUIComponents() {
        minLength = new JLongSpinner(1, 1, Integer.MAX_VALUE);
        maxLength = new JLongSpinner(1, 1, Integer.MAX_VALUE);
        lengthRange = new JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE);

        alphabetList = new JList<>(Alphabet.values());
        alphabetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        alphabetList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    }


    @Override
    public void loadSettings(final @NotNull StringSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.INSTANCE.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupHelper.INSTANCE.setValue(capitalizationGroup, settings.getCapitalization());

        for (int i = 0; i < Alphabet.values().length; i++) {
            if (settings.getAlphabets().contains(Alphabet.values()[i])) {
                alphabetList.addSelectionInterval(i, i);
            }
        }
    }

    @Override
    public void saveSettings(final @NotNull StringSettings settings) {
        settings.setMinLength(Math.toIntExact(minLength.getValue()));
        settings.setMaxLength(Math.toIntExact(maxLength.getValue()));
        settings.setEnclosure(ButtonGroupHelper.INSTANCE.getValue(enclosureGroup));
        settings.setCapitalization(CapitalizationMode.Companion
            .getMode(ButtonGroupHelper.INSTANCE.getValue(capitalizationGroup)));
        settings.setAlphabets(new HashSet<>(alphabetList.getSelectedValuesList()));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        return Optional.ofNullable(minLength.validateValue())
            .orElse(Optional.ofNullable(maxLength.validateValue())
                .orElse(lengthRange.validateValue()));
    }
}
