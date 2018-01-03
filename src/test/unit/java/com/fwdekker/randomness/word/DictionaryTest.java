package com.fwdekker.randomness.word;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;


/**
 * Unit tests for {@link Dictionary}.
 */
public final class DictionaryTest {
    @Test
    public void testGetWordsWithLengthInRangeInvertedRange() {
        assertThat(Dictionary.getWordsWithLengthInRange(1000, 0)).isEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeNegativeLength() {
        assertThat(Dictionary.getWordsWithLengthInRange(-1000, 0)).isEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeEmptyWord() {
        assertThat(Dictionary.getWordsWithLengthInRange(0, 0)).isEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeShortWord() {
        assertThat(Dictionary.getWordsWithLengthInRange(0, 1)).isNotEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeLongWord() {
        assertThat(Dictionary.getWordsWithLengthInRange(1000, 1001)).isEmpty();
    }


    @Test
    public void testWordWithLengthInRangeExistsInvertedRange() {
        assertThat(Dictionary.wordWithLengthInRangeExists(1000, 0)).isFalse();
    }

    @Test
    public void testWordWithLengthInRangeExistsNegativeLength() {
        assertThat(Dictionary.wordWithLengthInRangeExists(-1000, 0)).isFalse();
    }

    @Test
    public void testWordWithLengthInRangeExistsEmptyWord() {
        assertThat(Dictionary.wordWithLengthInRangeExists(0, 0)).isFalse();
    }

    @Test
    public void testWordWithLengthInRangeExistsShortWord() {
        assertThat(Dictionary.wordWithLengthInRangeExists(0, 1)).isTrue();
    }

    @Test
    public void testWordWithLengthInRangeExistsLongWord() {
        assertThat(Dictionary.wordWithLengthInRangeExists(1000, 1001)).isFalse();
    }
}
