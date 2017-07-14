package com.fwdekker.randomness.string;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link Alphabet}.
 */
public final class AlphabetTest {
    @Test
    public void testConcatenate() {
        final List<Alphabet> alphabets = Arrays.asList(Alphabet.LOWERCASE, Alphabet.MINUS, Alphabet.SPECIAL);

        assertThat(Alphabet.concatenate(alphabets)).isEqualTo("abcdefghijklmnopqrstuvwxyz-!@#$%^&*");
    }


    @Test
    public void testGetSymbols() {
        assertThat(Alphabet.UPPERCASE.getSymbols()).isEqualTo("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Test
    public void testGetDescription() {
        assertThat(Alphabet.UPPERCASE.getDescription()).isEqualTo("Uppercase (A, B, C, ...)");
    }

    @Test
    public void testToString() {
        assertThat(Alphabet.UPPERCASE.toString()).isEqualTo("Uppercase (A, B, C, ...)");
    }
}
