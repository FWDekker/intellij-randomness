package com.fwdekker.randomness.word

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [WordSettings].
 */
object WordSettingsTest : Spek({
    lateinit var wordSettings: WordSettings


    beforeEachTest {
        wordSettings = WordSettings()
    }


    describe("state management") {
        it("creates an independent copy") {
            val copy = wordSettings.copyState()
            wordSettings.minLength = 156
            copy.minLength = 37

            assertThat(wordSettings.minLength).isEqualTo(156)
        }

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
