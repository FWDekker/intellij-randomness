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
        assertThatThrownBy(() -> Dictionary.UserDictionary.Companion.get("invalid_file", true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Failed to read dictionary into memory.")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void testInitEmpty() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("");

        assertThatThrownBy(() -> Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Dictionary must be non-empty.");
    }

    @Test
    void testInitTwiceSame() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("Fonded\nLustrum\nUpgale");

        final Dictionary dictionaryA = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);
        final Dictionary dictionaryB = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);

        assertThat(dictionaryA).isSameAs(dictionaryB);
    }

    @Test
    void testInitTwiceNoCacheEqualButNotSame() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("Dyers\nHexsub\nBookit");

        final Dictionary dictionaryA = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);
        final Dictionary dictionaryB = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), false);

        assertThat(dictionaryB).isEqualTo(dictionaryA);
        assertThat(dictionaryB).isNotSameAs(dictionaryA);
    }

    @Test
    void testInitNoCacheStoresAnyway() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("Pecking\nAdinole\nFlashpan");

        final Dictionary dictionaryA = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), false);
        final Dictionary dictionaryB = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);

        assertThat(dictionaryB).isSameAs(dictionaryA);
    }

    @Test
    void testInitAfterClearCache() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("Melamin\nPetrol\nBruckled");
        final Dictionary dictionaryBefore =
            Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);

        Dictionary.UserDictionary.Companion.clearCache();

        FILE_HELPER.writeToFile(dictionaryFile, "Rutch\nDespin\nSweltry");
        final Dictionary dictionaryAfter =
            Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);

        assertThat(dictionaryBefore.getWords()).containsExactlyInAnyOrder("Melamin", "Petrol", "Bruckled");
        assertThat(dictionaryAfter.getWords()).containsExactlyInAnyOrder("Rutch", "Despin", "Sweltry");
    }


    @Test
    void testValidateInstanceSuccess() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("Rhodinal\nScruff\nPibrochs");
        final Dictionary dictionary = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);

        final ValidationInfo validationInfo = dictionary.validate();

        assertThat(validationInfo).isNull();
    }

    @Test
    void testValidateStaticSuccess() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("Bbls\nOverpray\nTreeward");

        final ValidationInfo validationInfo =
            Dictionary.UserDictionary.Companion.validate(dictionaryFile.getAbsolutePath());

        assertThat(validationInfo).isNull();
    }

    @Test
    void testValidateStaticFileDoesNotExist() {
        final ValidationInfo validationInfo = Dictionary.UserDictionary.Companion.validate("invalid_path");

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for invalid_path no longer exists.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    void testValidateStaticFileEmpty() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("");
        final String dictionaryName = dictionaryFile.getName();

        final ValidationInfo validationInfo =
            Dictionary.UserDictionary.Companion.validate(dictionaryFile.getAbsolutePath());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + dictionaryName + " is empty.");
        assertThat(validationInfo.component).isNull();
    }


    @Test
    void testToString() {
        final File dictionaryFile = FILE_HELPER.createDictionaryFile("Cholers\nJaloused\nStopback");
        final String dictionaryName = dictionaryFile.getName();

        final Dictionary dictionary = Dictionary.UserDictionary.Companion.get(dictionaryFile.getAbsolutePath(), true);

        assertThat(dictionary.toString()).isEqualTo("[custom] " + dictionaryName);
    }
}
