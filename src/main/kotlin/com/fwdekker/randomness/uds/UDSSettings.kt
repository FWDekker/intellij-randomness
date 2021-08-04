package com.fwdekker.randomness.uds

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlin.random.Random


/**
 * The user-configurable collection of schemes applicable to generating arbitrary strings using UDS syntax.
 *
 * @see UDSSettingsAction
 * @see UDSSettingsConfigurable
 */
@State(name = "UDSSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class UDSSettings(
    var descriptor: String = DEFAULT_DESCRIPTOR
) : Settings<UDSSettings> {
    override fun deepCopy() = UDSSettings(descriptor)

    override fun getState() = this

    override fun loadState(state: UDSSettings) = XmlSerializerUtil.copyBean(state, this)

    private val random: Random = Random.Default


    /**
     * Returns random UDS-based strings based on the descriptor.
     *
     * @param count the number of strings to generate
     * @return random UDS-based strings based on the descriptor
     */
    fun generateStrings(count: Int) =
        try {
            UDSParser.parse(descriptor).also { it.random = this.random }.generateStrings(count)
        } catch (e: UDSParseException) {
            throw DataGenerationException(e.message, e)
        }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [descriptor][descriptor] field.
         */
        const val DEFAULT_DESCRIPTOR = "Hello there, General Kenobi."

        /**
         * The persistent `UDSSettings` instance.
         */
        val default: UDSSettings
            get() = service()
    }
}


/**
 * The configurable for UDS settings.
 *
 * @see UDSSettingsAction
 */
class UDSSettingsConfigurable(
    override val component: UDSSettingsComponent = UDSSettingsComponent()
) : SettingsConfigurable<UDSSettings>() {
    override fun getDisplayName() = "UDSs"
}
