package com.fwdekker.randomness

import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent


/**
 * An editor for a [Scheme].
 *
 * @param S the type of scheme edited in this editor
 * @property scheme the scheme edited in this editor
 */
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

    /**
     * The additional [components] that determine the editor's current state but do not have a name.
     *
     * Do not register [SchemeEditor]s here; use the [decoratorEditors] field for that.
     */
    protected val extraComponents = mutableListOf<Any>()

    /**
     * The [SchemeEditor]s of [scheme]'s [DecoratorScheme]s.
     *
     * The editors registered in this list are automatically reset and applied in [reset] and [apply], respectively.
     */
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


    /**
     * Ensures [listener] is invoked on every change in this editor.
     *
     * @param listener the function to invoke on every change in this editor
     */
    @Suppress("detekt:SpreadOperator") // Acceptable because this method is called rarely
    fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(*(components + decoratorEditors).toTypedArray(), listener = listener)


    /**
     * Disposes this editor's resources.
     */
    override fun dispose() = Disposer.dispose(this)
}
