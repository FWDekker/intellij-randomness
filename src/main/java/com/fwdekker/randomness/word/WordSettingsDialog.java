package com.fwdekker.randomness.word;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ValidationException;
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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Dialog for settings of random word generation.
 */
@SuppressWarnings("PMD.SingularField") // Required by UI Framework
@SuppressFBWarnings(
        value = {"UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD"},
        justification = "Initialized by UI framework"
)
final class WordSettingsDialog extends SettingsDialog<WordSettings> {
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
    WordSettingsDialog() {
        this(WordSettings.getInstance());
    }

    /**
     * Constructs a new {@code WordSettingsDialog} that uses the given {@code WordSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    WordSettingsDialog(final @NotNull WordSettings settings) {
        super(settings);

        init();
        loadSettings();
    }


    @Override
    @NotNull
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    /**
     * Initialises custom UI components.
     * <p>
     * This method is called by the scene builder at the start of the constructor.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Method used by scene builder
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
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupHelper.setValue(capitalizationGroup, settings.getCapitalization());

        dictionaries.setEntries(settings.getValidAllDictionaries());
        dictionaries.setActiveEntries(settings.getValidActiveDictionaries());
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void saveSettings(final @NotNull WordSettings settings) {
        settings.setMinLength(Math.toIntExact(minLength.getValue()));
        settings.setMaxLength(Math.toIntExact(maxLength.getValue()));
        settings.setEnclosure(ButtonGroupHelper.getValue(enclosureGroup));
        settings.setCapitalization(CapitalizationMode.getMode(ButtonGroupHelper.getValue(capitalizationGroup)));

        settings.setBundledDictionaries(dictionaries.getEntries().stream()
                .filter(Dictionary.BundledDictionary.class::isInstance)
                .map(Dictionary::getUid)
                .collect(Collectors.toSet()));
        settings.setActiveBundledDictionaries(dictionaries.getActiveEntries().stream()
                .filter(Dictionary.BundledDictionary.class::isInstance)
                .map(Dictionary::getUid)
                .collect(Collectors.toSet()));
        Dictionary.BundledDictionary.clearCache();

        settings.setUserDictionaries(dictionaries.getEntries().stream()
                .filter(Dictionary.UserDictionary.class::isInstance)
                .map(Dictionary::getUid)
                .collect(Collectors.toSet()));
        settings.setActiveUserDictionaries(dictionaries.getActiveEntries().stream()
                .filter(Dictionary.UserDictionary.class::isInstance)
                .map(Dictionary::getUid)
                .collect(Collectors.toSet()));
        Dictionary.UserDictionary.clearCache();
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        if (dictionaries.getActiveEntries().isEmpty()) {
            return new ValidationInfo("Select at least one dictionary.", dictionaries);
        }

        final Optional<ValidationInfo> invalidDictionary = dictionaries.getActiveEntries().stream()
                .map(Dictionary::validate)
                .filter(Objects::nonNull)
                .findFirst();
        if (invalidDictionary.isPresent()) {
            return new ValidationInfo(invalidDictionary.get().message, dictionaries);
        }

        try {
            minLength.validateValue();
            maxLength.validateValue();
            lengthRange.validate();
        } catch (final ValidationException e) {
            return new ValidationInfo(e.getMessage(), e.getComponent());
        }

        return null;
    }


    /**
     * Fires when a new {@code Dictionary} is added to the list.
     */
    private void addDictionary() {
        FileChooser.chooseFiles(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"), null, null, files -> {
            if (files.isEmpty()) {
                return;
            }

            final ValidationInfo validationInfo = Dictionary.UserDictionary.validate(files.get(0).getCanonicalPath());
            if (validationInfo != null) {
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder(validationInfo.message, MessageType.ERROR, null)
                        .createBalloon()
                        .show(RelativePoint.getSouthOf(dictionaryAddButton), Balloon.Position.below);
                return;
            }

            final Dictionary newDictionary = Dictionary.UserDictionary.get(files.get(0).getCanonicalPath(), false);
            dictionaries.addEntry(newDictionary);
        });
    }

    /**
     * Fires when the currently highlighted {@code Dictionary} should be removed the list.
     */
    private void removeDictionary() {
        dictionaries.getHighlightedEntry().ifPresent(dictionary -> {
            if (dictionary instanceof Dictionary.UserDictionary) {
                dictionaries.removeEntry(dictionary);
            }
        });
    }

    /**
     * Fires when the user (un)highlights a dictionary.
     *
     * @param event the triggering event
     */
    private void onDictionaryHighlightChange(final ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            final Optional<Dictionary> highlightedDictionary = dictionaries.getHighlightedEntry();
            final boolean enable = highlightedDictionary.isPresent()
                    && highlightedDictionary.get() instanceof Dictionary.UserDictionary;
            dictionaryRemoveButton.setEnabled(enable);
        }
    }

    /**
     * Fires when the user (de)activates a dictionary.
     */
    private void onDictionaryActivityChange() {
        final Dictionary dictionary = Dictionary.combine(dictionaries.getActiveEntries());

        if (dictionary.getWords().isEmpty()) {
            minLength.setMaxValue(1);
            maxLength.setMinValue(Integer.MAX_VALUE);
        } else {
            minLength.setMaxValue(dictionary.getLongestWord().length());
            maxLength.setMinValue(dictionary.getShortestWord().length());
        }
    }
}
