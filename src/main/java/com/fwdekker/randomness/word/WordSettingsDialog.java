package com.fwdekker.randomness.word;

import com.fwdekker.randomness.CapitalizationMode;
import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JEditableList;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Dialog for settings of random word generation.
 */
public final class WordSettingsDialog extends SettingsDialog<WordSettings> {
    private JPanel contentPane;
    private JSpinnerRange lengthRange;
    private JLongSpinner minLength;
    private JLongSpinner maxLength;
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

        dictionaries = new JEditableList<>();
        dictionaries.getSelectionModel().addListSelectionListener(this::onDictionaryHighlightChange);
        dictionaries.addEntryActivityChangeListener(event -> onDictionaryActivityChange());
        onDictionaryActivityChange();

        dictionaryAddButton = new JButton();
        dictionaryAddButton.addActionListener(event -> addDictionary());
        dictionaryRemoveButton = new JButton();
        dictionaryRemoveButton.addActionListener(event -> removeDictionary());
    }


    @Override
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.INSTANCE.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupHelper.INSTANCE.setValue(capitalizationGroup, settings.getCapitalization());

        dictionaries.setEntries(WordSettingsDialogKt.addSets(
            settings.getBundledDictionaries(), settings.getUserDictionaries()));
        dictionaries.setActiveEntries(WordSettingsDialogKt.addSets(
            settings.getActiveBundledDictionaries(), settings.getActiveUserDictionaries()));
    }

    @Override
    public void saveSettings(final @NotNull WordSettings settings) {
        settings.setMinLength(Math.toIntExact(minLength.getValue()));
        settings.setMaxLength(Math.toIntExact(maxLength.getValue()));
        settings.setEnclosure(ButtonGroupHelper.INSTANCE.getValue(enclosureGroup));
        settings.setCapitalization(CapitalizationMode.Companion.getMode(
            ButtonGroupHelper.INSTANCE.getValue(capitalizationGroup)));

        settings.setBundledDictionaries(dictionaries.getEntries().stream()
            .filter(BundledDictionary.class::isInstance)
            .map(it -> (BundledDictionary) it)
            .collect(Collectors.toSet()));
        settings.setActiveBundledDictionaries(dictionaries.getActiveEntries().stream()
            .filter(BundledDictionary.class::isInstance)
            .map(it -> (BundledDictionary) it)
            .collect(Collectors.toSet()));
        BundledDictionary.Companion.getCache().clear();

        settings.setUserDictionaries(dictionaries.getEntries().stream()
            .filter(UserDictionary.class::isInstance)
            .map(it -> (UserDictionary) it)
            .collect(Collectors.toSet()));
        settings.setActiveUserDictionaries(dictionaries.getActiveEntries().stream()
            .filter(UserDictionary.class::isInstance)
            .map(it -> (UserDictionary) it)
            .collect(Collectors.toSet()));
        UserDictionary.Companion.getCache().clear();
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        if (dictionaries.getActiveEntries().isEmpty())
            return new ValidationInfo("Select at least one dictionary.", dictionaries);

        if (dictionaries.getActiveEntries().stream()
            .map(Dictionary::isValid)
            .anyMatch(it -> !it)) {
            return new ValidationInfo("One of these dictionaries is not valid.", dictionaries);
        }

        return Stream
            .of(
                minLength.validateValue(),
                maxLength.validateValue(),
                lengthRange.validateValue()
            )
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
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
        if (highlightedDictionary instanceof UserDictionary) {
            dictionaries.removeEntry(highlightedDictionary);
        }
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
     * Fires when the user (de)activates a dictionary.
     *
     * @return {@code Unit.INSTANCE}
     */
    private Unit onDictionaryActivityChange() {
        final Set<String> words = WordSettingsDialogKt.combineDictionaries(dictionaries.getActiveEntries());

        if (words.isEmpty()) {
            minLength.setMaxValue(1);
            maxLength.setMinValue(Integer.MAX_VALUE);
        } else {
            minLength.setMaxValue(words.stream().map(String::length).max(Comparator.comparing(Integer::valueOf)).get());
            maxLength.setMinValue(words.stream().map(String::length).min(Comparator.comparing(Integer::valueOf)).get());
        }

        return Unit.INSTANCE;
    }
}
