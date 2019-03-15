package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.Arrays
import java.util.HashSet


/**
 * Contains settings for generating random strings.
 */
@State(name = "StringSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
class StringSettings : Settings<StringSettings> {
    companion object {
        private const val DEFAULT_MIN_LENGTH = 3
        private const val DEFAULT_MAX_LENGTH = 8


        /**
         * The singleton `StringSettings` instance.
         */
        val default: StringSettings
            get() = ServiceManager.getService(StringSettings::class.java)
    }


    /**
     * The minimum length of a generated string, inclusive.
     */
    var minLength = DEFAULT_MIN_LENGTH
    /**
     * The maximum length of a generated string, inclusive.
     */
    var maxLength = DEFAULT_MAX_LENGTH
    /**
     * The string that encloses the generated string on both sides.
     */
    var enclosure = "\""
    /**
     * The capitalization mode of the generated string.
     */
    var capitalization = CapitalizationMode.UPPER
    /**
     * The alphabet to be used for generating strings.
     */
    var alphabets: Set<Alphabet> = HashSet(Arrays.asList(Alphabet.ALPHABET, Alphabet.DIGITS))


    override fun getState() = this

    override fun loadState(state: StringSettings) = XmlSerializerUtil.copyBean(state, this)
}
