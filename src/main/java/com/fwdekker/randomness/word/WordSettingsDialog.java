package com.fwdekker.randomness.word;

import com.fwdekker.randomness.CapitalizationMode;
import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JEditableList;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Dialog for settings of random word generation.
 *
 * @see WordSettings
 * @see WordSettingsAction
 */
public final class WordSettingsDialog extends SettingsDialog<WordSettings> {
    private JPanel contentPane;
    private JSpinnerRange lengthRange;
    private JIntSpinner minLength;
    private JIntSpinner maxLength;
    private ButtonGroup capitalizationGroup;
    private ButtonGroup enclosureGroup;
    private JEditableList<Dictionary> dictionaries;
    private JButton dictionaryAddButton;
    private JButton dictionaryRemoveButton;


    /**
     * Constructs a new {@code WordSettingsDialog} that uses the singleton {@code WordSettings} instance.
     */
    /* default */ WordSettingsDialog() {
        this(WordSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code WordSettingsDialog} that uses the given {@code WordSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    /* default */ WordSettingsDialog(final @NotNull WordSettings settings) {
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
        lengthRange = new JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE);

        dictionaries = new JEditableList<>();
        dictionaries.getSelectionModel().addListSelectionListener(this::onDictionaryHighlightChange);

        dictionaryAddButton = new JButton();
        dictionaryAddButton.addActionListener(event -> addDictionary());
        dictionaryRemoveButton = new JButton();
        dictionaryRemoveButton.addActionListener(event -> removeDictionary());
    }


    @Override
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupKt.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupKt.setValue(capitalizationGroup, settings.getCapitalization());

        dictionaries.setEntries(WordSettingsDialogHelperKt.addSets(
            settings.getBundledDictionaries(), settings.getUserDictionaries()));
        dictionaries.setActiveEntries(WordSettingsDialogHelperKt.addSets(
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

        settings.setBundledDictionaries(filterIsInstance(dictionaries.getEntries(), BundledDictionary.class));
        settings.setActiveBundledDictionaries(filterIsInstance(dictionaries.getActiveEntries(), BundledDictionary.class));
        BundledDictionary.Companion.getCache().clear();

        settings.setUserDictionaries(filterIsInstance(dictionaries.getEntries(), UserDictionary.class));
        settings.setActiveUserDictionaries(filterIsInstance(dictionaries.getActiveEntries(), UserDictionary.class));
        UserDictionary.Companion.getCache().clear();
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        BundledDictionary.Companion.getCache().clear();
        UserDictionary.Companion.getCache().clear();

        if (dictionaries.getActiveEntries().isEmpty())
            return new ValidationInfo("Select at least one dictionary.", dictionaries);

        for (final Dictionary dictionary : dictionaries.getActiveEntries()) {
            try {
                dictionary.validate();
            } catch (final InvalidDictionaryException e) {
                return new ValidationInfo(
                    "Dictionary " + dictionary.toString() + " is invalid: " + e.getMessage(),
                    dictionaries
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
     */
    private void addDictionary() {
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
                        .show(RelativePoint.getSouthOf(dictionaryAddButton), Balloon.Position.below);
                    return;
                }
            } catch (final InvalidDictionaryException e) {
                JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(e.getMessage(), MessageType.ERROR, null)
                    .createBalloon()
                    .show(RelativePoint.getSouthOf(dictionaryAddButton), Balloon.Position.below);
                return;
            }

            dictionaries.addEntry(newDictionary);
        });
    }

    /**
     * Fires when the currently highlighted {@code Dictionary} should be removed the list.
     */
    private void removeDictionary() {
        final Dictionary highlightedDictionary = dictionaries.getHighlightedEntry();
        if (highlightedDictionary instanceof UserDictionary)
            dictionaries.removeEntry(highlightedDictionary);
    }

    /**
     * Fires when the user (un)highlights a dictionary.
     *
     * @param event the triggering event
     */
    private void onDictionaryHighlightChange(final ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            final Dictionary highlightedDictionary = dictionaries.getHighlightedEntry();
            final boolean enable = highlightedDictionary instanceof UserDictionary;
            dictionaryRemoveButton.setEnabled(enable);
        }
    }

    /**
     * Returns `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed.
     *
     * @return `null` if the selected word range overlaps with words in the chosen dictionaries, or a `ValidationInfo`
     * object explaining which input should be changed
     */
    private ValidationInfo validateWordRange() {
        final Set<String> words = WordSettingsDialogHelperKt.combineDictionaries(dictionaries.getActiveEntries());

        final int maxWordLength = WordSettingsDialogHelperKt.maxLength(words);
        if (minLength.getValue() > maxWordLength) {
            return new ValidationInfo("" +
                "Enter a value less than or equal to " + maxWordLength + ", " +
                "the length of the longest word in the selected dictionaries.",
                minLength
            );
        }

        final int minWordLength = WordSettingsDialogHelperKt.minLength(words);
        if (maxLength.getValue() < minWordLength) {
            return new ValidationInfo("" +
                "Enter a value greater than or equal to " + minWordLength + ", " +
                "the length of the shortest word in the selected dictionaries.",
                maxLength
            );
        }

        return null;
    }


    /**
     * Filters
     *
     * @param list  a list of {@code SUP} elements
     * @param cls   the class to filter by
     * @param <SUB> the subclass
     * @param <SUP> the super class
     * @return a list containing the values of {@code list} that are of class {@code cls}
     */
    private static <SUB extends SUP, SUP> Set<SUB> filterIsInstance(final List<SUP> list, final Class<SUB> cls) {
        return list.stream().filter(cls::isInstance).map(cls::cast).collect(Collectors.toSet());
    }
}
