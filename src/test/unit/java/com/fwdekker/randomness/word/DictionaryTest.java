package com.fwdekker.randomness.word;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


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
    public void testLongestWordLength() {
        assertThat(Dictionary.longestWordLength()).isEqualTo(31);
    }
}
