package com.fwdekker.randomness.word

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Unit tests for [WordSettings].
 */
class WordSettingsTest {
    companion object {
        private val FILE_HELPER = DictionaryFileHelper()


        @AfterAll
        @JvmStatic
        fun afterAll() {
            FILE_HELPER.cleanUpDictionaries()
        }
    }

    private lateinit var wordSettings: WordSettings


    @BeforeEach
    fun beforeEach() {
        wordSettings = WordSettings()
    }


    @Test
    fun testGetLoadState() {
        wordSettings.minLength = 502
        wordSettings.maxLength = 812
        wordSettings.enclosure = "QJ8S4UrFaa"

        val newWordSettings = WordSettings()
        newWordSettings.loadState(wordSettings.state)

        assertThat(newWordSettings.minLength).isEqualTo(502)
        assertThat(newWordSettings.maxLength).isEqualTo(812)
        assertThat(newWordSettings.enclosure).isEqualTo("QJ8S4UrFaa")
    }

    @Test
    fun testGetSetMinLength() {
        wordSettings.minLength = 905

        assertThat(wordSettings.minLength).isEqualTo(905)
    }

    @Test
    fun testGetSetMaxLength() {
        wordSettings.maxLength = 756

        assertThat(wordSettings.maxLength).isEqualTo(756)
    }

    @Test
    fun testGetSetEnclosure() {
        wordSettings.enclosure = "IERMV6Q5Qx"

        assertThat(wordSettings.enclosure).isEqualTo("IERMV6Q5Qx")
    }

    @Test
    fun testGetSetBundledDictionaries() {
        val bundledDictionaries = mutableSetOf("6OE]SfZj6(", "HGeldsz2XM", "V6AhkeIKX6")
        wordSettings.bundledDictionaryFiles = bundledDictionaries

        assertThat(wordSettings.bundledDictionaryFiles).isEqualTo(bundledDictionaries)
    }

    @Test
    fun testGetSetUserDictionaries() {
        val userDictionaries = mutableSetOf(")asQAYwW[u", "Bz>GSRlNA1", "Cjsg{Olylo")
        wordSettings.userDictionaryFiles = userDictionaries

        assertThat(wordSettings.userDictionaryFiles).isEqualTo(userDictionaries)
    }

    @Test
    fun testGetSetActiveBundledDictionaries() {
        val bundledDictionaries = mutableSetOf("6QeMvZ>uHQ", "Onb]HUugM1", "008xGJhIXE")
        wordSettings.activeBundledDictionaryFiles = bundledDictionaries

        assertThat(wordSettings.activeBundledDictionaryFiles).isEqualTo(bundledDictionaries)
    }

    @Test
    fun testGetSetActiveUserDictionaries() {
        val userDictionaries = mutableSetOf("ukeB8}RLbm", "JRcuz7sm4(", "{QZGJQli36")
        wordSettings.activeUserDictionaryFiles = userDictionaries

        assertThat(wordSettings.activeUserDictionaryFiles).isEqualTo(userDictionaries)
    }
}
