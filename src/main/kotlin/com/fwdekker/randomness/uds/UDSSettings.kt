package com.fwdekker.randomness.uds

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Scheme.Companion.DEFAULT_NAME
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation


/**
 * The user-configurable collection of schemes applicable to generating arbitrary strings using UDS syntax.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 *
 * @see UDSSettingsAction
 * @see UDSSettingsConfigurable
 */
@State(name = "UDSSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class UDSSettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<UDSScheme> = DEFAULT_SCHEMES,
    override var currentSchemeName: String = DEFAULT_CURRENT_SCHEME_NAME
) : Settings<UDSSettings, UDSScheme> {
    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: UDSSettings) = XmlSerializerUtil.copyBean(state, this)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES: MutableList<UDSScheme>
            get() = mutableListOf(UDSScheme())

        /**
         * The default value of the [currentSchemeName][currentSchemeName] field.
         */
        const val DEFAULT_CURRENT_SCHEME_NAME = DEFAULT_NAME

        /**
         * The persistent `UDSSettings` instance.
         */
        val default: UDSSettings
            get() = service()
    }
}


/**
 * Contains settings for generating random arbitrary strings using UDS syntax.
 *
 * @property myName The name of the scheme.
 * @property descriptor The UDS descriptor for this scheme.
 *
 * @see UDSInsertAction
 * @see UDSSettings
 */
data class UDSScheme(
    override var myName: String = DEFAULT_NAME,
    var descriptor: String = DEFAULT_DESCRIPTOR
) : Scheme<UDSScheme> {
    override fun copyFrom(other: UDSScheme) = XmlSerializerUtil.copyBean(other, this)

    override fun copyAs(name: String) = this.copy(myName = name)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [descriptor][descriptor] field.
         */
        const val DEFAULT_DESCRIPTOR = "Hello there, General Kenobi."
    }
}


/**
 * The configurable for UDS settings.
 *
 * @see UDSSettingsAction
 */
class UDSSettingsConfigurable(
    override val component: UDSSettingsComponent = UDSSettingsComponent()
) : SettingsConfigurable<UDSSettings, UDSScheme>() {
    override fun getDisplayName() = "UDSs"
}
