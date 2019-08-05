package com.fwdekker.randomness.word;

import com.fwdekker.randomness.CapitalizationMode;
import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsComponent;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import java.util.Collection;
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
    private JPanel dictionaryPanel;
    private DictionaryTable dictionaryTable;


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
        dictionaryTable = new DictionaryTable();
        dictionaryPanel = dictionaryTable.createComponent();
    }


    @Override
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupKt.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupKt.setValue(capitalizationGroup, settings.getCapitalization());

        dictionaryTable.setDictionaries(WordSettingsComponentHelperKt.addSets(
            settings.getBundledDictionaries(), settings.getUserDictionaries()));
        dictionaryTable.setActiveDictionaries(WordSettingsComponentHelperKt.addSets(
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

        settings.setBundledDictionaries(filterIsInstance(dictionaryTable.getDictionaries(), BundledDictionary.class));
        settings.setActiveBundledDictionaries(filterIsInstance(dictionaryTable.getActiveDictionaries(), BundledDictionary.class));
        BundledDictionary.Companion.getCache().clear();

        settings.setUserDictionaries(filterIsInstance(dictionaryTable.getDictionaries(), UserDictionary.class));
        settings.setActiveUserDictionaries(filterIsInstance(dictionaryTable.getActiveDictionaries(), UserDictionary.class));
        UserDictionary.Companion.getCache().clear();
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        BundledDictionary.Companion.getCache().clear();
        UserDictionary.Companion.getCache().clear();

        if (dictionaryTable.getDictionaries().stream().distinct().count() != dictionaryTable.getDictionaries().size())
            return new ValidationInfo("Dictionaries must be unique.", dictionaryPanel);

        for (final Dictionary dictionary : dictionaryTable.getDictionaries()) {
            try {
                dictionary.validate();

                if (dictionary.getWords().isEmpty()) {
                    return new ValidationInfo("Dictionary `" + dictionary.toString() + "` is empty.", dictionaryPanel);
                }
            } catch (final InvalidDictionaryException e) {
                return new ValidationInfo(
                    "Dictionary `" + dictionary.toString() + "` is invalid: " + e.getMessage(),
                    dictionaryPanel
                );
            }
        }

        if (dictionaryTable.getActiveDictionaries().isEmpty())
            return new ValidationInfo("Select at least one dictionary.", dictionaryPanel);

        return JavaHelperKt.firstNonNull(
            validateWordRange(),
            minLength.validateValue(),
            maxLength.validateValue(),
            lengthRange.validateValue()
        );
    }


    /**
     * Returns `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed.
     *
     * @return `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed
     */
    private ValidationInfo validateWordRange() {
        final Set<String> words = WordSettingsComponentHelperKt.combineDictionaries(dictionaryTable.getActiveDictionaries());

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
     * @param list  a collection of {@code SUP} elements
     * @param cls   the class to filter by
     * @param <SUB> the subclass
     * @param <SUP> the super class
     * @return a list containing the values of {@code list} that are of class {@code cls}
     */
    private static <SUB, SUP> Set<SUB> filterIsInstance(final Collection<SUP> list, final Class<SUB> cls) {
        return list.stream().filter(cls::isInstance).map(cls::cast).collect(Collectors.toSet());
    }
}
