package com.fwdekker.randomness.array

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
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 *
 * @see ArraySettingsAction
 * @see ArraySettingsConfigurable
 */
@State(name = "ArraySettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class ArraySettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<ArrayScheme> = DEFAULT_SCHEMES,
    override var currentSchemeName: String = DEFAULT_CURRENT_SCHEME_NAME
) : Settings<ArraySettings, ArrayScheme> {
    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: ArraySettings) = XmlSerializerUtil.copyBean(state, this)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES: MutableList<ArrayScheme>
            get() = mutableListOf(ArrayScheme())

        /**
         * The default value of the [currentSchemeName][currentSchemeName] field.
         */
        const val DEFAULT_CURRENT_SCHEME_NAME = DEFAULT_NAME

        /**
         * The persistent `ArraySettings` instance.
         */
        val default: ArraySettings
            get() = service()
    }
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
 * @see ArraySettings
 * @see com.fwdekker.randomness.DataInsertArrayAction
 */
data class ArrayScheme(
    override var myName: String = DEFAULT_NAME,
    var count: Int = DEFAULT_COUNT,
    var brackets: String = DEFAULT_BRACKETS,
    var separator: String = DEFAULT_SEPARATOR,
    var isSpaceAfterSeparator: Boolean = DEFAULT_SPACE_AFTER_SEPARATOR
) : Scheme<ArrayScheme> {
    override fun copyFrom(other: ArrayScheme) = XmlSerializerUtil.copyBean(other, this)

    override fun copyAs(name: String) = this.copy(myName = name)


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


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [count][count] field.
         */
        const val DEFAULT_COUNT = 5

        /**
         * The default value of the [brackets][brackets] field.
         */
        const val DEFAULT_BRACKETS = "[]"

        /**
         * The default value of the [separator][separator] field.
         */
        const val DEFAULT_SEPARATOR = ","

        /**
         * The default value of the [isSpaceAfterSeparator][isSpaceAfterSeparator] field.
         */
        const val DEFAULT_SPACE_AFTER_SEPARATOR = true
    }
}


/**
 * The configurable for array settings.
 *
 * @see ArraySettingsAction
 */
class ArraySettingsConfigurable(override val component: ArraySettingsComponent = ArraySettingsComponent()) :
    SettingsConfigurable<ArraySettings, ArrayScheme>() {
    override fun getDisplayName() = "Arrays"
}
