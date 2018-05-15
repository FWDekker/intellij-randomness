package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link Dictionary.UserDictionary}.
 */
final class UserDictionaryTest {
    private static final DictionaryFileHelper FILE_HELPER = new DictionaryFileHelper();


    @AfterAll
    static void afterAll() {
        FILE_HELPER.cleanUpDictionaries();
    }


    @Test
    void testInitDoesNotExist() {
        assertThatThrownBy(() -> Dictionary.UserDictionary.get("invalid_file"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to read dictionary into memory.")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void testInitEmpty() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("");

        assertThatThrownBy(() -> Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dictionary must be non-empty.");
    }

    @Test
    void testInitTwiceSame() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("Fonded\nLustrum\nUpgale");

        final Dictionary dictionaryA = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());
        final Dictionary dictionaryB = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        assertThat(dictionaryA).isSameAs(dictionaryB);
    }

    @Test
    void testInitTwiceNoCacheEqualButNotSame() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("Dyers\nHexsub\nBookit");

        final Dictionary dictionaryA = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());
        final Dictionary dictionaryB = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath(), false);

        assertThat(dictionaryB).isEqualTo(dictionaryA);
        assertThat(dictionaryB).isNotSameAs(dictionaryA);
    }

    @Test
    void testInitNoCacheStoresAnyway() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("Pecking\nAdinole\nFlashpan");

        final Dictionary dictionaryA = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath(), false);
        final Dictionary dictionaryB = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        assertThat(dictionaryB).isSameAs(dictionaryA);
    }


    @Test
    void testValidateInstanceSuccess() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("Rhodinal\nScruff\nPibrochs");
        final Dictionary dictionary = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        final ValidationInfo validationInfo = dictionary.validate();

        assertThat(validationInfo).isNull();
    }

    @Test
    void testValidateStaticSuccess() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("Bbls\nOverpray\nTreeward");

        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate(dictionaryFile.getAbsolutePath());

        assertThat(validationInfo).isNull();
    }

    @Test
    void testValidateStaticFileDoesNotExist() {
        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate("invalid_path");

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for invalid_path no longer exists.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    void testValidateStaticFileEmpty() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("");
        final String dictionaryName = dictionaryFile.getName();

        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate(dictionaryFile.getAbsolutePath());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + dictionaryName + " is empty.");
        assertThat(validationInfo.component).isNull();
    }


    @Test
    void testToString() {
        final File dictionaryFile = FILE_HELPER.setUpDictionary("Cholers\nJaloused\nStopback");
        final String dictionaryName = dictionaryFile.getName();

        final Dictionary dictionary = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        assertThat(dictionary.toString()).isEqualTo("[custom] " + dictionaryName);
    }
}
