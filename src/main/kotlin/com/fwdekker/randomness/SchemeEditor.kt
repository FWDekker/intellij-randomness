package com.fwdekker.randomness

import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent


// TODO: Document this
abstract class SchemeEditor<S : Scheme>(val scheme: S) : Disposable {
    /**
     * The root component of the editor.
     */
    abstract val rootComponent: DialogPanel

    /**
     * The components contained within this editor that determine the editor's current state.
     */
    val components: Collection<Any>
        get() = rootComponent.components.filterNot { it.name == null } + extraComponents

    // TODO: Document this
    protected val extraComponents = mutableListOf<Any>()

    // TODO: Document this
    protected val decoratorEditors = mutableListOf<SchemeEditor<*>>()

    /**
     * The component that this editor prefers to be focused when the editor is focused.
     */
    open val preferredFocusedComponent: JComponent?
        get() = components.filterIsInstance<JComponent>().firstOrNull { it.isVisible }


    /**
     * Resets the editor's state to that of [scheme].
     *
     * If [apply] has been called, then [reset] resets to the state at the last invocation of [apply].
     */
    fun reset() {
        rootComponent.reset()
        decoratorEditors.forEach { it.reset() }
    }

    /**
     * Saves the editor's state into [scheme].
     */
    fun apply() {
        rootComponent.apply()
        decoratorEditors.forEach { it.apply() }
    }


    // TODO: Document this
    fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(*(components + decoratorEditors).toTypedArray(), listener = listener)


    /**
     * Disposes of this editor's resources.
     */
    override fun dispose() = Disposer.dispose(this)
}
