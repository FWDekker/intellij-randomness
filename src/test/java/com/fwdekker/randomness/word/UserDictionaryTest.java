package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;


/**
 * Unit tests for {@link Dictionary.UserDictionary}.
 */
public final class UserDictionaryTest {
    private File dictionaryFile;
    private Dictionary dictionary;


    @Before
    public void beforeEach() throws IOException {
        dictionaryFile = new File("test/test.dic");
        final File dictionaryDirectory = dictionaryFile.getParentFile();

        if (!dictionaryDirectory.exists() && !dictionaryDirectory.mkdirs()) {
            fail("Failed to set up test directory.");
        }
        if (!dictionaryFile.createNewFile()) {
            fail("Failed to set up test file.");
        }

        Files.write(dictionaryFile.toPath(), "Spanners\nHeralds\nTree".getBytes(StandardCharsets.UTF_8));
    }

    @After
    public void afterEach() {
        final File dictionaryDirectory = dictionaryFile.getParentFile();

        if (dictionaryFile.exists() && !dictionaryFile.delete()) {
            Logger.getLogger(getClass().getName()).warning("Failed to clean up test files.");
        } else if (dictionaryDirectory.exists() && !dictionaryDirectory.delete()) {
            Logger.getLogger(getClass().getName()).warning("Failed to clean up test directory.");
        }
    }


    @Test
    public void testInvalidFile() {
        assertThatThrownBy(() -> Dictionary.UserDictionary.get("invalid_file"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to read dictionary into memory.")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void testValidateSuccess() {
        dictionary = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        final ValidationInfo validationInfo = dictionary.validate();

        assertThat(validationInfo).isNull();
    }

    @Test
    public void testValidateFileDoesNotExist() {
        dictionary = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        if (!dictionaryFile.delete()) {
            fail("Failed to delete test file as part of test.");
        }

        final ValidationInfo validationInfo = dictionary.validate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary file for test.dic no longer exists.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    public void testToString() {
        dictionary = Dictionary.UserDictionary.get(dictionaryFile.getAbsolutePath());

        assertThat(dictionary.toString()).isEqualTo("[custom] test.dic");
    }
}
