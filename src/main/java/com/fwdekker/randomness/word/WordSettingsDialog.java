package com.fwdekker.randomness.word;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.common.ValidationException;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
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
    private JList bundledDictionaryList;
    private JList customDictionaryList;
    private JButton dictionaryAddButton;


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
        minLength = new JLongSpinner(1, Dictionary.getDefaultDictionary().longestWordLength());
        maxLength = new JLongSpinner(1, Dictionary.getDefaultDictionary().longestWordLength());
        lengthRange = new JSpinnerRange(minLength, maxLength, Integer.MAX_VALUE);

        bundledDictionaryList = new JList<>();
        bundledDictionaryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        bundledDictionaryList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        customDictionaryList = new JList<>();
        customDictionaryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        customDictionaryList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        dictionaryAddButton = new JButton();
        dictionaryAddButton.addActionListener(event -> {
            final VirtualFile newDictionarySource = FileChooser
                    .chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"), null, null);
            if (newDictionarySource == null) {
                return;
            }

            final Dictionary newDictionary = new Dictionary.CustomDictionary(newDictionarySource.getCanonicalPath());
            final Set<Dictionary> allDictionaries =
                    IntStream.range(0, customDictionaryList.getModel().getSize())
                            .mapToObj(index -> (Dictionary) customDictionaryList.getModel().getElementAt(index))
                            .collect(Collectors.toSet());
            allDictionaries.add(newDictionary);
            customDictionaryList.setListData(allDictionaries.toArray());
        });
    }


    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupHelper.setValue(capitalizationGroup, settings.getCapitalization());

        bundledDictionaryList.setListData(settings.getBundledDictionaries().toArray());
        for (int i = 0; i < settings.getBundledDictionaries().size(); i++) {
            if (settings.getActiveBundledDictionaries().contains(settings.getBundledDictionaries().toArray()[i])) {
                bundledDictionaryList.addSelectionInterval(i, i);
            }
        }

        customDictionaryList.setListData(settings.getCustomDictionaries().toArray());
        for (int i = 0; i < settings.getCustomDictionaries().size(); i++) {
            if (settings.getActiveCustomDictionaries().contains(settings.getCustomDictionaries().toArray()[i])) {
                customDictionaryList.addSelectionInterval(i, i);
            }
        }
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void saveSettings(final @NotNull WordSettings settings) {
        settings.setMinLength(Math.toIntExact(minLength.getValue()));
        settings.setMaxLength(Math.toIntExact(maxLength.getValue()));
        settings.setEnclosure(ButtonGroupHelper.getValue(enclosureGroup));
        settings.setCapitalization(CapitalizationMode.getMode(ButtonGroupHelper.getValue(capitalizationGroup)));
        settings.setBundledDictionaries(IntStream.range(0, bundledDictionaryList.getModel().getSize())
                                                .mapToObj(index -> bundledDictionaryList.getModel()
                                                        .getElementAt(index).toString())
                                                .collect(Collectors.toSet()));
        settings.setActiveBundledDictionaries(new HashSet<>(bundledDictionaryList.getSelectedValuesList()));
        settings.setCustomDictionaries(IntStream.range(0, customDictionaryList.getModel().getSize())
                                               .mapToObj(index -> customDictionaryList.getModel()
                                                       .getElementAt(index).toString())
                                               .collect(Collectors.toSet()));
        settings.setActiveCustomDictionaries(new HashSet<>(customDictionaryList.getSelectedValuesList()));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            minLength.validateValue();
            maxLength.validateValue();
            lengthRange.validate();
        } catch (final ValidationException e) {
            return new ValidationInfo(e.getMessage(), e.getComponent());
        }

        return null;
    }
}
