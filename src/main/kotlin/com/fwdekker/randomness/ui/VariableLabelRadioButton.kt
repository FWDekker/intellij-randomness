package com.fwdekker.randomness.ui

import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ItemEvent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField
import javax.swing.text.AbstractDocument
import javax.swing.text.DocumentFilter


/**
 * A [JRadioButton] of which the label can be changed using a [JTextField].
 *
 * @param width the preferred width of the text field
 * @param filter the filter to apply to the text field
 */
class VariableLabelRadioButton(
    width: Int = UIConstants.SIZE_TINY,
    filter: DocumentFilter? = null,
) : JPanel(BorderLayout()) {
    /**
     * The button that uses [textField] as its label.
     */
    // TODO: Really make this public? Maybe make it extend `AbstractButton` instead?
    val button = object : JBRadioButton() {
        override fun getActionCommand() = textField.text
    }

    /**
     * The editable label in the form of a [JTextField].
     */
    val textField = JBTextField()
        .also { (it.document as AbstractDocument).documentFilter = filter }

    /**
     * The current label, i.e. the contents of the text field.
     */
    var label: String
        get() = textField.text
        set(value) {
            textField.text = value
        }


    init {
        button.addItemListener(
            { _: ItemEvent? ->
                textField.isEnabled = button.isSelected
                if (button.isSelected) textField.requestFocus()
            }.also { it(null) }
        )
        textField.addFocusListener(FocusGainListener { button.isSelected = true })

        add(button, BorderLayout.WEST)
        add(textField, BorderLayout.CENTER)

        button.text = " "
        val preferredButtonHeight = button.preferredSize.height
        button.text = ""

        textField.preferredSize = Dimension(width, preferredButtonHeight)
        textField.font = textField.font.deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL))
        minimumSize = Dimension(minimumSize.width, preferredButtonHeight)
        preferredSize = Dimension(preferredSize.width, preferredButtonHeight)
    }


    /**
     * Adds [listener] as a listener to the button and the text field.
     *
     * @param listener the function to invoke whenever the button or the text field changes
     */
    fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(button, textField, listener = listener)


    /**
     * Enables or disables the button and the text field.
     *
     * @param enabled `true` if and only if this component should be enabled
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        button.isEnabled = enabled
        textField.isEnabled = enabled && button.isSelected
    }
}
