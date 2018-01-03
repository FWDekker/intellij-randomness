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
    private JList resourceDictionaryList;
    private JList localDictionaryList;
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

        resourceDictionaryList = new JList<>();
        resourceDictionaryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resourceDictionaryList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        localDictionaryList = new JList<>();
        localDictionaryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        localDictionaryList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        dictionaryAddButton = new JButton();
        dictionaryAddButton.addActionListener(e -> {
            final VirtualFile newDictionarySource = FileChooser
                    .chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor("dic"), null, null);
            if (newDictionarySource == null) {
                return;
            }

            final Dictionary newDictionary = new Dictionary.LocalDictionary(newDictionarySource.getCanonicalPath());
            final Set<Dictionary> allDictionaries =
                    IntStream.range(0, localDictionaryList.getModel().getSize())
                            .mapToObj(index -> (Dictionary) localDictionaryList.getModel().getElementAt(index))
                            .collect(Collectors.toSet());
            allDictionaries.add(newDictionary);
            localDictionaryList.setListData(allDictionaries.toArray());
        });
    }


    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minLength and such are always non-null
    public void loadSettings(final @NotNull WordSettings settings) {
        minLength.setValue(settings.getMinLength());
        maxLength.setValue(settings.getMaxLength());
        ButtonGroupHelper.setValue(enclosureGroup, settings.getEnclosure());
        ButtonGroupHelper.setValue(capitalizationGroup, settings.getCapitalization());

        resourceDictionaryList.setListData(settings.getResourceDictionaries().toArray());
        for (int i = 0; i < settings.getResourceDictionaries().size(); i++) {
            if (settings.getSelectedResourceDictionaries().contains(settings.getResourceDictionaries().toArray()[i])) {
                resourceDictionaryList.addSelectionInterval(i, i);
            }
        }

        localDictionaryList.setListData(settings.getLocalDictionaries().toArray());
        for (int i = 0; i < settings.getLocalDictionaries().size(); i++) {
            if (settings.getSelectedLocalDictionaries().contains(settings.getLocalDictionaries().toArray()[i])) {
                localDictionaryList.addSelectionInterval(i, i);
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
        settings.setResourceDictionaries(IntStream.range(0, resourceDictionaryList.getModel().getSize())
                                                 .mapToObj(index -> resourceDictionaryList.getModel()
                                                         .getElementAt(index).toString())
                                                 .collect(Collectors.toSet()));
        settings.setSelectedResourceDictionaries(new HashSet<>(resourceDictionaryList.getSelectedValuesList()));
        settings.setLocalDictionaries(IntStream.range(0, localDictionaryList.getModel().getSize())
                                              .mapToObj(index -> localDictionaryList.getModel()
                                                      .getElementAt(index).toString())
                                              .collect(Collectors.toSet()));
        settings.setSelectedLocalDictionaries(new HashSet<>(localDictionaryList.getSelectedValuesList()));
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
