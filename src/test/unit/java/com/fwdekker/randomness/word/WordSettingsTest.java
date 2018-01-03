package com.fwdekker.randomness.word;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link WordSettings}.
 */
public final class WordSettingsTest {
    private WordSettings wordSettings;


    @Before
    public void beforeEach() {
        wordSettings = new WordSettings();
    }


    @Test
    public void testGetComponentName() {
        assertThat(wordSettings.getComponentName()).isEqualTo("WordSettings");
    }

    @Test
    public void testGetLoadState() {
        wordSettings.setMinLength(502);
        wordSettings.setMaxLength(812);
        wordSettings.setEnclosure("QJ8S4UrFaa");

        final WordSettings newWordSettings = new WordSettings();
        newWordSettings.loadState(wordSettings.getState());

        assertThat(newWordSettings.getMinLength()).isEqualTo(502);
        assertThat(newWordSettings.getMaxLength()).isEqualTo(812);
        assertThat(newWordSettings.getEnclosure()).isEqualTo("QJ8S4UrFaa");
    }

    @Test
    public void testGetSetMinLength() {
        wordSettings.setMinLength(905);

        assertThat(wordSettings.getMinLength()).isEqualTo(905);
    }

    @Test
    public void testGetSetMaxLength() {
        wordSettings.setMaxLength(756);

        assertThat(wordSettings.getMaxLength()).isEqualTo(756);
    }

    @Test
    public void testGetSetEnclosure() {
        wordSettings.setEnclosure("IERMV6Q5Qx");

        assertThat(wordSettings.getEnclosure()).isEqualTo("IERMV6Q5Qx");
    }
}
