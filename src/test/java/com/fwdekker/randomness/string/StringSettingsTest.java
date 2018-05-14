package com.fwdekker.randomness.string;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link StringSettings}.
 */
final class StringSettingsTest {
    private StringSettings stringSettings;


    @BeforeEach
    void beforeEach() {
        stringSettings = new StringSettings();
    }


    @Test
    void testGetComponentName() {
        assertThat(stringSettings.getComponentName()).isEqualTo("StringSettings");
    }

    @Test
    void testGetLoadState() {
        final HashSet<Alphabet> alphabets = new HashSet<>(Collections.emptyList());

        stringSettings.setMinLength(730);
        stringSettings.setMaxLength(891);
        stringSettings.setEnclosure("Qh7");
        stringSettings.setAlphabets(alphabets);

        final StringSettings newStringSettings = new StringSettings();
        newStringSettings.loadState(stringSettings.getState());

        assertThat(newStringSettings.getMinLength()).isEqualTo(730);
        assertThat(newStringSettings.getMaxLength()).isEqualTo(891);
        assertThat(newStringSettings.getEnclosure()).isEqualTo("Qh7");
        assertThat(newStringSettings.getAlphabets()).isEqualTo(alphabets);
    }

    @Test
    void testGetSetMinLength() {
        stringSettings.setMinLength(173);

        assertThat(stringSettings.getMinLength()).isEqualTo(173);
    }

    @Test
    void testGetSetMaxLength() {
        stringSettings.setMaxLength(421);

        assertThat(stringSettings.getMaxLength()).isEqualTo(421);
    }

    @Test
    void testGetSetEnclosure() {
        stringSettings.setEnclosure("hWD");

        assertThat(stringSettings.getEnclosure()).isEqualTo("hWD");
    }

    @Test
    void testGetSetAlphabets() {
        final Set<Alphabet> alphabets
                = new HashSet<>(Arrays.asList(Alphabet.LOWERCASE, Alphabet.BRACKETS, Alphabet.MINUS));
        stringSettings.setAlphabets(alphabets);

        assertThat(stringSettings.getAlphabets()).isEqualTo(alphabets);
    }
}
