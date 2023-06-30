package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.JDateTimeField
import com.fwdekker.randomness.ui.addChangeListener
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.BrowserLink
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants


/**
 * Component for editing random date-time settings.
 *
 * @param scheme the scheme to edit in the component
 */
class DateTimeSchemeEditor(scheme: DateTimeScheme = DateTimeScheme()) : StateEditor<DateTimeScheme>(scheme) {
    override lateinit var rootComponent: JPanel private set
    override val preferredFocusedComponent
        get() = rootComponent

    private lateinit var valueSeparator: TitledSeparator
    private lateinit var minDateTimeField: JDateTimeField
    private lateinit var maxDateTimeField: JDateTimeField
    private lateinit var patternField: JTextField
    private lateinit var patternHelpButton: JButton
    private lateinit var patternCommentLabel: JLabel
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor
    private lateinit var arrayDecoratorPanel: JPanel


    init {
        bindDateTimes(minDateTimeField, maxDateTimeField)

        loadState()
    }

    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    private fun createUIComponents() {
        valueSeparator = SeparatorFactory.createSeparator(Bundle("datetime.ui.value_separator"), null)

        minDateTimeField = JDateTimeField(DateTimeScheme.DEFAULT_MIN_DATE_TIME.toLocalDateTime())
        maxDateTimeField = JDateTimeField(DateTimeScheme.DEFAULT_MAX_DATE_TIME.toLocalDateTime())

        patternHelpButton = BrowserLink(
            Bundle("datetime.ui.pattern_help"),
            "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html"
        )
        patternCommentLabel = JBLabel()
            .also {
                it.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                it.isFocusable = false
                it.setCopyable(false)
                it.verticalTextPosition = SwingConstants.TOP
            }

        arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        arrayDecoratorPanel = arrayDecoratorEditor.rootComponent
    }

    /**
     * Binds two `DateTimePicker`s together, analogous to how [com.fwdekker.randomness.ui.bindSpinners] works.
     */
    private fun bindDateTimes(minField: JDateTimeField, maxField: JDateTimeField) {
        minField.addChangeListener {
            val minEpoch = minField.value.toEpochMilli()
            val maxEpoch = maxField.value.toEpochMilli()

            if (minEpoch > maxEpoch) maxField.value = minField.value
        }
        maxField.addChangeListener {
            val minEpoch = minField.value.toEpochMilli()
            val maxEpoch = maxField.value.toEpochMilli()

            if (maxEpoch < minEpoch) minField.value = maxField.value
        }
    }


    override fun loadState(state: DateTimeScheme) {
        super.loadState(state)

        minDateTimeField.value = state.minDateTime.toLocalDateTime()
        maxDateTimeField.value = state.maxDateTime.toLocalDateTime()
        patternField.text = state.pattern
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        DateTimeScheme(
            minDateTime = minDateTimeField.value.toEpochMilli(),
            maxDateTime = maxDateTimeField.value.toEpochMilli(),
            pattern = patternField.text,
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minDateTimeField, maxDateTimeField, patternField, arrayDecoratorEditor,
            listener = listener
        )
}
