package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random UUIDs.
 *
 * @property enclosure The string that encloses the generated UUID on both sides.
 *
 * @see UuidInsertAction
 * @see UuidSettingsAction
 * @see UuidSettingsDialog
 */
@State(name = "UuidSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class UuidSettings(var enclosure: String = DEFAULT_ENCLOSURE) : Settings<UuidSettings> {
    companion object {
        /**
         * The default value of the [enclosure][UuidSettings.enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""


        /**
         * The persistent `UuidSettings` instance.
         */
        val default: UuidSettings
            get() = ServiceManager.getService(UuidSettings::class.java)
    }


    /**
     * Returns `this`.
     *
     * @return `this`
     */
    override fun getState() = this

    /**
     * Copies the fields of [state] to `this`.
     *
     * @param state the state to load into `this`
     */
    override fun loadState(state: UuidSettings) = XmlSerializerUtil.copyBean(state, this)
}
