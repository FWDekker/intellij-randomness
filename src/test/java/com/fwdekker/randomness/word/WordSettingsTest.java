package com.fwdekker.randomness.word;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link WordSettings}.
 */
final class WordSettingsTest {
    private static final DictionaryFileHelper FILE_HELPER = new DictionaryFileHelper();

    private WordSettings wordSettings;


    @AfterAll
    static void afterAll() {
        FILE_HELPER.cleanUpDictionaries();
    }

    @BeforeEach
    void beforeEach() {
        wordSettings = new WordSettings();
    }


    @Test
    void testGetLoadState() {
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
    void testGetSetMinLength() {
        wordSettings.setMinLength(905);

        assertThat(wordSettings.getMinLength()).isEqualTo(905);
    }

    @Test
    void testGetSetMaxLength() {
        wordSettings.setMaxLength(756);

        assertThat(wordSettings.getMaxLength()).isEqualTo(756);
    }

    @Test
    void testGetSetEnclosure() {
        wordSettings.setEnclosure("IERMV6Q5Qx");

        assertThat(wordSettings.getEnclosure()).isEqualTo("IERMV6Q5Qx");
    }

    @Test
    void testGetSetBundledDictionaries() {
        final Set<String> bundledDictionaries = new HashSet<>(Arrays.asList("6OE]SfZj6(", "HGeldsz2XM", "V6AhkeIKX6"));
        wordSettings.setBundledDictionaryFiles(bundledDictionaries);

        assertThat(wordSettings.getBundledDictionaryFiles()).isEqualTo(bundledDictionaries);
    }

    @Test
    void testGetSetUserDictionaries() {
        final Set<String> userDictionaries = new HashSet<>(Arrays.asList(")asQAYwW[u", "Bz>GSRlNA1", "Cjsg{Olylo"));
        wordSettings.setUserDictionaryFiles(userDictionaries);

        assertThat(wordSettings.getUserDictionaryFiles()).isEqualTo(userDictionaries);
    }

    @Test
    void testGetSetActiveBundledDictionaries() {
        final Set<String> bundledDictionaries = new HashSet<>(Arrays.asList("6QeMvZ>uHQ", "Onb]HUugM1", "008xGJhIXE"));
        wordSettings.setActiveBundledDictionaryFiles(bundledDictionaries);

        assertThat(wordSettings.getActiveBundledDictionaryFiles()).isEqualTo(bundledDictionaries);
    }

    @Test
    void testGetSetActiveUserDictionaries() {
        final Set<String> userDictionaries = new HashSet<>(Arrays.asList("ukeB8}RLbm", "JRcuz7sm4(", "{QZGJQli36"));
        wordSettings.setActiveUserDictionaryFiles(userDictionaries);

        assertThat(wordSettings.getActiveUserDictionaryFiles()).isEqualTo(userDictionaries);
    }
}
