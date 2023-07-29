package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.Cell
import javax.swing.JList
import javax.swing.text.AbstractDocument
import javax.swing.text.DocumentFilter
import javax.swing.text.JTextComponent


/**
 * Returns the current text in the [ComboBox], even if the user is still typing.
 *
 * @receiver the combo box to return the current text of
 * @return the current text in the [ComboBox], even if the user is still typing
 */
fun ComboBox<*>.getCurrentText(): String =
    (editor.editorComponent as? JTextComponent)?.text ?: item.toString()

// TODO: Document
fun <T : ComboBox<*>> Cell<T>.isEditable(editable: Boolean) =
    this.also { it.component.isEditable = editable }

/**
 * Sets a [DocumentFilter] on this [ComboBox]'s document.
 *
 * @receiver the combo box to set a filter on
 * @param filter the filter to set
 */
fun <E> Cell<ComboBox<E>>.withFilter(filter: DocumentFilter) =
    this.also {
        ((component.editor.editorComponent as? JTextComponent)?.document as? AbstractDocument)?.documentFilter = filter
    }

/**
 * Sets a [SimpleListCellRenderer] on the [ComboBox] on this [Cell] that sets the text of a cell based only on its
 * value.
 *
 * @param E the type of value contained in the combo box
 * @receiver the combo box to set the renderer on
 * @param renderer the renderer that gives the text to render for a given value
 */
fun <E> Cell<ComboBox<E>>.withSimpleRenderer(renderer: (E) -> String) =
    this.also {
        component.renderer = object : SimpleListCellRenderer<E>() {
            override fun customize(list: JList<out E>, value: E, index: Int, selected: Boolean, hasFocus: Boolean) {
                text = renderer(value)
            }
        }
    }
