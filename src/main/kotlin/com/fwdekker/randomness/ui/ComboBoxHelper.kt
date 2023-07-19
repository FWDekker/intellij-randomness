package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
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

/**
 * Sets a [DocumentFilter] on this [ComboBox]'s document.
 *
 * @receiver the combo box to set a filter on
 * @param filter the filter to set
 */
fun ComboBox<*>.setFilter(filter: DocumentFilter) {
    ((editor.editorComponent as? JTextComponent)?.document as? AbstractDocument)?.documentFilter = filter
}

/**
 * Sets a [SimpleListCellRenderer] on this [ComboBox] that sets the text of a cell based only on its value.
 *
 * @param E the type of value contained in this combo box
 * @receiver the combo box to set the renderer on
 * @param renderer the renderer that gives the text to render for a given value
 */
fun <E> ComboBox<E>.setSimpleRenderer(renderer: (E) -> String) {
    this.renderer = object : SimpleListCellRenderer<E>() {
        override fun customize(list: JList<out E>, value: E, index: Int, selected: Boolean, hasFocus: Boolean) {
            text = renderer(value)
        }
    }
}
