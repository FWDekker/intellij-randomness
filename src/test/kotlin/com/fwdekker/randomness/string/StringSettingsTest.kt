package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [StringSettings].
 */
object StringSettingsTest : Spek({
    lateinit var stringSettings: StringSettings


    beforeEachTest {
        stringSettings = StringSettings()
    }


    describe("state persistence") {
        it("copies state from another instance") {
            val alphabets = mutableSetOf<Alphabet>()

            stringSettings.minLength = 730
            stringSettings.maxLength = 891
            stringSettings.enclosure = "Qh7"
            stringSettings.alphabets = alphabets

            val newStringSettings = StringSettings()
            newStringSettings.loadState(stringSettings.state)

            assertThat(newStringSettings.minLength).isEqualTo(730)
            assertThat(newStringSettings.maxLength).isEqualTo(891)
            assertThat(newStringSettings.enclosure).isEqualTo("Qh7")
            assertThat(newStringSettings.alphabets).isEqualTo(alphabets)
        }
    }
})
