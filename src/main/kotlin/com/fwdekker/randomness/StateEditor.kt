package com.fwdekker.randomness

import java.awt.Component
import javax.swing.JPanel


/**
 * A component that can be used to edit a [State].
 *
 * The editor's state is initially read from [originalState]. Changes in the editor are written into [originalState]
 * when they are applied using [applyState].
 *
 * @param S the type of state that is edited; should be a self-reference
 * @property originalState the state object to write changes into
 */
abstract class StateEditor<S : State>(val originalState: S) {
    /**
     * The root component of the editor.
     */
    abstract val rootComponent: JPanel

    /**
     * The component that this editor prefers to be focused when the editor is focused.
     */
    open val preferredFocusedComponent: Component? = null


    /**
     * Loads the given state into the editor and into [originalState].
     *
     * @param state the state to load
     */
    open fun loadState(state: S = originalState) {
        originalState.copyFrom(state)
    }

    /**
     * Returns the editor's current state, including potentially unsaved changes.
     *
     * @return the editor's current state, including potentially unsaved changes
     */
    abstract fun readState(): S

    /**
     * Saves the editor's state into [originalState].
     *
     * Does nothing if and only if [isModified] returns false.
     */
    open fun applyState() = originalState.copyFrom(readState())


    /**
     * Returns true if and only if the editor contains modifications relative to the last saved state.
     *
     * Override this method if the default equals method of [S] is not sufficient to detect changes.
     */
    open fun isModified() = originalState != readState()

    /**
     * Resets the editor's state to the last saved state.
     *
     * Does nothing if and only if [isModified] return false.
     */
    open fun reset() = loadState(originalState)

    /**
     * Validates the state of the editor, i.e. of [readState], and indicates whether and why it is invalid.
     *
     * @return `null` if the state is valid, or a string explaining why the state is invalid
     */
    open fun doValidate(): String? = readState().doValidate()


    /**
     * Adds a listener that is invoked whenever the editor's state is modified.
     *
     * The editor's state is defined by [readState], such that this method is triggered when a change occurs such that
     * [readState] returns a value that is non-equal to the value before the change was made.
     *
     * @param listener the listener that is invoked
     */
    abstract fun addChangeListener(listener: () -> Unit)
}
