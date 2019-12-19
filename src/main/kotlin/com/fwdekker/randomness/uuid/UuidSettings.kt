package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation


/**
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 */
@State(name = "UuidSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class UuidSettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<UuidScheme> = DEFAULT_SCHEMES.toMutableList(),
    override var currentSchemeName: String = Scheme.DEFAULT_NAME
) : Settings<UuidSettings, UuidScheme> {
    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES
            get() = listOf(UuidScheme())

        /**
         * The persistent `UuidSettings` instance.
         */
        val default: UuidSettings
            get() = ServiceManager.getService(UuidSettings::class.java)
    }


    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: UuidSettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * Contains settings for generating random UUIDs.
 *
 * @property myName The name of the scheme.
 * @property version The version of UUIDs to generate.
 * @property enclosure The string that encloses the generated UUID on both sides.
 * @property capitalization The capitalization mode of the generated UUID.
 * @property addDashes True if and only if the UUID should have dashes in it.
 *
 * @see UuidInsertAction
 * @see UuidSettingsAction
 * @see UuidSettingsComponent
 */
data class UuidScheme(
    override var myName: String = Scheme.DEFAULT_NAME,
    var version: Int = DEFAULT_VERSION,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var addDashes: Boolean = DEFAULT_ADD_DASHES
) : Scheme<UuidScheme> {
    companion object {
        /**
         * The default value of the [version][version] field.
         */
        const val DEFAULT_VERSION = 4
        /**
         * The default value of the [enclosure][enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""
        /**
         * The default value of the [capitalization][capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.LOWER
        /**
         * The default value of the [addDashes][addDashes] field.
         */
        const val DEFAULT_ADD_DASHES = true
    }


    override fun copyFrom(other: UuidScheme) {
        this.myName = other.myName
        this.version = other.version
        this.enclosure = other.enclosure
        this.capitalization = other.capitalization
        this.addDashes = other.addDashes
    }

    override fun copyAs(name: String) = this.copy(myName = name)
}


/**
 * The configurable for UUID settings.
 *
 * @see UuidSettingsAction
 */
class UuidSettingsConfigurable(
    override val component: UuidSettingsComponent = UuidSettingsComponent()
) : SettingsConfigurable<UuidSettings, UuidScheme>() {
    override fun getDisplayName() = "UUIDs"
}
