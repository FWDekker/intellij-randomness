package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.layout.ComponentPredicate
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JTextField


/**
 * Evaluates [spec] inside a range of rows, indenting only if [indented] is `true`.
 *
 * @receiver Panel the panel on which to apply [spec]
 * @param indented `true` if and only if the range of rows should be indented
 * @param spec the specification of the range of rows
 * @return the created range of rows
 */
fun Panel.indentedIf(indented: Boolean, spec: Panel.() -> Unit) =
    if (indented) indent(spec)
    else rowsRange(spec)


/**
 * Enforces a fixed width for this [JComponent].
 *
 * @receiver the component to enforce a fixed [width] onto
 * @param width the fixed width to enforce onto [this]
 */
fun JComponent.setFixedWidth(width: Int) {
    if (this is JTextField)
        columns(0)

    minimumSize = Dimension(width, minimumSize.height)
    preferredSize = Dimension(width, preferredSize.height)
    maximumSize = Dimension(width, maximumSize.height)
}

/**
 * Enforces a fixed width for the [JComponent] in this [Cell].
 *
 * @param T the type of component
 * @receiver the cell to enforce a fixed [width] onto
 * @param width the fixed width to enforce onto [this]
 * @return [this]
 */
fun <T : JComponent> Cell<T>.withFixedWidth(width: Int): Cell<T> {
    component.setFixedWidth(width)
    return this
}

/**
 * Enforces a fixed height for this [JComponent].
 *
 * @receiver the component to enforce a fixed [height] onto
 * @param height the fixed height to enforce onto [this]
 */
fun JComponent.setFixedHeight(height: Int) {
    minimumSize = Dimension(minimumSize.width, height)
    preferredSize = Dimension(preferredSize.width, height)
    maximumSize = Dimension(maximumSize.width, height)
}

/**
 * Enforces a fixed height for the [JComponent] in this [Cell].
 *
 * @param T the type of component
 * @receiver the cell to enforce a fixed [height] onto
 * @param height the fixed height to enforce onto [this]
 * @return [this]
 */
fun <T : JComponent> Cell<T>.withFixedHeight(height: Int): Cell<T> {
    component.setFixedHeight(height)
    return this
}


/**
 * Creates a [ComponentPredicate] that evaluates a [lambda] on the value of this [JIntSpinner].
 *
 * @receiver the spinner to check the value of
 * @param lambda the function to evaluate on the value of this spinner
 * @return the created predicate
 */
fun JIntSpinner.hasValue(lambda: (Int) -> Boolean) =
    object : ComponentPredicate() {
        override fun invoke() = lambda(this@hasValue.value)

        override fun addListener(listener: (Boolean) -> Unit) {
            this@hasValue.addChangeListener { listener(invoke()) }
        }
    }

/**
 * Creates a [ComponentPredicate] that evaluates a [lambda] on the value of this [ComboBox].
 *
 * @param E the type of value contained in the [ComboBox]
 * @receiver the combo box to check the value of
 * @param lambda the function to evaluate on the value of this combo box
 * @return the created predicate
 */
fun <E> ComboBox<E>.hasItem(lambda: (E) -> Boolean) =
    object : ComponentPredicate() {
        override fun invoke() = lambda(this@hasItem.item)

        override fun addListener(listener: (Boolean) -> Unit) {
            addChangeListenerTo(this@hasItem) { listener(invoke()) }
        }
    }
