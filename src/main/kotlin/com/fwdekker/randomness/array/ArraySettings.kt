package com.fwdekker.randomness.array

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random arrays of other types of random values.
 */
@State(name = "ArraySettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
class ArraySettings : Settings<ArraySettings> {
    companion object {
        private const val DEFAULT_COUNT = 5
        private const val DEFAULT_BRACKETS = "[]"
        private const val DEFAULT_SEPARATOR = ","
        private const val DEFAULT_SPACE_AFTER_SEPARATOR = true


        /**
         * The singleton `ArraySettings` instance.
         */
        // TODO Rename to `default`
        val instance: ArraySettings
            get() = ServiceManager.getService(ArraySettings::class.java)
    }


    /**
     * The number of elements to generate.
     */
    var count = DEFAULT_COUNT
    /**
     * The brackets to surround arrays with.
     */
    var brackets = DEFAULT_BRACKETS
    /**
     * The separator to place between generated elements.
     */
    var separator = DEFAULT_SEPARATOR
    /**
     * `true` iff. a space should be placed after each separator.
     */
    var isSpaceAfterSeparator = DEFAULT_SPACE_AFTER_SEPARATOR


    override fun getState() = this

    override fun loadState(state: ArraySettings) = XmlSerializerUtil.copyBean(state, this)


    /**
     * Turns a collection of strings into a string representation as defined by this `ArraySettings`' settings.
     *
     * @param strings the strings to array-ify
     * @return a string representation as defined by this `ArraySettings`' settings
     */
    fun arrayify(strings: Collection<String>) =
        strings.joinToString(
            separator = this.separator + if (isSpaceAfterSeparator) " " else "",
            prefix = brackets.getOrNull(0)?.toString() ?: "",
            postfix = brackets.getOrNull(1)?.toString() ?: ""
        )
}
