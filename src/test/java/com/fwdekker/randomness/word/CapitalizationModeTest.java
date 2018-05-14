package com.fwdekker.randomness.word;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link CapitalizationMode}.
 */
final class CapitalizationModeTest {
    @Test
    void testRetainTransform() {
        assertThat(CapitalizationMode.RETAIN.getTransform().apply("AwfJYzzUoR")).isEqualTo("AwfJYzzUoR");
    }

    @Test
    void testSentenceTransformEmptyString() {
        assertThat(CapitalizationMode.SENTENCE.getTransform().apply("")).isEqualTo("");
    }

    @Test
    void testSentenceTransform() {
        assertThat(CapitalizationMode.SENTENCE.getTransform().apply("cOoKiE")).isEqualTo("Cookie");
    }

    @Test
    void testUpperTransform() {
        assertThat(CapitalizationMode.UPPER.getTransform().apply("vAnDaLisM")).isEqualTo("VANDALISM");
    }

    @Test
    void testLowerTransform() {
        assertThat(CapitalizationMode.LOWER.getTransform().apply("ChAnnEl")).isEqualTo("channel");
    }

    @Test
    void testFirstLetterTransform() {
        assertThat(CapitalizationMode.FIRST_LETTER.getTransform().apply("bgiOP SMQpR")).isEqualTo("Bgiop Smqpr");
    }

    @Test
    void testGetNameRetain() {
        assertThat(CapitalizationMode.RETAIN.getName()).isEqualTo("retain");
    }

    @Test
    void testGetNameSentence() {
        assertThat(CapitalizationMode.SENTENCE.getName()).isEqualTo("sentence");
    }

    @Test
    void testGetNameUpper() {
        assertThat(CapitalizationMode.UPPER.getName()).isEqualTo("upper");
    }

    @Test
    void testGetNameLower() {
        assertThat(CapitalizationMode.LOWER.getName()).isEqualTo("lower");
    }

    @Test
    void testGetNameFirstLetter() {
        assertThat(CapitalizationMode.FIRST_LETTER.getName()).isEqualTo("first letter");
    }

    @Test
    void testToStringRetain() {
        assertThat(CapitalizationMode.RETAIN.toString()).isEqualTo("retain");
    }

    @Test
    void testToStringSentence() {
        assertThat(CapitalizationMode.SENTENCE.toString()).isEqualTo("sentence");
    }

    @Test
    void testToStringUpper() {
        assertThat(CapitalizationMode.UPPER.toString()).isEqualTo("upper");
    }

    @Test
    void testToStringLower() {
        assertThat(CapitalizationMode.LOWER.toString()).isEqualTo("lower");
    }

    @Test
    void testToStringFirstLetter() {
        assertThat(CapitalizationMode.FIRST_LETTER.toString()).isEqualTo("first letter");
    }

    @Test
    void testGetModeRetain() {
        assertThat(CapitalizationMode.getMode("retain")).isEqualTo(CapitalizationMode.RETAIN);
    }

    @Test
    void testGetModeSentence() {
        assertThat(CapitalizationMode.getMode("sentence")).isEqualTo(CapitalizationMode.SENTENCE);
    }

    @Test
    void testGetModeUpper() {
        assertThat(CapitalizationMode.getMode("upper")).isEqualTo(CapitalizationMode.UPPER);
    }

    @Test
    void testGetModeLower() {
        assertThat(CapitalizationMode.getMode("lower")).isEqualTo(CapitalizationMode.LOWER);
    }

    @Test
    void testGetModeFirstLetter() {
        assertThat(CapitalizationMode.getMode("first letter")).isEqualTo(CapitalizationMode.FIRST_LETTER);
    }

    @Test
    void testGetModeOther() {
        assertThatThrownBy(() -> CapitalizationMode.getMode(""))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("There does not exist a capitalization mode with name ``.")
                .hasNoCause();
    }
}
