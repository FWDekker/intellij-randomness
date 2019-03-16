package com.fwdekker.randomness

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo


/**
 * Superclass for settings dialogs.
 *
 * Subclasses **MUST** call [.init] and [.loadSettings] in their constructor.
 *
 * @param settings the settings to manage
 * @param <S> the type of settings managed by the subclass
 */
abstract class SettingsDialog<S : Settings<*>>(private val settings: S) : DialogWrapper(null), SettingsManager<S> {
    override fun loadSettings() = loadSettings(settings)

    override fun saveSettings() = saveSettings(settings)


    override fun getDimensionServiceKey(): String = javaClass.simpleName

    override fun doOKAction() {
        processDoNotAskOnOk(DialogWrapper.OK_EXIT_CODE)

        if (okAction.isEnabled) {
            saveSettings()
            close(DialogWrapper.OK_EXIT_CODE)
        }
    }

    /**
     * Validates all input fields.
     *
     * @return `null` if the input is valid, or a [ValidationInfo] object explaining why the input is invalid
     */
    abstract override fun doValidate(): ValidationInfo?
}
