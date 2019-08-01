package com.fwdekker.randomness.string;

import com.fwdekker.randomness.CapitalizationMode;
import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsComponent;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JCheckBoxTable;
import com.fwdekker.randomness.ui.JDecoratedCheckBoxTablePanel;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import java.util.Arrays;
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
    private JDecoratedCheckBoxTablePanel<SymbolSet> symbolSetPanel;
    private JCheckBoxTable<SymbolSet> symbolSetTable;


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

        symbolSetPanel =
            new JDecoratedCheckBoxTablePanel<SymbolSet>(
                new JCheckBoxTable<>(
                    2,
                    it -> new SymbolSet(it.get(0), it.get(1)),
                    it -> Arrays.asList(it.getName(), it.getSymbols()),
                    "symbolSets"
                ),
                this::addSymbolSets, this::editSymbolSets, this::removeSymbolSets,
                it -> true, it -> it.size() == 1, it -> !it.isEmpty()
            );
        symbolSetTable = symbolSetPanel.getTable();
    }


    @Override
    public void loadSettings(final @NotNull StringSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupKt.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupKt.setValue(capitalizationGroup, settings.getCapitalization());

        symbolSetTable.setEntries(settings.getSymbolSetList());
        symbolSetTable.setActiveEntries(settings.getActiveSymbolSetList());
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

        settings.setSymbolSetList(symbolSetTable.getEntries());
        settings.setActiveSymbolSetList(symbolSetTable.getActiveEntries());
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        if (symbolSetTable.getActiveEntries().isEmpty())
            return new ValidationInfo("Select at least one symbol set.", symbolSetTable);

        return JavaHelperKt.firstNonNull(
            minLength.validateValue(),
            maxLength.validateValue(),
            lengthRange.validateValue()
        );
    }


    /**
     * Fires when a new {@code Dictionary} should be added to the list.
     *
     * @param symbolSets the symbol sets that are highlighted when the add button is pressed; ignored
     * @return {@link Unit}
     */
    private Unit addSymbolSets(final List<? extends SymbolSet> symbolSets) {
        final List<String> reservedNames = symbolSetTable.getEntries().stream()
            .map(SymbolSet::getName)
            .collect(Collectors.toList());

        final SymbolSetDialog dialog = new SymbolSetDialog(reservedNames);
        if (dialog.showAndGet()) {
            symbolSetTable.addEntry(new SymbolSet(dialog.getName(), dialog.getSymbols()));
        }

        return Unit.INSTANCE;
    }

    /**
     * Fires when the currently-highlighted {@code Dictionary} should be edited.
     *
     * @param symbolSets the symbol sets to be edited; only the first element is used; an exception is thrown if this
     *                   list is empty
     * @return {@link Unit}
     */
    private Unit editSymbolSets(final List<? extends SymbolSet> symbolSets) {
        final SymbolSet symbolSet = symbolSets.get(0);

        final List<String> reservedNames = symbolSetTable.getEntries().stream()
            .map(SymbolSet::getName)
            .filter(it -> !it.equals(symbolSet.getName()))
            .collect(Collectors.toList());

        final SymbolSetDialog dialog = new SymbolSetDialog(reservedNames, symbolSet);
        if (dialog.showAndGet()) {
            symbolSet.setName(dialog.getName());
            symbolSet.setSymbols(dialog.getSymbols());

            symbolSetTable.revalidate();
            symbolSetTable.repaint();
        }

        return Unit.INSTANCE;
    }

    /**
     * Fires when the currently-highlighted {@code SymbolSet}s should be removed from the list.
     *
     * @param symbolSets the symbol sets to be removed
     * @return {@link Unit}
     */
    private Unit removeSymbolSets(final List<? extends SymbolSet> symbolSets) {
        symbolSets.forEach(symbolSetTable::removeEntry);
        return Unit.INSTANCE;
    }
}
