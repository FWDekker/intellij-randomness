package com.fwdekker.randomness.word;

import com.fwdekker.randomness.CapitalizationMode;
import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsComponent;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JCheckBoxTable;
import com.fwdekker.randomness.ui.JDecoratedCheckBoxTablePanel;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.CommonActionsPanel;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Component for settings of random word generation.
 *
 * @see WordSettings
 * @see WordSettingsAction
 */
public final class WordSettingsComponent extends SettingsComponent<WordSettings> {
    private JPanel contentPane;
    private JSpinnerRange lengthRange;
    private JIntSpinner minLength;
    private JIntSpinner maxLength;
    private ButtonGroup capitalizationGroup;
    private ButtonGroup enclosureGroup;
    private JDecoratedCheckBoxTablePanel<Dictionary> dictionaryPanel;
    private JCheckBoxTable<Dictionary> dictionaryTable;


    /**
     * Constructs a new {@code WordSettingsComponent} that uses the singleton {@code WordSettings} instance.
     */
    /* default */ WordSettingsComponent() {
        this(WordSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code WordSettingsComponent} that uses the given {@code WordSettings} instance.
     *
     * @param settings the settings to manipulate with this component
     */
    /* default */ WordSettingsComponent(final @NotNull WordSettings settings) {
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

        dictionaryTable = new JCheckBoxTable<>(
            Arrays.asList(
                new JCheckBoxTable.Column("Type", false),
                new JCheckBoxTable.Column("Location", false)
            ),
            it -> "bundled".equals(it.get(0))
                ? BundledDictionary.Companion.getCache().get(it.get(1), true)
                : UserDictionary.Companion.getCache().get(it.get(1), true),
            it -> it instanceof BundledDictionary
                ? Arrays.asList("bundled", ((BundledDictionary) it).getFilename())
                : Arrays.asList("user", ((UserDictionary) it).getFilename()),
            UserDictionary.class::isInstance
        );
        dictionaryTable.setName("dictionaries");

        dictionaryPanel = new JDecoratedCheckBoxTablePanel<Dictionary>(
            dictionaryTable,
            this::addDictionaries, null, this::removeDictionaries,
            it -> true, it -> false, it -> it.stream().allMatch(UserDictionary.class::isInstance)
        );
    }


    @Override
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupKt.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupKt.setValue(capitalizationGroup, settings.getCapitalization());

        dictionaryTable.setEntries(WordSettingsComponentHelperKt.addSets(
            settings.getBundledDictionaries(), settings.getUserDictionaries()));
        dictionaryTable.setActiveEntries(WordSettingsComponentHelperKt.addSets(
            settings.getActiveBundledDictionaries(), settings.getActiveUserDictionaries()));
    }

    @Override
    public void saveSettings(final @NotNull WordSettings settings) {
        settings.setMinLength(minLength.getValue());
        settings.setMaxLength(maxLength.getValue());

        final String enclosure = ButtonGroupKt.getValue(enclosureGroup);
        settings.setEnclosure(enclosure == null ? WordSettings.DEFAULT_ENCLOSURE : enclosure);

        final String capitalization = ButtonGroupKt.getValue(capitalizationGroup);
        settings.setCapitalization(capitalization == null
            ? WordSettings.Companion.getDEFAULT_CAPITALIZATION()
            : CapitalizationMode.Companion.getMode(capitalization));

        settings.setBundledDictionaries(filterIsInstance(dictionaryTable.getEntries(), BundledDictionary.class));
        settings.setActiveBundledDictionaries(filterIsInstance(dictionaryTable.getActiveEntries(), BundledDictionary.class));
        BundledDictionary.Companion.getCache().clear();

        settings.setUserDictionaries(filterIsInstance(dictionaryTable.getEntries(), UserDictionary.class));
        settings.setActiveUserDictionaries(filterIsInstance(dictionaryTable.getActiveEntries(), UserDictionary.class));
        UserDictionary.Companion.getCache().clear();
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        BundledDictionary.Companion.getCache().clear();
        UserDictionary.Companion.getCache().clear();

        if (dictionaryTable.getActiveEntries().isEmpty())
            return new ValidationInfo("Select at least one dictionary.", dictionaryTable);

        for (final Dictionary dictionary : dictionaryTable.getActiveEntries()) {
            try {
                dictionary.validate();
            } catch (final InvalidDictionaryException e) {
                return new ValidationInfo(
                    "Dictionary " + dictionary.toString() + " is invalid: " + e.getMessage(),
                    dictionaryTable
                );
            }
        }

        return JavaHelperKt.firstNonNull(
            validateWordRange(),
            minLength.validateValue(),
            maxLength.validateValue(),
            lengthRange.validateValue()
        );
    }


    /**
     * Fires when a new {@code Dictionary} should be added to the list.
     *
     * @param dictionaries the symbol sets that are highlighted when the add button is pressed; ignored
     * @return {@link Unit}
     */
    private Unit addDictionaries(final List<? extends Dictionary> dictionaries) {
        FileChooser.chooseFiles(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"), null, null, files -> {
            if (files.isEmpty())
                return;

            final String canonicalPath = files.get(0).getCanonicalPath();
            if (canonicalPath == null)
                return;

            final UserDictionary newDictionary = UserDictionary.Companion.getCache().get(canonicalPath, false);
            try {
                if (newDictionary.getWords().isEmpty()) {
                    JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder("The dictionary file is empty.", MessageType.ERROR, null)
                        .createBalloon()
                        .show(
                            dictionaryPanel.getButton(CommonActionsPanel.Buttons.ADD).getPreferredPopupPoint(),
                            Balloon.Position.atRight
                        );
                    return;
                }
            } catch (final InvalidDictionaryException e) {
                JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(e.getMessage(), MessageType.ERROR, null)
                    .createBalloon()
                    .show(
                        dictionaryPanel.getButton(CommonActionsPanel.Buttons.ADD).getPreferredPopupPoint(),
                        Balloon.Position.atRight
                    );
                return;
            }

            dictionaryTable.addEntry(newDictionary);
        });

        return Unit.INSTANCE;
    }

    /**
     * Fires when the currently-highlighted {@code Dictionary} should be removed from the list.
     *
     * @param dictionaries the dictionaries to be removed; only {@code UserDictionary} instances are removed
     * @return {@link Unit}
     */
    private Unit removeDictionaries(final List<? extends Dictionary> dictionaries) {
        filterIsInstance(dictionaries, UserDictionary.class).forEach(dictionaryTable::removeEntry);
        return Unit.INSTANCE;
    }

    /**
     * Returns `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed.
     *
     * @return `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed
     */
    private ValidationInfo validateWordRange() {
        final Set<String> words = WordSettingsComponentHelperKt.combineDictionaries(dictionaryTable.getActiveEntries());

        final int maxWordLength = WordSettingsComponentHelperKt.maxLength(words);
        if (minLength.getValue() > maxWordLength) {
            return new ValidationInfo("" +
                "The longest word in the selected dictionaries is " + maxWordLength + " characters. " +
                "Set the minimum length to a value less than or equal to " + maxWordLength + ".",
                minLength
            );
        }

        final int minWordLength = WordSettingsComponentHelperKt.minLength(words);
        if (maxLength.getValue() < minWordLength) {
            return new ValidationInfo("" +
                "The shortest word in the selected dictionaries is " + minWordLength + " characters. " +
                "Set the maximum length to a value less than or equal to " + minWordLength + ".",
                maxLength
            );
        }

        return null;
    }


    /**
     * Filters instances of {@code SUP} to only return instances of {@code SUB}.
     *
     * @param list  a list of {@code SUP} elements
     * @param cls   the class to filter by
     * @param <SUB> the subclass
     * @param <SUP> the super class
     * @return a list containing the values of {@code list} that are of class {@code cls}
     */
    private static <SUB, SUP> Set<SUB> filterIsInstance(final List<SUP> list, final Class<SUB> cls) {
        return list.stream().filter(cls::isInstance).map(cls::cast).collect(Collectors.toSet());
    }
}
