package com.fwdekker.randomness.word;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

    @Test
    public void testGetSetBundledDictionaries() {
        final Set<String> bundledDictionaries
                = new HashSet<>(Arrays.asList("6OE]SfZj6(", "HGeldsz2XM", "V6AhkeIKX6"));
        wordSettings.setBundledDictionaries(bundledDictionaries);

        assertThat(wordSettings.getBundledDictionaries()).isEqualTo(bundledDictionaries);
    }

    @Test
    public void testGetSetUserDictionaries() {
        final Set<String> userDictionaries
                = new HashSet<>(Arrays.asList(")asQAYwW[u", "Bz>GSRlNA1", "Cjsg{Olylo"));
        wordSettings.setUserDictionaries(userDictionaries);

        assertThat(wordSettings.getUserDictionaries()).isEqualTo(userDictionaries);
    }

    @Test
    public void testGetSetActiveBundledDictionaries() {
        final Set<String> bundledDictionaries
                = new HashSet<>(Arrays.asList("6QeMvZ>uHQ", "Onb]HUugM1", "008xGJhIXE"));
        wordSettings.setActiveBundledDictionaries(bundledDictionaries);

        assertThat(wordSettings.getActiveBundledDictionaries()).isEqualTo(bundledDictionaries);
    }

    @Test
    public void testGetSetActiveUserDictionaries() {
        final Set<String> userDictionaries
                = new HashSet<>(Arrays.asList("ukeB8}RLbm", "JRcuz7sm4(", "{QZGJQli36"));
        wordSettings.setActiveUserDictionaries(userDictionaries);

        assertThat(wordSettings.getActiveUserDictionaries()).isEqualTo(userDictionaries);
    }
}
