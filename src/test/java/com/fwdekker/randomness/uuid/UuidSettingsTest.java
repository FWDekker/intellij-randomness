package com.fwdekker.randomness.uuid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link UuidSettings}.
 */
final class UuidSettingsTest {
    private UuidSettings uuidSettings;


    @BeforeEach
    void beforeEach() {
        uuidSettings = new UuidSettings();
    }


    @Test
    void testGetLoadState() {
        uuidSettings.setEnclosure("nvpB");

        final UuidSettings newUuidSettings = new UuidSettings();
        newUuidSettings.loadState(uuidSettings.getState());

        assertThat(newUuidSettings.getEnclosure()).isEqualTo("nvpB");
    }

    @Test
    void testGetSetMinLength() {
        uuidSettings.setEnclosure("RpTT5");

        assertThat(uuidSettings.getEnclosure()).isEqualTo("RpTT5");
    }
}
