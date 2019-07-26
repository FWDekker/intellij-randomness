package com.fwdekker.randomness.string;

import com.fwdekker.randomness.CapitalizationMode;
import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsComponent;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JCheckBoxList;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Component for settings of random string generation.
 *
 * @see StringSettings
 * @see StringSettingsAction
 */
public final class StringSettingsComponent extends SettingsComponent<StringSettings> {
    private JPanel contentPane;
    private JSpinnerRange lengthRange;
    private JIntSpinner minLength;
    private JIntSpinner maxLength;
    private ButtonGroup enclosureGroup;
    private ButtonGroup capitalizationGroup;
    private JCheckBoxList<SymbolSet> symbolSets;
    private JButton symbolSetAddButton;
    private JButton symbolSetRemoveButton;
    private JButton symbolSetEditButton;


    /**
     * Constructs a new {@code StringSettingsComponent} that uses the singleton {@code StringSettings} instance.
     */
    /* default */ StringSettingsComponent() {
        this(StringSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code StringSettingsComponent} that uses the given {@code StringSettings} instance.
     *
     * @param settings the settings to manipulate with this component
     */
    /* default */ StringSettingsComponent(final @NotNull StringSettings settings) {
        super(settings);

        loadSettings();
    }


    @Override
    public JPanel getRootPane() {
        return contentPane;
    }

    /**
     * Initialises custom UI components.
     * <p>
     * This method is called by the scene builder at the start of the constructor.
     */
    private void createUIComponents() {
        minLength = new JIntSpinner(1, 1);
        maxLength = new JIntSpinner(1, 1);
        lengthRange = new JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE, "length");

        symbolSets = new JCheckBoxList<>("symbolSets");
        symbolSets.getSelectionModel().addListSelectionListener(this::onSymbolSetHighlightChange);

        symbolSetAddButton = new JButton();
        symbolSetAddButton.addActionListener(event -> addSymbolSet());
        symbolSetEditButton = new JButton();
        symbolSetEditButton.addActionListener(event -> editSymbolSet());
        symbolSetRemoveButton = new JButton();
        symbolSetRemoveButton.addActionListener(event -> removeSymbolSet());
    }


    @Override
    public void loadSettings(final @NotNull StringSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupKt.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupKt.setValue(capitalizationGroup, settings.getCapitalization());

        symbolSets.setEntries(settings.getSymbolSetList());
        symbolSets.setActiveEntries(settings.getActiveSymbolSetList());
        onSymbolSetHighlightChange(null);
    }

    @Override
    public void saveSettings(final @NotNull StringSettings settings) {
        settings.setMinLength(minLength.getValue());
        settings.setMaxLength(maxLength.getValue());

        final String enclosure = ButtonGroupKt.getValue(enclosureGroup);
        settings.setEnclosure(enclosure == null ? StringSettings.DEFAULT_ENCLOSURE : enclosure);

        final String capitalizationMode = ButtonGroupKt.getValue(capitalizationGroup);
        settings.setCapitalization(capitalizationMode == null
            ? StringSettings.Companion.getDEFAULT_CAPITALIZATION()
            : CapitalizationMode.Companion.getMode(capitalizationMode));

        settings.setSymbolSetList(symbolSets.getEntries());
        settings.setActiveSymbolSetList(symbolSets.getActiveEntries());
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        if (symbolSets.getActiveEntries().isEmpty())
            return new ValidationInfo("Select at least one symbol set.", symbolSets);

        return JavaHelperKt.firstNonNull(
            minLength.validateValue(),
            maxLength.validateValue(),
            lengthRange.validateValue()
        );
    }


    /**
     * Fires when a new {@code Dictionary} should be added to the list.
     */
    private void addSymbolSet() {
        final List<String> reservedNames = symbolSets.getEntries().stream()
            .map(SymbolSet::getName)
            .collect(Collectors.toList());

        final SymbolSetDialog dialog = new SymbolSetDialog(reservedNames);
        if (dialog.showAndGet()) {
            symbolSets.addEntry(new SymbolSet(dialog.getName(), dialog.getSymbols()));
        }
    }

    /**
     * Fires when the currently-highlighted {@code Dictionary} should be edited.
     */
    private void editSymbolSet() {
        final SymbolSet highlightedSymbolSet = symbolSets.getHighlightedEntry();
        if (highlightedSymbolSet == null) {
            return;
        }

        final List<String> reservedNames = symbolSets.getEntries().stream()
            .map(SymbolSet::getName)
            .filter(it -> !it.equals(highlightedSymbolSet.getName()))
            .collect(Collectors.toList());

        final SymbolSetDialog dialog = new SymbolSetDialog(reservedNames, highlightedSymbolSet);
        if (dialog.showAndGet()) {
            highlightedSymbolSet.setName(dialog.getName());
            highlightedSymbolSet.setSymbols(dialog.getSymbols());

            symbolSets.revalidate();
            symbolSets.repaint();
        }
    }

    /**
     * Fires when the currently-highlighted {@code SymbolSet} should be removed the list.
     */
    private void removeSymbolSet() {
        final SymbolSet highlightedSymbolSet = symbolSets.getHighlightedEntry();
        if (highlightedSymbolSet == null) {
            return;
        }

        symbolSets.removeEntry(highlightedSymbolSet);
    }

    /**
     * Fires when the user (un)highlights a symbol set.
     *
     * @param event the triggering event
     */
    private void onSymbolSetHighlightChange(final ListSelectionEvent event) {
        if (event == null || !event.getValueIsAdjusting()) {
            final boolean enabled = symbolSets.getHighlightedEntry() != null;
            symbolSetEditButton.setEnabled(enabled);
            symbolSetRemoveButton.setEnabled(enabled);
        }
    }
}
