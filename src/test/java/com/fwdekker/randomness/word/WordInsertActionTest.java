package com.fwdekker.randomness.word;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Parameterized unit tests for {@link WordInsertAction}.
 */
final class WordInsertActionTest {
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Used as parameterized method source
    private static Collection<Object[]> provider() {
        return Arrays.asList(new Object[][]{
            {0, 1, ""},
            {1, 1, ""},
            {12, 12, ""},
            {3, 15, "\""},
            {3, 13, "`"},
            {7, 9, "delim"},
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final int minLength, final int maxLength, final String enclosure) {
        final WordSettings wordSettings = new WordSettings();
        wordSettings.setMinLength(minLength);
        wordSettings.setMaxLength(maxLength);
        wordSettings.setEnclosure(enclosure);

        final WordInsertAction insertRandomWord = new WordInsertAction(wordSettings);
        final String randomString = insertRandomWord.generateString();

        assertThat(randomString)
            .startsWith(enclosure)
            .endsWith(enclosure);
        assertThat(randomString.length())
            .isGreaterThanOrEqualTo(minLength + 2 * enclosure.length())
            .isLessThanOrEqualTo(maxLength + 2 * enclosure.length());
    }
}
