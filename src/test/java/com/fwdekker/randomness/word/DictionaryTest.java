package com.fwdekker.randomness.word;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link Dictionary}.
 */
final class DictionaryTest {
    private Dictionary dictionary;


    @Test
    void testEmptyDictionary() {
        assertThatThrownBy(() -> Dictionary.BundledDictionary.get("dictionaries/empty.dic"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Dictionary must be non-empty.")
            .hasNoCause();
    }


    @Test
    void testGetPath() {
        useDictionary("simple");

        assertThat(dictionary.getUid())
            .isEqualTo("dictionaries/simple.dic");
    }

    @Test
    void testGetWords() {
        useDictionary("simple");

        assertThat(dictionary.getWords())
            .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow");
    }


    @Test
    void testGetWordsWithLengthInRangeInvertedRange() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(696, 54))
            .isEmpty();
    }

    @Test
    void testGetWordsWithLengthInRangeNegativeLength() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(-4, -2))
            .isEmpty();
    }

    @Test
    void testGetWordsWithLengthInRangeEmptyRange() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(0, 0))
            .isEmpty();
    }

    @Test
    void testGetWordsWithLengthInRangeOvershortWord() {
        useDictionary("varied");

        assertThat(dictionary.getWordsWithLengthInRange(0, 1))
            .isEmpty();
    }

    @Test
    void testGetWordsWithLengthInRangeShortWord() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(1, 1))
            .containsExactly("a");
    }

    @Test
    void testGetWordsWithLengthInRangeLongWord() {
        useDictionary("varied");

        assertThat(dictionary.getWordsWithLengthInRange(40, 50))
            .containsExactly("pneumonoultramicroscopicsilicovolcanoconiosis");
    }

    @Test
    void testGetWordsWithLengthInRangeOverlongWord() {
        useDictionary("simple");

        assertThat(dictionary.getWordsWithLengthInRange(1000, 1001))
            .isEmpty();
    }


    @Test
    void testGetShortestWordSimple() {
        useDictionary("simple");

        assertThat(dictionary.getShortestWord())
            .hasSize(1);
    }

    @Test
    void testGetShortestWordVaried() {
        useDictionary("varied");

        assertThat(dictionary.getShortestWord())
            .hasSize(4);
    }

    @Test
    void testGetLongestWordSimple() {
        useDictionary("simple");

        assertThat(dictionary.getLongestWord())
            .hasSize(4);
    }

    @Test
    void testGetLongestWordVaried() {
        useDictionary("varied");

        assertThat(dictionary.getLongestWord())
            .hasSize(45);
    }


    @Test
    void testCombine() {
        final Dictionary combined = Dictionary.combine(Arrays.asList(
            Dictionary.BundledDictionary.get("dictionaries/simple.dic"),
            Dictionary.BundledDictionary.get("dictionaries/varied.dic")));

        assertThat(combined.getWords())
            .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow", "simplicity", "bend",
                "consideration", "pneumonoultramicroscopicsilicovolcanoconiosis");
    }

    @Test
    void testCombineDuplicates() {
        final Dictionary combined = Dictionary.combine(Arrays.asList(
            Dictionary.BundledDictionary.get("dictionaries/simple.dic"),
            Dictionary.BundledDictionary.get("dictionaries/simple.dic")));

        assertThat(combined.getWords())
            .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow");
    }


    @Test
    void testEqualsContract() {
        EqualsVerifier.forClass(Dictionary.class)
            .usingGetClass()
            .withIgnoredFields("name", "words")
            .verify();
    }


    /**
     * Selects the {@link Dictionary} to use for the current test.
     *
     * @param dictionaryName the filename of the dictionary to use for the current test
     */
    private void useDictionary(final String dictionaryName) {
        dictionary = Dictionary.BundledDictionary.get("dictionaries/" + dictionaryName + ".dic");
    }
}
