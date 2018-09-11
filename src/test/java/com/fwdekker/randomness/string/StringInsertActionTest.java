package com.fwdekker.randomness.string;

import com.fwdekker.randomness.CapitalizationMode;
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
                {0, 0, "", CapitalizationMode.RETAIN, new Alphabet[]{}},
                {0, 0, "'", CapitalizationMode.UPPER, new Alphabet[]{}},
                {0, 0, "a", CapitalizationMode.LOWER, new Alphabet[]{}},
                {0, 0, "2Rv", CapitalizationMode.FIRST_LETTER, new Alphabet[]{}},

                {723, 723, "", CapitalizationMode.UPPER, new Alphabet[]{Alphabet.ALPHABET}},
                {466, 466, "z", CapitalizationMode.LOWER, new Alphabet[]{Alphabet.ALPHABET, Alphabet.UNDERSCORE}}
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final int minLength, final int maxLength, final String enclosure,
                   final CapitalizationMode capitalization, final Alphabet... alphabets) {
        final Set<Alphabet> alphabetSet = new HashSet<>(Arrays.asList(alphabets));

        final StringSettings stringSettings = new StringSettings();
        stringSettings.setMinLength(minLength);
        stringSettings.setMaxLength(maxLength);
        stringSettings.setEnclosure(enclosure);
        stringSettings.setCapitalization(capitalization);
        stringSettings.setAlphabets(alphabetSet);

        final StringInsertAction insertRandomString = new StringInsertAction(stringSettings);
        final Pattern expectedPattern
                = buildExpectedPattern(minLength, maxLength, enclosure, capitalization, alphabetSet);

        assertThat(insertRandomString.generateString()).containsPattern(expectedPattern);
    }


    private Pattern buildExpectedPattern(final int minLength, final int maxLength, final String enclosure,
                                         final CapitalizationMode capitalization, final Set<Alphabet> alphabets) {
        final StringBuilder regex = new StringBuilder();

        regex.append(enclosure);
        if (!alphabets.isEmpty()) {
            regex
                    .append('[')
                    .append(capitalization.getTransform().apply(Alphabet.concatenate(alphabets)))
                    .append("]{")
                    .append(minLength).append(',').append(maxLength)
                    .append('}');
        }
        regex.append(enclosure);

        return Pattern.compile(regex.toString());
    }
}
