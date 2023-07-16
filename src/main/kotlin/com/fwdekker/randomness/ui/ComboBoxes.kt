package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import javax.swing.DefaultComboBoxModel
import javax.swing.JList
import javax.swing.text.AbstractDocument
import javax.swing.text.DocumentFilter
import javax.swing.text.JTextComponent


/**
 * A [ComboBox] to choose a string.
 *
 * Strings are rendered as they are, except for some minor modifications for understandability.
 *
 * @param strings the default strings shown to the user
 * @param filter the filter that enforces which inputs are valid
 */
class StringComboBox(strings: List<String>, filter: DocumentFilter? = null) :
    ComboBox<String>(DefaultComboBoxModel(strings.toTypedArray())) {
    init {
        ((editor.editorComponent as? JTextComponent)?.document as? AbstractDocument)?.documentFilter = filter

        renderer = object : SimpleListCellRenderer<String>() {
            override fun customize(
                list: JList<out String>,
                value: String,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean,
            ) {
                text =
                    if (value == "") Bundle("shared.option.none")
                    else value
            }
        }
    }


    /**
     * Returns the selected item; the user may still be typing.
     *
     * @return the selected item
     */
    override fun getItem(): String = (editor.editorComponent as? JTextComponent)?.text ?: super.getItem()
}


/**
 * A [ComboBox] to choose a [CapitalizationMode].
 *
 * @param modes the default capitalization modes shown to the user
 */
class CapitalizationComboBox(modes: List<CapitalizationMode>) : ComboBox<CapitalizationMode>(modes.toTypedArray()) {
    init {
        renderer = object : SimpleListCellRenderer<CapitalizationMode>() {
            override fun customize(
                list: JList<out CapitalizationMode>,
                value: CapitalizationMode,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean,
            ) {
                text = Bundle("shared.capitalization.${value.descriptor.replace(' ', '_')}")
            }
        }
    }
}
