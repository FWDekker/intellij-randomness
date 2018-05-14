package com.fwdekker.randomness.string;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Parameterized unit tests for {@link StringInsertAction}.
 */
final class StringInsertActionTest {
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Used as parameterized method source
    private static Collection<Object[]> provider() {
        return Arrays.asList(new Object[][]{
                {0, 0, "", new Alphabet[]{}},
                {0, 0, "'", new Alphabet[]{}},
                {0, 0, "a", new Alphabet[]{}},
                {0, 0, "2Rv", new Alphabet[]{}},

                {723, 723, "", new Alphabet[]{Alphabet.LOWERCASE}},
                {466, 466, "z", new Alphabet[]{Alphabet.UPPERCASE, Alphabet.SPECIAL, Alphabet.UNDERSCORE}}
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final int minLength, final int maxLength, final String enclosure, final Alphabet... alphabets) {
        final Set<Alphabet> alphabetSet = new HashSet<>(Arrays.asList(alphabets));

        final StringSettings stringSettings = new StringSettings();
        stringSettings.setMinLength(minLength);
        stringSettings.setMaxLength(maxLength);
        stringSettings.setEnclosure(enclosure);
        stringSettings.setAlphabets(alphabetSet);

        final StringInsertAction insertRandomString = new StringInsertAction(stringSettings);
        final Pattern expectedPattern = buildExpectedPattern(minLength, maxLength, enclosure, alphabetSet);

        assertThat(insertRandomString.generateString()).containsPattern(expectedPattern);
    }


    private Pattern buildExpectedPattern(final int minLength, final int maxLength, final String enclosure,
                                         final Set<Alphabet> alphabets) {
        final StringBuilder regex = new StringBuilder();

        regex.append(enclosure);
        if (!alphabets.isEmpty()) {
            regex
                    .append('[')
                    .append(Alphabet.concatenate(alphabets))
                    .append("]{")
                    .append(minLength).append(',').append(maxLength)
                    .append('}');
        }
        regex.append(enclosure);

        return Pattern.compile(regex.toString());
    }
}
