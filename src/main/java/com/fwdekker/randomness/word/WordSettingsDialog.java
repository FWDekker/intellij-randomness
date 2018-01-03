package com.fwdekker.randomness.word;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.common.ValidationException;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JEditableTable;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random word generation.
 */
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
    private JEditableTable<Dictionary> bundledDictionaries;
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
        minLength = new JLongSpinner(Dictionary.getDefaultDictionary().shortestWordLength(), Integer.MAX_VALUE);
        maxLength = new JLongSpinner(1, Dictionary.getDefaultDictionary().longestWordLength());
        lengthRange = new JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE);

        bundledDictionaries = new JEditableTable<>();

        dictionaryAddButton = new JButton();
        dictionaryAddButton.addActionListener(event -> {
            final VirtualFile newDictionarySource = FileChooser
                    .chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"), null, null);

            if (newDictionarySource != null) {
                bundledDictionaries.addEntry(new Dictionary.CustomDictionary(newDictionarySource.getCanonicalPath()));
            }
        });
        dictionaryRemoveButton = new JButton();
        dictionaryRemoveButton.addActionListener(event -> {
            bundledDictionaries.getHighlightedEntry().ifPresent(dictionary -> {
                if (dictionary instanceof Dictionary.CustomDictionary) {
                    bundledDictionaries.removeEntry(dictionary);
                } else {
                    throw new IllegalStateException("Button should have been disabled.");
                }
            });
        });

        bundledDictionaries.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                final Optional<Dictionary> highlightedDictionary = bundledDictionaries.getHighlightedEntry();
                dictionaryRemoveButton.setEnabled(highlightedDictionary.isPresent() && highlightedDictionary.get() instanceof Dictionary.CustomDictionary);
            }
        });
    }


    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupHelper.setValue(capitalizationGroup, settings.getCapitalization());

        bundledDictionaries.setEntries(settings.getAllDictionaries());
        bundledDictionaries.setActiveEntries(settings.getActiveDictionaries());
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void saveSettings(final @NotNull WordSettings settings) {
        settings.setMinLength(Math.toIntExact(minLength.getValue()));
        settings.setMaxLength(Math.toIntExact(maxLength.getValue()));
        settings.setEnclosure(ButtonGroupHelper.getValue(enclosureGroup));
        settings.setCapitalization(CapitalizationMode.getMode(ButtonGroupHelper.getValue(capitalizationGroup)));

        settings.setBundledDictionaries(bundledDictionaries.getEntries().stream()
                                                .filter(Dictionary.BundledDictionary.class::isInstance)
                                                .map(Dictionary::getPath)
                                                .collect(Collectors.toSet()));
        settings.setActiveBundledDictionaries(bundledDictionaries.getActiveEntries().stream()
                                                      .filter(Dictionary.BundledDictionary.class::isInstance)
                                                      .map(Dictionary::getPath)
                                                      .collect(Collectors.toSet()));
        settings.setCustomDictionaries(bundledDictionaries.getEntries().stream()
                                               .filter(Dictionary.CustomDictionary.class::isInstance)
                                               .map(Dictionary::getPath)
                                               .collect(Collectors.toSet()));
        settings.setActiveCustomDictionaries(bundledDictionaries.getActiveEntries().stream()
                                                     .filter(Dictionary.CustomDictionary.class::isInstance)
                                                     .map(Dictionary::getPath)
                                                     .collect(Collectors.toSet()));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            if (bundledDictionaries.getActiveEntries().isEmpty()) {
                throw new ValidationException("Select at least one dictionary.", bundledDictionaries);
            }

            final Dictionary dictionary = Dictionary.combine(bundledDictionaries.getActiveEntries());
            minLength.setMinValue(dictionary.shortestWordLength());
            maxLength.setMaxValue(dictionary.longestWordLength());

            minLength.validateValue();
            maxLength.validateValue();
            lengthRange.validate();
        } catch (final ValidationException e) {
            return new ValidationInfo(e.getMessage(), e.getComponent());
        }

        return null;
    }
}
