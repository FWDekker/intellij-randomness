package com.fwdekker.randomness

import com.fwdekker.randomness.SchemesPanel.Listener
import com.intellij.application.options.schemes.AbstractSchemeActions
import com.intellij.application.options.schemes.SchemesModel
import com.intellij.application.options.schemes.SimpleSchemesPanel
import javax.swing.JPanel


/**
 * A component that allows the user to edit settings and its corresponding schemes.
 *
 * Subclasses **MUST** call `loadSettings` in their constructor.
 *
 * There are multiple settings [S] instances at any time. The `settings` given in the constructor is read when the
 * component is created and is written to when the user saves its changes. The currently-selected scheme is loaded into
 * the component's inputs. When the user selects a different scheme of which to change its values, the values in the
 * input fields are stored in a copy of `settings`. This way, the local changes are not lost when switching between
 * schemes, and the user can still revert all unsaved changes if desired.
 *
 * @param S the type of settings to manage
 * @param T the type of scheme to manage
 * @param settings the settings to manage
 */
abstract class SettingsComponent<S : Settings<S, T>, T : Scheme<T>>(private val settings: S) : SettingsManager<S> {
    /**
     * The panel containing the settings.
     */
    abstract val rootPane: JPanel?
    /**
     * The local copy that represents the currently-unsaved settings that are being edited by the user.
     */
    abstract val unsavedSettings: S
    /**
     * The panel containing the dropdown box of schemes and action buttons to rename, delete, etc. schemes.
     */
    abstract val schemesPanel: SchemesPanel<T>


    final override fun loadSettings() = loadSettings(settings)

    final override fun loadSettings(settings: S) {
        unsavedSettings.loadState(settings.deepCopy())
        loadScheme(unsavedSettings.currentScheme)
        schemesPanel.updateComboBoxList()
    }

    /**
     * Loads the given scheme into the component's state.
     *
     * @param scheme the scheme to load
     */
    abstract fun loadScheme(scheme: T)

    final override fun saveSettings() = saveSettings(settings)

    final override fun saveSettings(settings: S) {
        saveScheme(unsavedSettings.currentScheme)
        settings.loadState(unsavedSettings.deepCopy())
    }

    /**
     * Saves the component's state into the given scheme.
     *
     * @param scheme the scheme to save
     */
    abstract fun saveScheme(scheme: T)


    /**
     * Returns true if this component contains unsaved changes.
     *
     * @return true if this component contains unsaved changes
     */
    fun isModified() = settings.deepCopy().also { saveSettings(it) } != settings || isModified(settings)

    /**
     * Returns true if this component contains unsaved changes.
     *
     * Implement this method if the user should be able to reset their changes even if saving would not actually change
     * the settings object.
     *
     * @param settings the settings as they were loaded into the component
     * @return true if this component contains unsaved changes
     */
    open fun isModified(settings: S): Boolean = false

    /**
     * Discards unsaved changes.
     */
    fun reset() = loadSettings()


    /**
     * Validates all input fields.
     *
     * @return `null` if the input is valid, or a `ValidationInfo` object explaining why the input is invalid
     */
    abstract fun doValidate(): ValidationInfo?
}


/**
 * A panel to manage schemes with, providing a dropdown box to select schemes from and buttons to remove, rename, etc.
 * schemes.
 *
 * @param T the type of scheme to manage
 * @property settings the settings model that backs this panel; changes made through this panel are reflected in the
 * given settings instance
 */
abstract class SchemesPanel<T : Scheme<T>>(val settings: Settings<*, T>) : SimpleSchemesPanel<T>(), SchemesModel<T> {
    /**
     * Listeners for changes in the scheme selection.
     */
    private val listeners = mutableListOf<Listener<T>>()

    /**
     * Actions that can be performed with this panel.
     */
    val actions = createSchemeActions()


    /**
     * The type of scheme being managed.
     */
    abstract val type: Class<T>

    /**
     * Returns a list of the default instances.
     *
     * This method **must** return new instances every time it is called.
     *
     * @return a list of the default instances
     */
    abstract fun createDefaultInstances(): List<T>


    /**
     * Registers a listener that will be informed whenever the scheme selection changed.
     *
     * @param listener the listener that listens to scheme-change events
     */
    fun addListener(listener: Listener<T>) = listeners.add(listener)

    /**
     * Forcefully updates the combo box so that its entries and the current selection reflect the `settings` instance.
     *
     * This panel instance will update the combo box by itself. Call this method if the `settings` instance has been
     * changed externally and these changes need to be reflected.
     */
    fun updateComboBoxList() {
        settings.currentScheme.also { currentScheme ->
            resetSchemes(settings.schemes)
            selectScheme(currentScheme)
        }
    }


    /**
     * Returns the scheme with the given name.
     *
     * @param name the name of the scheme to return
     */
    fun getScheme(name: String) = settings.schemes.single { it.name == name }

    /**
     * Removes the given scheme from the settings.
     *
     * If the currently-selected scheme is removed, the current selection will change to the default scheme.
     * If there are somehow no more schemes, the default scheme is inserted and selected.
     *
     * @param scheme the scheme to remove
     * @throws IllegalArgumentException if the default scheme is being removed
     */
    override fun removeScheme(scheme: T) {
        require(scheme.name !in createDefaultInstances().map { it.name }) { "Cannot remove default scheme." }

        listeners.forEach { it.onCurrentSchemeWillChange(scheme) }

        settings.currentSchemeName = createDefaultInstances().map { it.name }[0]
        settings.schemes.remove(scheme)

        if (settings.schemes.isEmpty()) {
            settings.schemes.addAll(createDefaultInstances())
            settings.currentSchemeName = createDefaultInstances().map { it.name }[0]
        }

        updateComboBoxList()

        listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
    }

    /**
     * Returns true if a scheme with the given name is present in this panel.
     *
     * @param name the name to check for
     * @param projectScheme ignored
     * @return true if a scheme with the given name is present in this panel
     */
    override fun containsScheme(name: String, projectScheme: Boolean) = settings.schemes.any { it.name == name }

    /**
     * Returns an object with a number of actions that can be performed on this panel.
     *
     * @return an object with a number of actions that can be performed on this panel
     */
    final override fun createSchemeActions() = SchemeActions()


    /**
     * Returns this panel, because this panel also functions as the model.
     *
     * @return this panel, because this panel also functions as the model
     */
    override fun getModel() = this

    /**
     * Returns true if the given scheme is the default scheme.
     *
     * Since the default scheme is defined by its name, this method returns true if its name is not the default name.
     *
     * @param scheme the scheme to compare against the default scheme
     */
    override fun differsFromDefault(scheme: T) = scheme !in createDefaultInstances()

    /**
     * Returns false because project-specific schemes are not supported.
     *
     * @return false because project-specific schemes are not supported
     */
    override fun supportsProjectSchemes() = false

    /**
     * Returns true so that non-default schemes are highlighted.
     *
     * @return true so that non-default schemes are highlighted
     */
    override fun highlightNonDefaultSchemes() = true

    /**
     * Returns true so that non-removable schemes are highlighted.
     *
     * Since only the default scheme is non-removable, only the default scheme will be bold.
     *
     * @return true so that non-removable schemes are highlighted
     */
    override fun useBoldForNonRemovableSchemes() = true

    /**
     * Returns false because project-specific schemes are not supported.
     *
     * @param scheme ignored
     * @return false because project-specific schemes are not supported
     */
    override fun isProjectScheme(scheme: T) = false

    /**
     * Returns true if the given scheme can be deleted.
     *
     * Because only the default scheme cannot be deleted, this method is equivalent to [differsFromDefault].
     *
     * @param scheme the scheme to check for deletability
     * @return true if the given scheme can be deleted
     */
    override fun canDeleteScheme(scheme: T) = scheme.name !in createDefaultInstances().map { it.name }

    /**
     * Returns true because all schemes can be duplicated.
     *
     * @param scheme ignored
     * @return true because all schemes can be duplicated
     */
    override fun canDuplicateScheme(scheme: T) = true

    /**
     * Returns true if the given scheme can be renamed.
     *
     * Because only the default scheme cannot be renamed, this method is equivalent to [differsFromDefault].
     *
     * @param scheme the scheme to check for renamability
     * @return true if the given scheme can be renamed
     */
    override fun canRenameScheme(scheme: T) = scheme.name !in createDefaultInstances().map { it.name }

    /**
     * Returns true if the given scheme can be reset.
     *
     * Because only the default scheme cannot be reset, this method is equivalent to [differsFromDefault].
     *
     * @param scheme the scheme to check for resetability
     * @return true if the given scheme can be reset
     */
    override fun canResetScheme(scheme: T) = scheme.name in createDefaultInstances().map { it.name }


    /**
     * The actions that can be performed with this panel.
     */
    inner class SchemeActions : AbstractSchemeActions<T>(this) {
        /**
         * Called when the user changes the scheme using the combo box.
         *
         * @param scheme the scheme that has become the selected scheme
         */
        public override fun onSchemeChanged(scheme: T?) {
            if (scheme == null)
                return

            listeners.forEach { it.onCurrentSchemeWillChange(settings.currentScheme) }
            settings.currentScheme = scheme
            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        /**
         * Called when the user renames a scheme.
         *
         * @param scheme the scheme that is being renamed
         * @param newName the new name of the scheme
         */
        public override fun renameScheme(scheme: T, newName: String) {
            require(canRenameScheme(scheme)) { "Cannot rename given scheme." }

            listeners.forEach { it.onCurrentSchemeWillChange(scheme) }

            scheme.myName = newName
            settings.currentScheme = scheme
            updateComboBoxList()

            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        /**
         * Called when a user opts to reset a scheme to its defaults.
         *
         * @param scheme the scheme to reset
         */
        public override fun resetScheme(scheme: T) {
            require(canResetScheme(scheme)) { "Cannot reset given scheme." }

            scheme.copyFrom(createDefaultInstances().single { it.myName == scheme.myName })
            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        /**
         * Called when a user wants to duplicate a scheme.
         *
         * @param scheme the scheme to duplicate; it is assumed that this is the currently-selected scheme
         * @param newName the name to be applied to the duplicate
         */
        public override fun duplicateScheme(scheme: T, newName: String) {
            require(canDuplicateScheme(scheme)) { "Cannot duplicate given scheme." }

            listeners.forEach { it.onCurrentSchemeWillChange(scheme) }

            val copy = scheme.copyAs(newName)
            settings.schemes.add(copy)
            settings.currentScheme = copy
            updateComboBoxList()

            listeners.forEach { it.onCurrentSchemeHasChanged(copy) }
        }

        /**
         * Returns the type of scheme actions apply to.
         *
         * @return the type of scheme actions apply to
         */
        override fun getSchemeType() = type
    }


    /**
     * A listener that listens to events that occur to this panel.
     *
     * @param T the type of scheme about which events are generated
     */
    interface Listener<T : Scheme<T>> {
        /**
         * Invoked when the currently-selected scheme is about to change.
         *
         * @param scheme the scheme that is about to be replaced in favor of another scheme
         */
        fun onCurrentSchemeWillChange(scheme: T)

        /**
         * Invoked when the currently-selected scheme has just been changed.
         *
         * @param scheme the scheme that has become the currently-selected scheme
         */
        fun onCurrentSchemeHasChanged(scheme: T)
    }
}


/**
 * A [Listener] that takes events occurring in a [SchemesPanel] and handles them in a [SettingsComponent].
 *
 * Consider this listener the glue between the schemes panel (the model) and the settings component (the view).
 *
 * @param S the type of settings to manage
 * @param T the type of scheme to manage
 * @param component the settings component in which the changes should be reflected
 */
class SettingsComponentListener<S : Settings<S, T>, T : Scheme<T>>(private val component: SettingsComponent<S, T>) :
    Listener<T> {
    override fun onCurrentSchemeWillChange(scheme: T) = component.saveScheme(scheme)

    override fun onCurrentSchemeHasChanged(scheme: T) = component.loadScheme(scheme)
}
