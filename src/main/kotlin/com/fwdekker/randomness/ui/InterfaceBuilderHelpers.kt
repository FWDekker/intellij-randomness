package com.fwdekker.randomness.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.TopGap
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
 * Creates and returns a range of rows from [init], headed by an underlined [title] if and only if [title] is not
 * `null`, and indented if and only if [indent] is `true`.
 */
fun Panel.decoratedRowRange(title: String? = null, indent: Boolean, init: Panel.() -> Unit) =
    when {
        title != null -> rowsRange { group(title, indent = indent, init = init).topGap(TopGap.MEDIUM) }
        indent -> indent(init)
        else -> rowsRange(init)
    }


/**
 * Registers the [callback] to be invoked on the [JComponent] in this [Cell] when the dialog is reset, and returns
 * `this`.
 */
fun <T : JComponent> Cell<T>.onResetThis(callback: (Cell<T>) -> Unit) = onReset { callback(this) }

/**
 * Sets the [name] of the [JComponent] in this [Cell] and returns `this`.
 */
fun <T : JComponent> Cell<T>.withName(name: String) = this.also { it.component.name = name }

/**
 * Forces this [JComponent] to be [width] pixels wide.
 */
fun JComponent.setFixedWidth(width: Int) {
    if (this is JTextField)
        columns(0)

    minimumSize = Dimension(width, minimumSize.height)
    preferredSize = Dimension(width, preferredSize.height)
    maximumSize = Dimension(width, maximumSize.height)
}

/**
 * Forces the [JComponent] in this [Cell] to be [width] pixels wide, and returns `this`.
 */
fun <T : JComponent> Cell<T>.withFixedWidth(width: Int) = this.also { it.component.setFixedWidth(width) }

/**
 * Forces this [JComponent] to be [height] pixels high.
 */
fun JComponent.setFixedHeight(height: Int) {
    minimumSize = Dimension(minimumSize.width, height)
    preferredSize = Dimension(preferredSize.width, height)
    maximumSize = Dimension(maximumSize.width, height)
}

/**
 * Forces the [JComponent] in this [Cell] to be [height] pixels high, and returns `this`.
 */
fun <T : JComponent> Cell<T>.withFixedHeight(height: Int): Cell<T> {
    component.setFixedHeight(height)
    return this
}


/**
 * Loads the mnemonic for the [AbstractButton] in this [Cell] based on its [AbstractButton.text].
 */
fun <T : AbstractButton> Cell<T>.loadMnemonic(): Cell<T> {
    DialogUtil.registerMnemonic(component, '&')
    return this
}

/**
 * Removes the mnemonic from the [AbstractButton.text] of the [AbstractButton] in this [Cell].
 */
fun <T : AbstractButton> Cell<T>.disableMnemonic(): Cell<T> {
    component.text = component.text.filterNot { it == '&' }
    return this
}


/**
 * Sets the [document] of the [JTextField] in this [Cell] and returns `this`.
 */
fun <T : JTextField> Cell<T>.withDocument(document: Document) = this.also { component.document = document }


/**
 * Sets whether the editor of the [ComboBox] in this [Cell] [isEditable], and returns `this`.
 */
fun <E> Cell<ComboBox<E>>.isEditable(editable: Boolean) = this.also { it.component.isEditable = editable }

/**
 * Sets the [filter] on the document of the [ComboBox] in this [Cell], and returns `this`.
 */
fun <E> Cell<ComboBox<E>>.withFilter(filter: DocumentFilter): Cell<ComboBox<E>> {
    ((component.editor.editorComponent as? JTextComponent)?.document as? AbstractDocument)?.documentFilter = filter
    return this
}


/**
 * A predicate that always returns [output].
 */
class LiteralPredicate(private val output: Boolean) : ComponentPredicate() {
    /**
     * Does nothing, because there's no need to respond to any events to determine the output of [invoke].
     */
    override fun addListener(listener: (Boolean) -> Unit) = Unit

    /**
     * Returns [output].
     */
    override fun invoke() = output
}

/**
 * Returns a [ComponentPredicate] that evaluates [lambda] on the value of this [JIntSpinner].
 */
fun JIntSpinner.hasValue(lambda: (Int) -> Boolean) =
    object : ComponentPredicate() {
        override fun invoke() = lambda(this@hasValue.value)

        override fun addListener(listener: (Boolean) -> Unit) {
            this@hasValue.addChangeListener { listener(invoke()) }
        }
    }


/**
 * Binds the current possibly-non-committed value of the [ComboBox] in this [Cell] to [property].
 */
fun Cell<ComboBox<String>>.bindCurrentText(property: KMutableProperty0<String>) =
    bind(
        { comboBox -> (comboBox.editor.editorComponent as? JTextComponent)?.text ?: comboBox.item.toString() },
        { comboBox, value -> comboBox.item = value },
        property.toMutableProperty()
    )

/**
 * Binds the value of the [JIntSpinner] in this [Cell] to [property].
 */
fun Cell<JIntSpinner>.bindIntValue(property: KMutableProperty0<Int>) =
    bind(
        { spinner -> spinner.value },
        { spinner, value -> spinner.value = value },
        property.toMutableProperty()
    )

/**
 * Binds the value of the [JLongSpinner] in this [Cell] to [property].
 */
fun Cell<JLongSpinner>.bindLongValue(property: KMutableProperty0<Long>) =
    bind(
        { spinner -> spinner.value },
        { spinner, value -> spinner.value = value },
        property.toMutableProperty()
    )

/**
 * Binds the [Long] representation of the value of the [JDateTimeField] in this [Cell] to [property].
 */
fun Cell<JDateTimeField>.bindDateTimeLongValue(property: KMutableProperty0<Long>) =
    bind(
        { field -> field.longValue },
        { field, value -> field.longValue = value },
        property.toMutableProperty()
    )
