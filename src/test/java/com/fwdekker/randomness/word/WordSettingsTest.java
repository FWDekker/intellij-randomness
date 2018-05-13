package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link WordSettings}.
 */
public final class WordSettingsTest {
    private static final DictionaryFileHelper FILE_HELPER = new DictionaryFileHelper();

    private WordSettings wordSettings;


    @AfterClass
    public static void afterAll() {
        FILE_HELPER.cleanUpDictionaries();
    }

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
        final Set<String> bundledDictionaries = new HashSet<>(Arrays.asList("6OE]SfZj6(", "HGeldsz2XM", "V6AhkeIKX6"));
        wordSettings.setBundledDictionaries(bundledDictionaries);

        assertThat(wordSettings.getBundledDictionaries()).isEqualTo(bundledDictionaries);
    }

    @Test
    public void testGetSetUserDictionaries() {
        final Set<String> userDictionaries = new HashSet<>(Arrays.asList(")asQAYwW[u", "Bz>GSRlNA1", "Cjsg{Olylo"));
        wordSettings.setUserDictionaries(userDictionaries);

        assertThat(wordSettings.getUserDictionaries()).isEqualTo(userDictionaries);
    }

    @Test
    public void testGetSetActiveBundledDictionaries() {
        final Set<String> bundledDictionaries = new HashSet<>(Arrays.asList("6QeMvZ>uHQ", "Onb]HUugM1", "008xGJhIXE"));
        wordSettings.setActiveBundledDictionaries(bundledDictionaries);

        assertThat(wordSettings.getActiveBundledDictionaries()).isEqualTo(bundledDictionaries);
    }

    @Test
    public void testGetSetActiveUserDictionaries() {
        final Set<String> userDictionaries = new HashSet<>(Arrays.asList("ukeB8}RLbm", "JRcuz7sm4(", "{QZGJQli36"));
        wordSettings.setActiveUserDictionaries(userDictionaries);

        assertThat(wordSettings.getActiveUserDictionaries()).isEqualTo(userDictionaries);
    }


    @Test
    public void testValidateAllDictionariesSuccessEmpty() {
        wordSettings.setBundledDictionaries(Collections.emptySet());
        wordSettings.setUserDictionaries(Collections.emptySet());

        assertThat(wordSettings.validateAllDictionaries()).isNull();
    }

    @Test
    public void testValidateAllDictionariesSuccessNonEmpty() {
        final File userDictionary = FILE_HELPER.setUpDictionary("Reflet\nHerniate\nBuz");

        wordSettings.setBundledDictionaries(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setUserDictionaries(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

        assertThat(wordSettings.validateAllDictionaries()).isNull();
    }

    @Test
    public void testValidateAllDictionaryInvalidBundled() {
        final File userDictionary = FILE_HELPER.setUpDictionary("Inblow\nImmunes\nEnteroid");

        wordSettings.setBundledDictionaries(new HashSet<>(Arrays.asList("dictionaries/empty.dic")));
        wordSettings.setUserDictionaries(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

        final ValidationInfo validationInfo = wordSettings.validateAllDictionaries();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary resource for empty.dic is empty.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    public void testValidateAllDictionaryInvalidUser() {
        final File userDictionary = FILE_HELPER.setUpDictionary("");
        final String userDictionaryName = userDictionary.getName();

        wordSettings.setBundledDictionaries(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setUserDictionaries(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

        final ValidationInfo validationInfo = wordSettings.validateAllDictionaries();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + userDictionaryName + " is empty.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    public void testValidateActiveDictionariesSuccessEmpty() {
        wordSettings.setActiveBundledDictionaries(Collections.emptySet());
        wordSettings.setActiveUserDictionaries(Collections.emptySet());

        assertThat(wordSettings.validateActiveDictionaries()).isNull();
    }

    @Test
    public void testValidateActiveDictionariesSuccessNonEmpty() {
        final File userDictionary = FILE_HELPER.setUpDictionary("Dicranum\nJiffy\nChatties");

        wordSettings.setActiveBundledDictionaries(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setActiveUserDictionaries(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

        assertThat(wordSettings.validateActiveDictionaries()).isNull();
    }

    @Test
    public void testValidateActiveDictionaryInvalidBundled() {
        final File userDictionary = FILE_HELPER.setUpDictionary("Fastest\nWows\nBrimmers");

        wordSettings.setActiveBundledDictionaries(new HashSet<>(Arrays.asList("dictionaries/empty.dic")));
        wordSettings.setActiveUserDictionaries(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

        final ValidationInfo validationInfo = wordSettings.validateActiveDictionaries();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary resource for empty.dic is empty.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    public void testValidateActiveDictionaryInvalidUser() {
        final File userDictionary = FILE_HELPER.setUpDictionary("");
        final String userDictionaryName = userDictionary.getName();

        wordSettings.setActiveBundledDictionaries(new HashSet<>(Arrays.asList("dictionaries/simple.dic")));
        wordSettings.setActiveUserDictionaries(new HashSet<>(Arrays.asList(userDictionary.getAbsolutePath())));

        final ValidationInfo validationInfo = wordSettings.validateActiveDictionaries();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + userDictionaryName + " is empty.");
        assertThat(validationInfo.component).isNull();
    }


    @Test
    public void testGetValidAllDictionariesEmpty() {
        wordSettings.setBundledDictionaries(Collections.emptySet());
        wordSettings.setUserDictionaries(Collections.emptySet());

        assertThat(wordSettings.getValidAllDictionaries()).isEmpty();
    }

    @Test
    public void testGetValidAllDictionariesFilterBoth() {
        final File validUserDictionary = FILE_HELPER.setUpDictionary("Resilium\nAncerata\nBylander");
        final File invalidUserDictionary = FILE_HELPER.setUpDictionary("");

        wordSettings.setBundledDictionaries(new HashSet<>(Arrays.asList(
                "dictionaries/simple.dic",
                "dictionaries/empty.dic"
        )));
        wordSettings.setUserDictionaries(new HashSet<>(Arrays.asList(
                validUserDictionary.getAbsolutePath(),
                invalidUserDictionary.getAbsolutePath()
        )));

        final List<Dictionary> dictionaries = wordSettings.getValidAllDictionaries();

        assertThat(dictionaries).containsExactlyInAnyOrder(
                Dictionary.UserDictionary.get(validUserDictionary.getAbsolutePath()),
                Dictionary.BundledDictionary.get("dictionaries/simple.dic")
        );
    }

    @Test
    public void testGetValidActiveDictionariesEmpty() {
        wordSettings.setActiveBundledDictionaries(Collections.emptySet());
        wordSettings.setActiveUserDictionaries(Collections.emptySet());

        assertThat(wordSettings.getValidActiveDictionaries()).isEmpty();
    }

    @Test
    public void testGetValidActiveDictionariesFilterBoth() {
        final File validUserDictionary = FILE_HELPER.setUpDictionary("Resilium\nAncerata\nBylander");
        final File invalidUserDictionary = FILE_HELPER.setUpDictionary("");

        wordSettings.setActiveBundledDictionaries(new HashSet<>(Arrays.asList(
                "dictionaries/simple.dic",
                "dictionaries/empty.dic"
        )));
        wordSettings.setActiveUserDictionaries(new HashSet<>(Arrays.asList(
                validUserDictionary.getAbsolutePath(),
                invalidUserDictionary.getAbsolutePath()
        )));

        final List<Dictionary> dictionaries = wordSettings.getValidActiveDictionaries();

        assertThat(dictionaries).containsExactlyInAnyOrder(
                Dictionary.UserDictionary.get(validUserDictionary.getAbsolutePath()),
                Dictionary.BundledDictionary.get("dictionaries/simple.dic")
        );
    }
}
