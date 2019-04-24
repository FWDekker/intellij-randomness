package com.fwdekker.randomness.word

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [WordSettings].
 */
object WordSettingsTest : Spek({
    lateinit var wordSettings: WordSettings


    beforeEachTest {
        wordSettings = WordSettings()
    }


    describe("state persistence") {
        it("copies state from another instance") {
            wordSettings.minLength = 502
            wordSettings.maxLength = 812
            wordSettings.enclosure = "QJ8S4UrFaa"

            val newWordSettings = WordSettings()
            newWordSettings.loadState(wordSettings.state)

            assertThat(newWordSettings.minLength).isEqualTo(502)
            assertThat(newWordSettings.maxLength).isEqualTo(812)
            assertThat(newWordSettings.enclosure).isEqualTo("QJ8S4UrFaa")
        }
    }
})
