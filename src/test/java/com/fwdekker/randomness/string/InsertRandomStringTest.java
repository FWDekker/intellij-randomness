package com.fwdekker.randomness.string;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Parameterized unit tests for {@link InsertRandomString}.
 */
@RunWith(Parameterized.class)
public final class InsertRandomStringTest {
    private final int minLength;
    private final int maxLength;
    private final String enclosure;
    private final Set<Alphabet> alphabets;


    public InsertRandomStringTest(final int minLength, final int maxLength, final String enclosure,
                                  final Alphabet... alphabets) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.enclosure = enclosure;
        this.alphabets = new HashSet<>(Arrays.asList(alphabets));
    }


    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] {
                {0, 0, "", new Alphabet[] {}},
                {0, 0, "'", new Alphabet[] {}},
                {0, 0, "a", new Alphabet[] {}},
                {0, 0, "2Rv", new Alphabet[] {}},

                {723, 723, "", new Alphabet[] {Alphabet.LOWERCASE}},
                {466, 466, "z", new Alphabet[] {Alphabet.UPPERCASE, Alphabet.SPECIAL, Alphabet.UNDERSCORE}}
        });
    }


    @Test
    public void testValue() {
        final StringSettings stringSettings = new StringSettings();
        stringSettings.setMinLength(minLength);
        stringSettings.setMaxLength(maxLength);
        stringSettings.setEnclosure(enclosure);
        stringSettings.setAlphabets(alphabets);

        final InsertRandomString insertRandomString = new InsertRandomString(stringSettings);

        assertThat(insertRandomString.generateString()).containsPattern(buildResultPattern());
    }


    /**
     * Builds a pattern describing the expected format of generated string.
     *
     * @return a pattern describing the expected format of generated string
     */
    private Pattern buildResultPattern() {
        final StringBuilder regex = new StringBuilder();

        regex.append(enclosure);
        if (!alphabets.isEmpty()) {
            regex.append("[" + Alphabet.concatenate(alphabets) + "]{" + minLength + "," + maxLength + "}");
        }
        regex.append(enclosure);

        return Pattern.compile(regex.toString());
    }
}
