package com.fwdekker.randomness.string;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link StringSettings}.
 */
public final class StringSettingsTest {
    private StringSettings stringSettings;


    @Before
    public void beforeEach() {
        stringSettings = new StringSettings();
    }


    @Test
    public void testGetComponentName() {
        assertThat(stringSettings.getComponentName()).isEqualTo("StringSettings");
    }

    @Test
    public void testGetLoadState() {
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
    public void testGetSetMinLength() {
        stringSettings.setMinLength(173);

        assertThat(stringSettings.getMinLength()).isEqualTo(173);
    }

    @Test
    public void testGetSetMaxLength() {
        stringSettings.setMaxLength(421);

        assertThat(stringSettings.getMaxLength()).isEqualTo(421);
    }

    @Test
    public void testGetSetEnclosure() {
        stringSettings.setEnclosure("hWD");

        assertThat(stringSettings.getEnclosure()).isEqualTo("hWD");
    }

    @Test
    public void testGetSetAlphabet() {
        final HashSet<Alphabet> alphabets
                = new HashSet<>(Arrays.asList(Alphabet.LOWERCASE, Alphabet.BRACKETS, Alphabet.MINUS));
        stringSettings.setAlphabets(alphabets);

        assertThat(stringSettings.getAlphabets()).isEqualTo(alphabets);
    }
}
