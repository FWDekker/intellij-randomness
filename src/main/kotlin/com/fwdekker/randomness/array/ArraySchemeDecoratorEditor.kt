package com.fwdekker.randomness.array

import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.array.ArraySchemeDecorator.Companion.DEFAULT_BRACKETS
import com.fwdekker.randomness.array.ArraySchemeDecorator.Companion.DEFAULT_SEPARATOR
import com.fwdekker.randomness.array.ArraySchemeDecorator.Companion.MIN_COUNT
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.getValue
import com.fwdekker.randomness.ui.setValue
import com.jgoodies.forms.factories.DefaultComponentFactory
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent


/**
 * Component for settings of random array generation.
 *
 * @param settings the settings to edit in the component
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class ArraySchemeDecoratorEditor(settings: ArraySchemeDecorator) : SchemeEditor<ArraySchemeDecorator>(settings) {
    override lateinit var rootComponent: JPanel private set
    private lateinit var separator: JComponent
    private lateinit var enabledCheckBox: JCheckBox
    private lateinit var arrayDetailsPanel: JPanel
    private lateinit var countSpinner: JIntSpinner
    private lateinit var bracketsGroup: ButtonGroup
    private lateinit var separatorGroup: ButtonGroup
    private lateinit var newlineSeparatorButton: JRadioButton
    private lateinit var spaceAfterSeparatorCheckBox: JCheckBox


    init {
        enabledCheckBox.addChangeListener { arrayDetailsPanel.isVisible = enabledCheckBox.isSelected }
        enabledCheckBox.changeListeners.forEach { it.stateChanged(ChangeEvent(enabledCheckBox)) }

        newlineSeparatorButton.addChangeListener {
            spaceAfterSeparatorCheckBox.isEnabled = !newlineSeparatorButton.isSelected
        }
        newlineSeparatorButton.changeListeners.forEach { it.stateChanged(ChangeEvent(newlineSeparatorButton)) }

        loadScheme()
    }

    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        val bundle = ResourceBundle.getBundle("randomness")
        val factory = DefaultComponentFactory.getInstance()

        countSpinner = JIntSpinner(value = MIN_COUNT, minValue = MIN_COUNT)
        separator = factory.createSeparator(bundle.getString("settings.array"))
    }


    override fun loadScheme(scheme: ArraySchemeDecorator) {
        super.loadScheme(scheme)

        enabledCheckBox.isSelected = scheme.enabled
        countSpinner.value = scheme.count
        bracketsGroup.setValue(scheme.brackets)
        separatorGroup.setValue(scheme.separator)
        spaceAfterSeparatorCheckBox.isSelected = scheme.isSpaceAfterSeparator
    }

    override fun readScheme(): ArraySchemeDecorator =
        ArraySchemeDecorator(
            enabled = enabledCheckBox.isSelected,
            count = countSpinner.value,
            brackets = bracketsGroup.getValue() ?: DEFAULT_BRACKETS,
            separator = separatorGroup.getValue() ?: DEFAULT_SEPARATOR,
            isSpaceAfterSeparator = spaceAfterSeparatorCheckBox.isSelected
        )


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            enabledCheckBox, countSpinner, bracketsGroup, separatorGroup, spaceAfterSeparatorCheckBox,
            listener = listener
        )
}
