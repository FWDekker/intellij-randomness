package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random UUIDs.
 */
@State(name = "UuidSettings", storages = [Storage("\$APP_CONFIG$/randomness.xml")])
class UuidSettings : Settings<UuidSettings> {
    companion object {
        /**
         * The singleton `UuidSettings` instance.
         */
        val instance: UuidSettings
            get() = ServiceManager.getService(UuidSettings::class.java)
    }


    /**
     * The string that encloses the generated UUID on both sides.
     */
    var enclosure = "\""


    override fun getState() = this

    override fun loadState(state: UuidSettings) = XmlSerializerUtil.copyBean(state, this)
}
