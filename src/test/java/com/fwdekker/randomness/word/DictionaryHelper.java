package com.fwdekker.randomness.word;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.fail;


/**
 * Helper class for tests of {@code Dictionary}s.
 */
public final class DictionaryHelper {
    /**
     * Private constructor to prevent instantiation.
     */
    private DictionaryHelper() {
    }


    /**
     * Creates a temporary dictionary file with the given contents.
     * <p>
     * Because the created file is a temporary file, it does not have to be cleaned up afterwards.
     *
     * @param contents the contents to write to the dictionary file
     * @return the created temporary dictionary file
     */
    public static File setUpDictionary(final String contents) {
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
