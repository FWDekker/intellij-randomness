package com.fwdekker.randomness.string;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link Alphabet}.
 */
final class AlphabetTest {
    @Test
    void testConcatenate() {
        final List<Alphabet> alphabets = Arrays.asList(Alphabet.ALPHABET, Alphabet.MINUS, Alphabet.SPECIAL);

        assertThat(Alphabet.concatenate(alphabets)).isEqualTo("abcdefghijklmnopqrstuvwxyz-!@#$%^&*");
    }


    @Test
    void testGetName() {
        assertThat(Alphabet.ALPHABET.getName()).isEqualTo("Alphabet (a, b, c, ...)");
    }

    @Test
    void testGetSymbols() {
        assertThat(Alphabet.ALPHABET.getSymbols()).isEqualTo("abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    void testToString() {
        assertThat(Alphabet.ALPHABET.toString()).isEqualTo("Alphabet (a, b, c, ...)");
    }
}
