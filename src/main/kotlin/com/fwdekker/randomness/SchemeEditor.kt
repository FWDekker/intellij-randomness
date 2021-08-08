package com.fwdekker.randomness

import javax.swing.JPanel


/**
 * A component that can be used to edit a [Scheme].
 *
 * The editor's scheme is initially read from [originalScheme]. Changes in the editor are written into [originalScheme]
 * when they are applied using [applyScheme].
 *
 * @param S the type of scheme that is edited; should be a self-reference
 * @property originalScheme the scheme object to write changes into
 */
abstract class SchemeEditor<S : Scheme>(val originalScheme: S) {
    /**
     * The root component of the editor.
     */
    abstract val rootComponent: JPanel


    /**
     * Loads the given scheme into the editor and into [originalScheme].
     *
     * @param scheme the scheme to load
     */
    open fun loadScheme(scheme: S = originalScheme) {
        originalScheme.copyFrom(scheme)
    }

    /**
     * Returns the editor's current scheme, including potentially unsaved changes.
     *
     * @return the editor's current scheme, including potentially unsaved changes
     */
    abstract fun readScheme(): S

    /**
     * Saves the editor's scheme into [originalScheme].
     *
     * Does nothing if and only if [isModified] returns false.
     */
    fun applyScheme() = originalScheme.copyFrom(readScheme())


    /**
     * Returns true if and only if the editor contains modifications relative to the last saved scheme.
     *
     * Override this method if the default equals method of [S] is not sufficient to detect changes.
     */
    open fun isModified() = originalScheme != readScheme()

    /**
     * Resets the editor's scheme to the last saved scheme.
     *
     * Does nothing if and only if [isModified] return false.
     */
    fun reset() = loadScheme(originalScheme)

    /**
     * Validates the scheme of the editor, i.e. of [readScheme], and indicates whether and why it is invalid.
     *
     * @return `null` if the scheme is valid, or a string explaining why the scheme is invalid
     */
    open fun doValidate(): String? = readScheme().doValidate()


    /**
     * Adds a listener that is invoked whenever the editor's scheme is modified.
     *
     * The editor's scheme is defined by [readScheme], such that this method is triggered when a change occurs such that
     * [readScheme] returns a value that is non-equal to the value before the change was made.
     *
     * @param listener the listener that is invoked
     */
    abstract fun addChangeListener(listener: () -> Unit)
}
