package com.fwdekker.randomness.array

import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.Scheme
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Transient


@State(name = "ArraySettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
// TODO Do not store the default schemes in the XML because the whole point is that the user can always fall back on the
// defaults even when the configuration is corrupted.
data class ArraySettings(
    @MapAnnotation(sortBeforeSave = false)
    var schemes: MutableList<ArrayScheme> = DEFAULT_SCHEMES.toMutableList(),
    var currentSchemeName: String = DEFAULT_CURRENT_SCHEME_NAME
) : Settings<ArraySettings> {
    companion object {
        /**
         * The default value of the [schemes][ArraySettings.schemes] field.
         */
        val DEFAULT_SCHEMES = listOf(ArrayScheme())
        /**
         * The default value of the [currentSchemeName][ArraySettings.currentSchemeName] field.
         */
        const val DEFAULT_CURRENT_SCHEME_NAME = ArrayScheme.DEFAULT_NAME

        /**
         * The persistent `ArraySettings` instance.
         */
        val default: ArraySettings
            get() = ServiceManager.getService(ArraySettings::class.java)
    }


    var currentScheme: ArrayScheme
        @Transient
        get() = schemes.first { it.name == currentSchemeName }
        set(value) {
            currentSchemeName = value.name
        }


    override fun copyState() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: ArraySettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * Contains settings for generating arrays of other types of random values.
 *
 * @property myName The name of the scheme.
 * @property count The number of elements to generate.
 * @property brackets The brackets to surround arrays with.
 * @property separator The string to place between generated elements.
 * @property isSpaceAfterSeparator True iff a space should be placed after each separator.
 *
 * @see com.fwdekker.randomness.DataInsertArrayAction
 */
data class ArrayScheme(
    var myName: String = DEFAULT_NAME,
    var count: Int = DEFAULT_COUNT,
    var brackets: String = DEFAULT_BRACKETS,
    var separator: String = DEFAULT_SEPARATOR,
    var isSpaceAfterSeparator: Boolean = DEFAULT_SPACE_AFTER_SEPARATOR
) : Scheme {
    companion object {
        /**
         * The default value of the [myName][ArrayScheme.myName] field.
         */
        const val DEFAULT_NAME = "Default"
        /**
         * The default value of the [count][ArrayScheme.count] field.
         */
        const val DEFAULT_COUNT = 5
        /**
         * The default value of the [brackets][ArrayScheme.brackets] field.
         */
        const val DEFAULT_BRACKETS = "[]"
        /**
         * The default value of the [separator][ArrayScheme.separator] field.
         */
        const val DEFAULT_SEPARATOR = ","
        /**
         * The default value of the [isSpaceAfterSeparator][ArrayScheme.isSpaceAfterSeparator] field.
         */
        const val DEFAULT_SPACE_AFTER_SEPARATOR = true
    }


    /**
     * Same as [myName][ArrayScheme.myName].
     */
    override fun getName() = myName


    /**
     * Turns a collection of strings into a single string based on the fields of this `ArraySettings` object.
     *
     * @param strings the strings to arrayify
     * @return an array-like string representation of `strings`
     */
    fun arrayify(strings: Collection<String>) =
        strings.joinToString(
            separator = this.separator + if (isSpaceAfterSeparator && this.separator !== "\n") " " else "",
            prefix = brackets.getOrNull(0)?.toString() ?: "",
            postfix = brackets.getOrNull(1)?.toString() ?: ""
        )
}


/**
 * The configurable for array settings.
 *
 * @see ArraySettingsAction
 */
class ArraySettingsConfigurable(
    override val component: ArraySettingsComponent = ArraySettingsComponent()
) : SettingsConfigurable<ArraySettings>() {
    override fun getDisplayName() = "Arrays"
}
