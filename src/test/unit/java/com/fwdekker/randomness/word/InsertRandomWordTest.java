package com.fwdekker.randomness.word;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Parameterized unit tests for {@link com.fwdekker.randomness.word.InsertRandomWord}.
 */
@RunWith(Parameterized.class)
public final class InsertRandomWordTest {
    private final int minLength;
    private final int maxLength;
    private final String enclosure;


    public InsertRandomWordTest(final int minLength, final int maxLength, final String enclosure) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.enclosure = enclosure;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] {
                {0, 1, ""},
                {1, 1, ""},
                {12, 12, ""},
                {3, 15, "\""},
                {3, 13, "`"},
                {7, 9, "delim"},
        });
    }


    @Test
    public void testValue() {
        final WordSettings wordSettings = new WordSettings();
        wordSettings.setMinLength(minLength);
        wordSettings.setMaxLength(maxLength);
        wordSettings.setEnclosure(enclosure);

        final InsertRandomWord insertRandomWord = new InsertRandomWord(wordSettings);
        final String randomString = insertRandomWord.generateString();

        assertThat(randomString)
                .startsWith(enclosure)
                .endsWith(enclosure);
        assertThat(randomString.length())
                .isGreaterThanOrEqualTo(minLength + 2 * enclosure.length())
                .isLessThanOrEqualTo(maxLength + 2 * enclosure.length());
    }
}