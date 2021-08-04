package com.fwdekker.randomness.array

import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @see ArraySettingsAction
 * @see ArraySettingsConfigurable
 */
@State(
    name = "com.fwdekker.randomness.array.ArraySettings",
    storages = [Storage("\$APP_CONFIG\$/randomness.xml")]
)
data class ArraySettings(
    var count: Int = DEFAULT_COUNT,
    var brackets: String = DEFAULT_BRACKETS,
    var separator: String = DEFAULT_SEPARATOR,
    var isSpaceAfterSeparator: Boolean = DEFAULT_SPACE_AFTER_SEPARATOR
) : Settings<ArraySettings> {
    override fun deepCopy() = ArraySettings(count, brackets, separator, isSpaceAfterSeparator)

    override fun getState() = this

    override fun loadState(state: ArraySettings) = XmlSerializerUtil.copyBean(state, this)


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

        /**
         * The persistent `ArraySettings` instance.
         */
        val default: ArraySettings
            get() = service()
    }
}


/**
 * The configurable for array settings.
 *
 * @see ArraySettingsAction
 */
class ArraySettingsConfigurable(override val component: ArraySettingsComponent = ArraySettingsComponent()) :
    SettingsConfigurable<ArraySettings>() {
    override fun getDisplayName() = "Arrays"
}
