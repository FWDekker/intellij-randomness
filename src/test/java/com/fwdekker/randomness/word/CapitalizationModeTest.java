package com.fwdekker.randomness.word;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link CapitalizationMode}.
 */
public final class CapitalizationModeTest {
    @Test
    public void testRetainTransform() {
        assertThat(CapitalizationMode.RETAIN.getTransform().apply("AwfJYzzUoR")).isEqualTo("AwfJYzzUoR");
    }

    @Test
    public void testSentenceTransformEmptyString() {
        assertThat(CapitalizationMode.SENTENCE.getTransform().apply("")).isEqualTo("");
    }

    @Test
    public void testSentenceTransform() {
        assertThat(CapitalizationMode.SENTENCE.getTransform().apply("cOoKiE")).isEqualTo("Cookie");
    }

    @Test
    public void testUpperTransform() {
        assertThat(CapitalizationMode.UPPER.getTransform().apply("vAnDaLisM")).isEqualTo("VANDALISM");
    }

    @Test
    public void testLowerTransform() {
        assertThat(CapitalizationMode.LOWER.getTransform().apply("ChAnnEl")).isEqualTo("channel");
    }

    @Test
    public void testGetNameRetain() {
        assertThat(CapitalizationMode.RETAIN.getName()).isEqualTo("retain");
    }

    @Test
    public void testGetNameSentence() {
        assertThat(CapitalizationMode.SENTENCE.getName()).isEqualTo("sentence");
    }

    @Test
    public void testGetNameUpper() {
        assertThat(CapitalizationMode.UPPER.getName()).isEqualTo("upper");
    }

    @Test
    public void testGetNameLower() {
        assertThat(CapitalizationMode.LOWER.getName()).isEqualTo("lower");
    }

    @Test
    public void testToStringRetain() {
        assertThat(CapitalizationMode.RETAIN.toString()).isEqualTo("retain");
    }

    @Test
    public void testToStringSentence() {
        assertThat(CapitalizationMode.SENTENCE.toString()).isEqualTo("sentence");
    }

    @Test
    public void testToStringUpper() {
        assertThat(CapitalizationMode.UPPER.toString()).isEqualTo("upper");
    }

    @Test
    public void testToStringLower() {
        assertThat(CapitalizationMode.LOWER.toString()).isEqualTo("lower");
    }

    @Test
    public void testGetModeRetain() {
        assertThat(CapitalizationMode.getMode("retain")).isEqualTo(CapitalizationMode.RETAIN);
    }

    @Test
    public void testGetModeSentence() {
        assertThat(CapitalizationMode.getMode("sentence")).isEqualTo(CapitalizationMode.SENTENCE);
    }

    @Test
    public void testGetModeUpper() {
        assertThat(CapitalizationMode.getMode("upper")).isEqualTo(CapitalizationMode.UPPER);
    }

    @Test
    public void testGetModeLower() {
        assertThat(CapitalizationMode.getMode("lower")).isEqualTo(CapitalizationMode.LOWER);
    }

    @Test
    public void testGetModeOther() {
        assertThatThrownBy(() -> CapitalizationMode.getMode(""))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("There does not exist a capitalization mode with name ``.")
                .hasNoCause();
    }
}
