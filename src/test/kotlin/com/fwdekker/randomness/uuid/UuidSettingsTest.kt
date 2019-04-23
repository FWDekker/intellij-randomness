package com.fwdekker.randomness.uuid

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Unit tests for [UuidSettings].
 */
class UuidSettingsTest {
    private lateinit var uuidSettings: UuidSettings


    @BeforeEach
    fun beforeEach() {
        uuidSettings = UuidSettings()
    }


    @Test
    fun testGetLoadState() {
        uuidSettings.enclosure = "nvpB"

        val newUuidSettings = UuidSettings()
        newUuidSettings.loadState(uuidSettings.state)

        assertThat(newUuidSettings.enclosure).isEqualTo("nvpB")
    }

    @Test
    fun testGetSetMinLength() {
        uuidSettings.enclosure = "RpTT5"

        assertThat(uuidSettings.enclosure).isEqualTo("RpTT5")
    }
}
