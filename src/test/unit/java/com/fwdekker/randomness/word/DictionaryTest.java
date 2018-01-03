package com.fwdekker.randomness.word;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link Dictionary}.
 */
public final class DictionaryTest {
    private Dictionary dictionary;


    @Test
    public void testEmptyDictionary() {
        assertThatThrownBy(() -> new Dictionary.BundledDictionary("dictionaries/empty.dic"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dictionary must be non-empty.")
                .hasNoCause();
    }


    @Test
    public void testGetWords() {
        useDictionary("simple");

        assertThat(dictionary.getWords())
                .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow");
    }


    @Test
    public void testGetWordsWithLengthInRangeInvertedRange() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(696, 54))
                .isEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeNegativeLength() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(-4, -2))
                .isEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeEmptyRange() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(0, 0))
                .isEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeOvershortWord() {
        useDictionary("varied");

        assertThat(dictionary.getWordsWithLengthInRange(0, 1))
                .isEmpty();
    }

    @Test
    public void testGetWordsWithLengthInRangeShortWord() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(1, 1))
                .containsExactly("a");
    }

    @Test
    public void testGetWordsWithLengthInRangeLongWord() {
        useDictionary("varied");

        assertThat(dictionary.getWordsWithLengthInRange(40, 50))
                .containsExactly("pneumonoultramicroscopicsilicovolcanoconiosis");
    }

    @Test
    public void testGetWordsWithLengthInRangeOverlongWord() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(1000, 1001))
                .isEmpty();
    }


    @Test
    public void testLongestWordSimpleLength() {
        useDictionary("simple");

        assertThat(dictionary.longestWordLength())
                .isEqualTo(4);
    }

    @Test
    public void testLongestWordVariedLength() {
        useDictionary("varied");

        assertThat(dictionary.longestWordLength())
                .isEqualTo(45);
    }


    @Test
    public void testCombineWith() {
        final Dictionary combined = new Dictionary.BundledDictionary("dictionaries/simple.dic")
                .combineWith(new Dictionary.BundledDictionary("dictionaries/varied.dic"));

        assertThat(combined.getWords())
                .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow", "simplicity", "bend",
                                           "consideration", "pneumonoultramicroscopicsilicovolcanoconiosis");
    }

    @Test
    public void testCombineWithDuplicates() {
        final Dictionary combined = new Dictionary.BundledDictionary("dictionaries/simple.dic")
                .combineWith(new Dictionary.BundledDictionary("dictionaries/simple.dic"));

        assertThat(combined.getWords())
                .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow");
    }


    /**
     * Selects the {@link Dictionary} to use for the current test.
     *
     * @param dictionaryName the filename of the dictionary to use for the current test
     */
    private void useDictionary(final String dictionaryName) {
        dictionary = new Dictionary.BundledDictionary("dictionaries/" + dictionaryName + ".dic");
    }
}
