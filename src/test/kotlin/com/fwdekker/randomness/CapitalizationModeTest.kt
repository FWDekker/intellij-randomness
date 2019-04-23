package com.fwdekker.randomness

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.NoSuchElementException


/**
 * Unit tests for [CapitalizationMode].
 */
class CapitalizationModeTest {
    @Test
    fun testRetainTransform() {
        assertThat(CapitalizationMode.RETAIN.transform.invoke("AwfJYzzUoR")).isEqualTo("AwfJYzzUoR")
    }

    @Test
    fun testSentenceTransformEmptyString() {
        assertThat(CapitalizationMode.SENTENCE.transform.invoke("")).isEqualTo("")
    }

    @Test
    fun testSentenceTransform() {
        assertThat(CapitalizationMode.SENTENCE.transform.invoke("cOoKiE")).isEqualTo("Cookie")
    }

    @Test
    fun testUpperTransform() {
        assertThat(CapitalizationMode.UPPER.transform.invoke("vAnDaLisM")).isEqualTo("VANDALISM")
    }

    @Test
    fun testLowerTransform() {
        assertThat(CapitalizationMode.LOWER.transform.invoke("ChAnnEl")).isEqualTo("channel")
    }

    @Test
    fun testFirstLetterTransform() {
        assertThat(CapitalizationMode.FIRST_LETTER.transform.invoke("bgiOP SMQpR")).isEqualTo("Bgiop Smqpr")
    }

    @Test
    fun testRandomTransform() {
        assertThat(CapitalizationMode.RANDOM.transform.invoke("GHmdukhNqua"))
            .isNotEqualTo("GHmdukhNqua") // Has a chance of 0.002% of failing
            .isEqualToIgnoringCase("GHmdukhNqua")
    }


    @Test
    fun testGetNameRetain() {
        assertThat(CapitalizationMode.RETAIN.descriptor).isEqualTo("retain")
    }

    @Test
    fun testGetNameSentence() {
        assertThat(CapitalizationMode.SENTENCE.descriptor).isEqualTo("sentence")
    }

    @Test
    fun testGetNameUpper() {
        assertThat(CapitalizationMode.UPPER.descriptor).isEqualTo("upper")
    }

    @Test
    fun testGetNameLower() {
        assertThat(CapitalizationMode.LOWER.descriptor).isEqualTo("lower")
    }

    @Test
    fun testGetNameFirstLetter() {
        assertThat(CapitalizationMode.FIRST_LETTER.descriptor).isEqualTo("first letter")
    }

    @Test
    fun testGetNameRandom() {
        assertThat(CapitalizationMode.RANDOM.descriptor).isEqualTo("random")
    }


    @Test
    fun testToStringRetain() {
        assertThat(CapitalizationMode.RETAIN.toString()).isEqualTo("retain")
    }

    @Test
    fun testToStringSentence() {
        assertThat(CapitalizationMode.SENTENCE.toString()).isEqualTo("sentence")
    }

    @Test
    fun testToStringUpper() {
        assertThat(CapitalizationMode.UPPER.toString()).isEqualTo("upper")
    }

    @Test
    fun testToStringLower() {
        assertThat(CapitalizationMode.LOWER.toString()).isEqualTo("lower")
    }

    @Test
    fun testToStringFirstLetter() {
        assertThat(CapitalizationMode.FIRST_LETTER.toString()).isEqualTo("first letter")
    }

    @Test
    fun testToStringRandom() {
        assertThat(CapitalizationMode.RANDOM.toString()).isEqualTo("random")
    }


    @Test
    fun testGetModeRetain() {
        assertThat(CapitalizationMode.getMode("retain")).isEqualTo(CapitalizationMode.RETAIN)
    }

    @Test
    fun testGetModeSentence() {
        assertThat(CapitalizationMode.getMode("sentence")).isEqualTo(CapitalizationMode.SENTENCE)
    }

    @Test
    fun testGetModeUpper() {
        assertThat(CapitalizationMode.getMode("upper")).isEqualTo(CapitalizationMode.UPPER)
    }

    @Test
    fun testGetModeLower() {
        assertThat(CapitalizationMode.getMode("lower")).isEqualTo(CapitalizationMode.LOWER)
    }

    @Test
    fun testGetModeFirstLetter() {
        assertThat(CapitalizationMode.getMode("first letter")).isEqualTo(CapitalizationMode.FIRST_LETTER)
    }

    @Test
    fun testGetModeRandom() {
        assertThat(CapitalizationMode.getMode("random")).isEqualTo(CapitalizationMode.RANDOM)
    }

    @Test
    fun testGetModeOther() {
        assertThatThrownBy { CapitalizationMode.getMode("") }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("There does not exist a capitalization mode with name ``.")
            .hasNoCause()
    }
}
