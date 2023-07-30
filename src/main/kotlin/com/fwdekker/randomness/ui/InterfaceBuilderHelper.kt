package com.fwdekker.randomness.ui

import com.fwdekker.randomness.datetime.toEpochMilli
import com.fwdekker.randomness.datetime.toLocalDateTime
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.toMutableProperty
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.DialogUtil
import java.awt.Dimension
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.text.AbstractDocument
import javax.swing.text.Document
import javax.swing.text.DocumentFilter
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty0


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


// TODO: Document
fun <T : JComponent> Cell<T>.onResetThis(callback: (Cell<T>) -> Unit) = onReset { callback(this) }

// TODO: Document
fun <T : JComponent> Cell<T>.withName(name: String) =
    this.also { it.component.name = name }

// TODO: Document
fun <T : JTextField> Cell<T>.withDocument(document: Document) =
    this.also { component.document = document }


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
 * Renders items in the [ComboBox] in this [Cell] as a [JBLabel] containing the output of [toString].
 *
 * @param E the type of value contained in the combo box
 * @receiver the combo box to set the renderer on
 * @param toString given the item to render, returns the string to put in the [JBLabel]
 */
fun <E> Cell<ComboBox<E>>.withSimpleRenderer(toString: (E) -> String = { it.toString() }) =
    this.also { component.setRenderer { _, value, _, _, _ -> JBLabel(toString(value)) } }


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
 * Loads the mnemonic for the [AbstractButton] in this cell based on its text.
 *
 * @receiver the cell with the [AbstractButton] to load the mnemonic of
 * @return [this]
 */
fun <B : AbstractButton> Cell<B>.loadMnemonic(): Cell<B> {
    DialogUtil.registerMnemonic(component, '&')
    return this
}

/**
 * Removes the mnemonic from the label of this [AbstractButton].
 *
 * @receiver the cell with the [AbstractButton] to disable the mnemonic of
 * @return [this]
 */
fun <B : AbstractButton> Cell<B>.disableMnemonic(): Cell<B> {
    component.text = component.text.filterNot { it == '&' }
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


fun Cell<ComboBox<String>>.bindCurrentText(property: KMutableProperty0<String>) =
    bind(
        { comboBox -> comboBox.getCurrentText() },
        { comboBox, value -> comboBox.item = value },
        property.toMutableProperty()
    )

fun Cell<JIntSpinner>.bindIntValue(property: KMutableProperty0<Int>) =
    bind(
        { spinner -> spinner.value },
        { spinner, value -> spinner.value = value },
        property.toMutableProperty()
    )

fun Cell<JLongSpinner>.bindLongValue(property: KMutableProperty0<Long>) =
    bind(
        { spinner -> spinner.value },
        { spinner, value -> spinner.value = value },
        property.toMutableProperty()
    )

fun Cell<JDateTimeField>.bindDateTimeLongValue(property: KMutableProperty0<Long>) =
    bind(
        { field -> field.value.toEpochMilli() },
        { field, value -> field.value = value.toLocalDateTime() },
        property.toMutableProperty()
    )
