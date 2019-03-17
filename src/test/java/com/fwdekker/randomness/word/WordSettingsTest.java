package com.fwdekker.randomness.word;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
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


    @Test
    @Ignore // TODO Rewrite these tests
    void testValidateAllDictionariesSuccessEmpty() {
        wordSettings.setBundledDictionaryFiles(Collections.emptySet());
        wordSettings.setUserDictionaryFiles(Collections.emptySet());

//        assertThat(wordSettings.validateAllDictionaries()).isNull();
    }

    @Test
    @Ignore
    void testValidateAllDictionariesSuccessNonEmpty() {
        final File userDictionary = FILE_HELPER.createDictionaryFile("Reflet\nHerniate\nBuz");

        wordSettings.setBundledDictionaryFiles(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setUserDictionaryFiles(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

//        assertThat(wordSettings.validateAllDictionaries()).isNull();
    }

    @Test
    @Ignore
    void testValidateAllDictionaryInvalidBundled() {
        final File userDictionary = FILE_HELPER.createDictionaryFile("Inblow\nImmunes\nEnteroid");

        wordSettings.setBundledDictionaryFiles(new HashSet<>(Arrays.asList("dictionaries/empty.dic")));
        wordSettings.setUserDictionaryFiles(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

//        final ValidationInfo validationInfo = wordSettings.validateAllDictionaries();

//        assertThat(validationInfo).isNotNull();
//        assertThat(validationInfo.message).isEqualTo("The dictionary resource for empty.dic is empty.");
//        assertThat(validationInfo.component).isNull();
    }

    @Test
    @Ignore
    void testValidateAllDictionaryInvalidUser() {
        final File userDictionary = FILE_HELPER.createDictionaryFile("");
        final String userDictionaryName = userDictionary.getName();

        wordSettings.setBundledDictionaryFiles(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setUserDictionaryFiles(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

//        final ValidationInfo validationInfo = wordSettings.validateAllDictionaries();

//        assertThat(validationInfo).isNotNull();
//        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + userDictionaryName + " is empty.");
//        assertThat(validationInfo.component).isNull();
    }

    @Test
    @Ignore
    void testValidateActiveDictionariesSuccessEmpty() {
        wordSettings.setActiveBundledDictionaryFiles(Collections.emptySet());
        wordSettings.setActiveUserDictionaryFiles(Collections.emptySet());

//        assertThat(wordSettings.validateActiveDictionaries()).isNull();
    }

    @Test
    @Ignore
    void testValidateActiveDictionariesSuccessNonEmpty() {
        final File userDictionary = FILE_HELPER.createDictionaryFile("Dicranum\nJiffy\nChatties");

        wordSettings.setActiveBundledDictionaryFiles(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setActiveUserDictionaryFiles(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

//        assertThat(wordSettings.validateActiveDictionaries()).isNull();
    }

    @Test
    @Ignore
    void testValidateActiveDictionaryInvalidBundled() {
        final File userDictionary = FILE_HELPER.createDictionaryFile("Fastest\nWows\nBrimmers");

        wordSettings.setActiveBundledDictionaryFiles(new HashSet<>(Arrays.asList("dictionaries/empty.dic")));
        wordSettings.setActiveUserDictionaryFiles(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

//        final ValidationInfo validationInfo = wordSettings.validateActiveDictionaries();

//        assertThat(validationInfo).isNotNull();
//        assertThat(validationInfo.message).isEqualTo("The dictionary resource for empty.dic is empty.");
//        assertThat(validationInfo.component).isNull();
    }

    @Test
    @Ignore
    void testValidateActiveDictionaryInvalidUser() {
        final File userDictionary = FILE_HELPER.createDictionaryFile("");
        final String userDictionaryName = userDictionary.getName();

        wordSettings.setActiveBundledDictionaryFiles(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setActiveUserDictionaryFiles(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

//        final ValidationInfo validationInfo = wordSettings.validateActiveDictionaries();

//        assertThat(validationInfo).isNotNull();
//        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + userDictionaryName + " is empty.");
//        assertThat(validationInfo.component).isNull();
    }


    @Test
    @Ignore
    void testGetValidAllDictionariesEmpty() {
        wordSettings.setBundledDictionaryFiles(Collections.emptySet());
        wordSettings.setUserDictionaryFiles(Collections.emptySet());

//        assertThat(wordSettings.getValidAllDictionaries()).isEmpty();
    }

    @Test
    @Ignore
    void testGetValidAllDictionariesFilterBoth() {
        final File validUserDictionary = FILE_HELPER.createDictionaryFile("Resilium\nAncerata\nBylander");
        final File invalidUserDictionary = FILE_HELPER.createDictionaryFile("");

        wordSettings.setBundledDictionaryFiles(new HashSet<>(Arrays.asList(
            "dictionaries/simple.dic",
            "dictionaries/empty.dic"
        )));
        wordSettings.setUserDictionaryFiles(new HashSet<>(Arrays.asList(
            validUserDictionary.getAbsolutePath(),
            invalidUserDictionary.getAbsolutePath()
        )));

//        final List<Dictionary> dictionaries = wordSettings.getValidAllDictionaries();

//        assertThat(dictionaries).containsExactlyInAnyOrder(
//            Dictionary.UserDictionary.Companion.get(validUserDictionary.getAbsolutePath(), true),
//            Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", true)
//        );
    }

    @Test
    @Ignore
    void testGetValidActiveDictionariesEmpty() {
        wordSettings.setActiveBundledDictionaryFiles(Collections.emptySet());
        wordSettings.setActiveUserDictionaryFiles(Collections.emptySet());

//        assertThat(wordSettings.getValidActiveDictionaries()).isEmpty();
    }

    @Test
    @Ignore
    void testGetValidActiveDictionariesFilterBoth() {
        final File validUserDictionary = FILE_HELPER.createDictionaryFile("Resilium\nAncerata\nBylander");
        final File invalidUserDictionary = FILE_HELPER.createDictionaryFile("");

        wordSettings.setActiveBundledDictionaryFiles(new HashSet<>(Arrays.asList(
            "dictionaries/simple.dic",
            "dictionaries/empty.dic"
        )));
        wordSettings.setActiveUserDictionaryFiles(new HashSet<>(Arrays.asList(
            validUserDictionary.getAbsolutePath(),
            invalidUserDictionary.getAbsolutePath()
        )));

//        final List<Dictionary> dictionaries = wordSettings.getValidActiveDictionaries();

//        assertThat(dictionaries).containsExactlyInAnyOrder(
//            Dictionary.UserDictionary.Companion.get(validUserDictionary.getAbsolutePath(), true),
//            Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", true)
//        );
    }
}
