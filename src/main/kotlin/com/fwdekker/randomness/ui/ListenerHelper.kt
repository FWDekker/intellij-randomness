package com.fwdekker.randomness.ui

import com.fwdekker.randomness.StateEditor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.beans.PropertyChangeEvent
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document


/**
 * Adds a `ChangeListener` to each of the given components.
 *
 * @param components the components to add the listener to
 * @param listener the listener to invoke whenever any of the given components changes state
 */
@Suppress("SpreadOperator") // Acceptable because this method is called rarely
fun addChangeListenerTo(vararg components: Any, listener: () -> Unit) {
    components.forEach { component ->
        when (component) {
            is ActivityTableModelEditor<*> -> component.addChangeListener(listener)
            is ButtonGroup -> addChangeListenerTo(*component.buttons(), listener = listener)
            is JCheckBox -> component.addItemListener { listener() }
            is JRadioButton -> component.addItemListener { listener() }
            is JSpinner -> component.addChangeListener { listener() }
            is JTextField -> component.addChangeListener { listener() }
            is StateEditor<*> -> component.addChangeListener { listener() }
            else -> throw IllegalArgumentException("Unknown component type '${component.javaClass.canonicalName}'.")
        }
    }
}

/**
 * Adds a `ChangeListener` to a text field.
 *
 * Code taken from [StackOverflow][https://stackoverflow.com/a/27190162/3307872], but without the `invokeLater`.
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


/**
 * A [MouseListener] that listens only to mouse clicks.
 *
 * @property listener The listener to invoke whenever the mouse is clicked on the element that this listener is attached
 * to.
 */
class MouseClickListener(private val listener: (MouseEvent?) -> Unit) : MouseListener {
    /**
     * Invokes the [listener].
     *
     * @param event the event that triggered the listener
     */
    override fun mouseClicked(event: MouseEvent?) = listener(event)

    /**
     * Does nothing.
     *
     * @param event ignored
     */
    override fun mousePressed(event: MouseEvent?) = Unit

    /**
     * Does nothing.
     *
     * @param event ignored
     */
    override fun mouseReleased(event: MouseEvent?) = Unit

    /**
     * Does nothing.
     *
     * @param event ignored
     */
    override fun mouseEntered(event: MouseEvent?) = Unit

    /**
     * Does nothing.
     *
     * @param event ignored
     */
    override fun mouseExited(event: MouseEvent?) = Unit
}
