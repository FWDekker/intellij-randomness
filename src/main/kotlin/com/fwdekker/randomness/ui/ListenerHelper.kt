package com.fwdekker.randomness.ui

import com.fwdekker.randomness.StateEditor
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JComboBox
import javax.swing.JSpinner
import javax.swing.JTree
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.text.Document
import javax.swing.text.JTextComponent
import com.intellij.openapi.editor.Document as JBDocument
import com.intellij.openapi.editor.event.DocumentEvent as JBDocumentEvent
import com.intellij.openapi.editor.event.DocumentListener as JBDocumentListener


/**
 * Adds [listener] to each of [components].
 *
 * @param components the components to add the listener to
 * @param listener the listener to invoke whenever any of the given components changes state
 */
@Suppress("detekt:SpreadOperator") // Acceptable because this method is called rarely
fun addChangeListenerTo(vararg components: Any, listener: () -> Unit) {
    components.forEach { component ->
        when (component) {
            is AbstractButton -> component.addItemListener { listener() }
            is ButtonGroup -> addChangeListenerTo(*component.buttons(), listener = listener)
            is Document -> component.addDocumentListener(SimpleDocumentListener { listener() })
            is JBDocument -> component.addDocumentListener(SimpleJBDocumentListener { listener() })
            is JComboBox<*> -> {
                component.addActionListener { listener() }
                addChangeListenerTo(component.editor.editorComponent, listener = listener)
            }
            is JSpinner -> component.addChangeListener { listener() }
            is JTextComponent -> addChangeListenerTo(component.document, listener = listener)
            is JTree -> {
                component.model.addTreeModelListener(SimpleTreeModelListener { listener() })
                component.addTreeSelectionListener { listener() }
            }
            is StateEditor<*> -> component.addChangeListener { listener() }
            else -> Unit
        }
    }
}


/**
 * A [JBDocumentListener] that invokes [listener] on each event.
 *
 * @property listener The listener to invoke on any event.
 */
class SimpleJBDocumentListener(private val listener: (JBDocumentEvent) -> Unit) : JBDocumentListener {
    /**
     * Invoked after contents have been changed.
     *
     * @param event the event that triggered the listener
     */
    override fun documentChanged(event: JBDocumentEvent) {
        listener(event)
    }
}

/**
 * A [DocumentListener] that invokes [listener] on each event.
 *
 * @property listener The listener to invoke on any event.
 */
class SimpleDocumentListener(private val listener: (DocumentEvent) -> Unit) : DocumentListener {
    /**
     * Invoked after text has been inserted.
     *
     * @param event the event that triggered the listener
     */
    override fun insertUpdate(event: DocumentEvent) = listener(event)

    /**
     * Invoked after text has been removed.
     *
     * @param event the event that triggered the listener
     */
    override fun removeUpdate(event: DocumentEvent) = listener(event)

    /**
     * Invoked after attributes have been changed.
     *
     * @param event the event that triggered the listener
     */
    override fun changedUpdate(event: DocumentEvent) = listener(event)
}

/**
 * A [TreeModelListener] that invokes [listener] on each event.
 *
 * @property listener The listener to invoke on any event.
 */
class SimpleTreeModelListener(private val listener: (TreeModelEvent) -> Unit) : TreeModelListener {
    /**
     * Invoked after a node has changed.
     *
     * @param event the event that triggered the listener
     */
    override fun treeNodesChanged(event: TreeModelEvent) = listener(event)

    /**
     * Invoked after a node has been inserted.
     *
     * @param event the event that triggered the listener
     */
    override fun treeNodesInserted(event: TreeModelEvent) = listener(event)

    /**
     * Invoked after a node has been removed.
     *
     * @param event the event that triggered the listener
     */
    override fun treeNodesRemoved(event: TreeModelEvent) = listener(event)

    /**
     * Invoked after the structure of the tree has changed.
     *
     * @param event the event that triggered the listener
     */
    override fun treeStructureChanged(event: TreeModelEvent) = listener(event)
}


/**
 * A [FocusListener] that invokes [listener] when focus is gained only.
 *
 * @property listener The listener to invoke when focus is gained.
 */
class FocusGainListener(private val listener: (FocusEvent) -> Unit) : FocusListener {
    /**
     * Invokes [listener].
     *
     * @param event the event passed to [listener]
     */
    override fun focusGained(event: FocusEvent) {
        listener(event)
    }

    /**
     * Does nothing.
     *
     * @param event ignored
     */
    override fun focusLost(event: FocusEvent) {
        // Do nothing
    }
}
