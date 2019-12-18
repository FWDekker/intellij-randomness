package com.fwdekker.randomness

import com.intellij.application.options.schemes.AbstractSchemeActions
import com.intellij.application.options.schemes.SchemesModel
import com.intellij.application.options.schemes.SimpleSchemesPanel
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.JPanel


/**
 * A component that allows the user to edit settings and its corresponding schemes.
 *
 * Subclasses **MUST** call `loadSettings` in their constructor.
 *
 * @param S the type of settings to manage
 * @param T the type of scheme to manage
 * @property settings the settings to manage
 */
abstract class SettingsComponent<S : Settings<S, T>, T : Scheme<T>>(private val settings: S) : SettingsManager<S, T> {
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
    abstract val schemesPanel: SchemesPanel<S, T>


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
 * @param S the type of settings to manage
 * @param T the type of scheme to manage
 * @property settings the settings model that backs this panel; changed made through this panel are reflected in the
 * given settings instance
 * @property defaultName the name of the default scheme instance
 */
abstract class SchemesPanel<S : Settings<S, T>, T : Scheme<T>>(
    private val settings: S,
    private val defaultName: String
) : SimpleSchemesPanel<T>(), SchemesModel<T> {
    private val listeners = mutableListOf<Listener<T>>()

    /**
     * The type of scheme being managed.
     */
    abstract val type: Class<T>

    /**
     * Returns a new scheme instance that is equivalent to the default scheme instance.
     *
     * This method **must** return a new instance every time it is called.
     *
     * @return a new scheme instance that is equivalent to the default scheme instance
     */
    abstract fun createDefaultInstance(): T


    /**
     * Registers a listener that will be informed whenever the scheme selection changed.
     *
     * @param listener the listener that listens to scheme-change events
     */
    fun addListener(listener: Listener<T>) = listeners.add(listener)

    /**
     * Forcefully updates the combo box so that its entries and the current selection reflect the [settings] instance.
     *
     * This panel instance will update the combo box by itself. Call this method if the [settings] instance has been
     * changed externally and these changes need to be reflected.
     */
    fun updateComboBoxList() =
        settings.currentScheme.also { currentScheme ->
            resetSchemes(settings.schemes)
            selectScheme(currentScheme)
        }


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
        require(differsFromDefault(scheme)) { "Cannot remove default scheme." }

        listeners.forEach { it.onCurrentSchemeWillChange(scheme) }

        if (scheme == settings.currentScheme)
            settings.currentSchemeName = defaultName // TODO Must this be done beforehand?

        settings.schemes.remove(scheme)

        if (settings.schemes.isEmpty()) {
            settings.schemes.add(createDefaultInstance())
            settings.currentSchemeName = defaultName
        }

        updateComboBoxList() // TODO Must this happen before updating listeners?

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

    override fun createSchemeActions() = object : AbstractSchemeActions<T>(this) {
        override fun onSchemeChanged(scheme: T?) {
            if (scheme == null)
                return

            listeners.forEach { it.onCurrentSchemeWillChange(settings.currentScheme) }
            settings.currentScheme = scheme
            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        override fun renameScheme(scheme: T, newName: String) {
            require(differsFromDefault(scheme)) { "Cannot rename default scheme." }

            listeners.forEach { it.onCurrentSchemeWillChange(scheme) }

            scheme.myName = newName
            settings.currentScheme = scheme
            updateComboBoxList()

            listeners.forEach { it.onCurrentSchemeHasChanged(scheme) }
        }

        override fun resetScheme(scheme: T) {
            require(scheme.name == defaultName) { "Cannot reset non-default scheme." }
            scheme.copyFrom(createDefaultInstance().copyAs(scheme.myName))
        }

        override fun duplicateScheme(scheme: T, newName: String) {
            listeners.forEach { it.onCurrentSchemeWillChange(scheme) }

            val copy = scheme.copyAs(newName)
            settings.schemes.add(copy)
            settings.currentScheme = copy

            listeners.forEach { it.onCurrentSchemeHasChanged(copy) }

            updateComboBoxList()
        }

        override fun getSchemeType() = type
    }


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
    override fun differsFromDefault(scheme: T) = scheme.name != defaultName

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
    override fun canDeleteScheme(scheme: T) = differsFromDefault(scheme)

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
    override fun canRenameScheme(scheme: T) = differsFromDefault(scheme)

    /**
     * Returns true if the given scheme can be reset.
     *
     * Because only the default scheme cannot be reset, this method is equivalent to [differsFromDefault].
     *
     * @param scheme the scheme to check for resetability
     * @return true if the given scheme can be reset
     */
    override fun canResetScheme(scheme: T) = !differsFromDefault(scheme)


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
