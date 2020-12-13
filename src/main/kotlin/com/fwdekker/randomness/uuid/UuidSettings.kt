package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
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
 * The user-configurable collection of schemes applicable to generating UUIDs.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 *
 * @see UuidSettingsAction
 * @see UuidSettingsConfigurable
 */
@State(name = "UuidSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class UuidSettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<UuidScheme> = DEFAULT_SCHEMES,
    override var currentSchemeName: String = DEFAULT_CURRENT_SCHEME_NAME
) : Settings<UuidSettings, UuidScheme> {
    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: UuidSettings) = XmlSerializerUtil.copyBean(state, this)


    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES: MutableList<UuidScheme>
            get() = mutableListOf(UuidScheme())

        /**
         * The default value of the [currentSchemeName][currentSchemeName] field.
         */
        const val DEFAULT_CURRENT_SCHEME_NAME = DEFAULT_NAME

        /**
         * The persistent `UuidSettings` instance.
         */
        val default: UuidSettings
            get() = service()
    }
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
 * @see UuidSettings
 */
data class UuidScheme(
    override var myName: String = DEFAULT_NAME,
    var version: Int = DEFAULT_VERSION,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var addDashes: Boolean = DEFAULT_ADD_DASHES
) : Scheme<UuidScheme> {
    override fun copyFrom(other: UuidScheme) = XmlSerializerUtil.copyBean(other, this)

    override fun copyAs(name: String) = this.copy(myName = name)


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
