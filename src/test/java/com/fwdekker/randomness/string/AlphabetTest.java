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
        final List<Alphabet> alphabets = Arrays.asList(Alphabet.LOWERCASE, Alphabet.MINUS, Alphabet.SPECIAL);

        assertThat(Alphabet.concatenate(alphabets)).isEqualTo("abcdefghijklmnopqrstuvwxyz-!@#$%^&*");
    }


    @Test
    void testGetName() {
        assertThat(Alphabet.UPPERCASE.getName()).isEqualTo("Uppercase (A, B, C, ...)");
    }

    @Test
    void testGetSymbols() {
        assertThat(Alphabet.UPPERCASE.getSymbols()).isEqualTo("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Test
    void testToString() {
        assertThat(Alphabet.UPPERCASE.toString()).isEqualTo("Uppercase (A, B, C, ...)");
    }
}
