package com.fwdekker.randomness.ui

import java.beans.PropertyChangeEvent
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JList
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document


// TODO: Docs
fun addChangeListenerTo(vararg components: Any, listener: () -> Unit) {
    components.forEach { component ->
        when (component) {
            is ActivityTableModelEditor<*> -> component.addChangeListener(listener)
            is ButtonGroup -> addChangeListenerTo(*component.buttons(), listener = listener)
            is JCheckBox -> component.addItemListener { listener() }
            is JList<*> -> component.addPropertyChangeListener { listener() }
            is JRadioButton -> component.addItemListener { listener() }
            is JSpinner -> component.addChangeListener { listener() }
            is JTextField -> component.addChangeListener { listener() }
            else -> throw IllegalArgumentException("Unknown component type '${component.javaClass.canonicalName}'.")
        }
    }
}


/**
 * Adds a `ChangeListener` to a text field.
 *
 * Code taken from [StackOverflow](https://stackoverflow.com/a/27190162/3307872), with without the `invokeLater`.
 *
 * @param changeListener the change listener that responds to changes in the text field
 */
fun JTextField.addChangeListener(changeListener: (JTextField) -> Unit) {
    val dl = object : DocumentListener {
        private var lastChange = 0
        private var lastNotifiedChange = 0


        override fun changedUpdate(e: DocumentEvent?) {
            lastChange++
            if (lastNotifiedChange == lastChange) return

            lastNotifiedChange = lastChange
            changeListener(this@addChangeListener)
        }

        override fun insertUpdate(e: DocumentEvent?) = changedUpdate(e)

        override fun removeUpdate(e: DocumentEvent?) = changedUpdate(e)
    }

    this.addPropertyChangeListener("document") { e: PropertyChangeEvent ->
        (e.oldValue as? Document)?.removeDocumentListener(dl)
        (e.newValue as? Document)?.addDocumentListener(dl)
        dl.changedUpdate(null)
    }
    this.document.addDocumentListener(dl)
}
