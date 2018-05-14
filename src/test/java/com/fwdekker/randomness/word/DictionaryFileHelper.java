package com.fwdekker.randomness.word;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.fail;


/**
 * Helper class for file manipulation for tests of {@code Dictionary}s.
 */
final class DictionaryFileHelper {
    /**
     * The files that have been created by this helper.
     */
    private final List<File> files;


    /**
     * Constructs a new {@code DictionaryFileHelper}.
     */
    DictionaryFileHelper() {
        files = new ArrayList<>();
    }


    /**
     * Creates a temporary dictionary file with the given contents.
     * <p>
     * Because the created file is a temporary file, it does not have to be cleaned up afterwards.
     *
     * @param contents the contents to write to the dictionary file
     * @return the created temporary dictionary file
     */
    File setUpDictionary(final String contents) {
        final File dictionaryFile;

        try {
            dictionaryFile = File.createTempFile("dictionary", ".dic");
            Files.write(dictionaryFile.toPath(), contents.getBytes(StandardCharsets.UTF_8));
            files.add(dictionaryFile);

            return dictionaryFile;
        } catch (final IOException e) {
            fail("Could not set up dictionary file.");
            return new File("");
        }
    }

    /**
     * Cleans up the created dictionary files.
     */
    void cleanUpDictionaries() {
        for (final File dictionaryFile : files) {
            if (dictionaryFile.exists() && !dictionaryFile.delete()) {
                Logger.getLogger(this.getClass().getName()).warning("Failed to clean up dictionary file.");
            }
        }

        files.clear();
    }
}
