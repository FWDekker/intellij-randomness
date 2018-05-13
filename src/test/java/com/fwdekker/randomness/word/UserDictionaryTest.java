package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;


/**
 * Unit tests for {@link Dictionary.UserDictionary}.
 */
public final class UserDictionaryTest {
    @Test
    public void testInitDoesNotExist() {
        assertThatThrownBy(() -> Dictionary.UserDictionary.get("invalid_file"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to read dictionary into memory.")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void testInitEmpty() {
        final File dictionaryFile = setUpDictionary("");

        assertThatThrownBy(() -> Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dictionary must be non-empty.");
    }

    @Test
    public void testInitTwiceEquals() {
        final File dictionaryFile = setUpDictionary("Fonded\nLustrum\nUpgale");

        final Dictionary dictionaryA = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());
        final Dictionary dictionaryB = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        assertThat(dictionaryA).isEqualTo(dictionaryB);
    }

    @Test
    public void testInitListTwiceEquals() {
        final File dictionaryFile = setUpDictionary("Fomenter\nOutwits\nManqu");

        final List<Dictionary.UserDictionary> dictionaries = Dictionary.UserDictionary.get(Arrays.asList(
                dictionaryFile.getAbsolutePath(),
                dictionaryFile.getAbsolutePath()
        ));

        assertThat(dictionaries).hasSize(2);
        assertThat(dictionaries.get(0)).isEqualTo(dictionaries.get(1));
    }


    @Test
    public void testValidateInstanceSuccess() {
        final File dictionaryFile = setUpDictionary("Rhodinal\nScruff\nPibrochs");
        final Dictionary dictionary = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        final ValidationInfo validationInfo = dictionary.validate();

        assertThat(validationInfo).isNull();
    }

    @Test
    public void testValidateStaticSuccess() {
        final File dictionaryFile = setUpDictionary("Bbls\nOverpray\nTreeward");

        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate(dictionaryFile.getAbsolutePath());

        assertThat(validationInfo).isNull();
    }

    @Test
    public void testValidateStaticFileDoesNotExist() {
        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate("invalid_path");

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for invalid_path no longer exists.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    public void testValidateStaticFileEmpty() {
        final File dictionaryFile = setUpDictionary("");
        final String dictionaryName = dictionaryFile.getName();

        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate(dictionaryFile.getAbsolutePath());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + dictionaryName + " is empty.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    public void testValidateStaticListSuccess() {
        final File dictionaryFileA = setUpDictionary("Hexaxon\nChuse\nFricace");
        final File dictionaryFileB = setUpDictionary("Psyllid\nRefroze\nRoving");

        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate(Arrays.asList(
                dictionaryFileA.getAbsolutePath(),
                dictionaryFileB.getAbsolutePath()
        ));

        assertThat(validationInfo).isNull();
    }

    @Test
    public void testValidateStaticListPartial() {
        final File dictionaryFileA = setUpDictionary("Hexaxon\nChuse\nFricace");
        final File dictionaryFileB = setUpDictionary("");
        final String dictionaryFileBName = dictionaryFileB.getName();

        final ValidationInfo validationInfo = Dictionary.UserDictionary.validate(Arrays.asList(
                dictionaryFileA.getAbsolutePath(),
                dictionaryFileB.getAbsolutePath()
        ));

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for " + dictionaryFileBName + " is empty.");
        assertThat(validationInfo.component).isNull();
    }


    @Test
    public void testToString() {
        final File dictionaryFile = setUpDictionary("Cholers\nJaloused\nStopback");
        final String dictionaryName = dictionaryFile.getName();

        final Dictionary dictionary = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        assertThat(dictionary.toString()).isEqualTo("[custom] " + dictionaryName);
    }


    /**
     * Creates a temporary dictionary file with the given contents.
     * <p>
     * Because the created file is a temporary file, it does not have to be cleaned up afterwards.
     *
     * @param contents the contents to write to the dictionary file
     * @return the created temporary dictionary file
     */
    private File setUpDictionary(final String contents) {
        final File dictionaryFile;

        try {
            dictionaryFile = File.createTempFile("dictionary", ".dic");
            Files.write(dictionaryFile.toPath(), contents.getBytes(StandardCharsets.UTF_8));

            return dictionaryFile;
        } catch (final IOException e) {
            fail("Could not set up dictionary file.");
            return new File("");
        }
    }
}
