package com.fwdekker.randomness.word;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link Dictionary}.
 */
public final class DictionaryTest {
    private Dictionary dictionary;


    @Test
    public void testEmptyDictionary() {
        assertThatThrownBy(() -> Dictionary.BundledDictionary.get("dictionaries/empty.dic"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dictionary must be non-empty.")
                .hasNoCause();
    }

    @Test
    public void testInvalidFile() {
        assertThatThrownBy(() -> Dictionary.UserDictionary.get("invalid_file"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to read dictionary into memory.")
                .hasCauseInstanceOf(IOException.class);
    }


    @Test
    public void testGetPath() {
        useDictionary("simple");

        assertThat(dictionary.getUid())
                .isEqualTo("dictionaries/simple.dic");
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
    public void testGetShortestWordSimple() {
        useDictionary("simple");

        assertThat(dictionary.getShortestWord())
                .hasSize(1);
    }

    @Test
    public void testGetShortestWordVaried() {
        useDictionary("varied");

        assertThat(dictionary.getShortestWord())
                .hasSize(4);
    }

    @Test
    public void testGetLongestWordSimple() {
        useDictionary("simple");

        assertThat(dictionary.getLongestWord())
                .hasSize(4);
    }

    @Test
    public void testGetLongestWordVaried() {
        useDictionary("varied");

        assertThat(dictionary.getLongestWord())
                .hasSize(45);
    }


    @Test
    public void testCombine() {
        final Dictionary combined = Dictionary.combine(Arrays.asList(
                Dictionary.BundledDictionary.get("dictionaries/simple.dic"),
                Dictionary.BundledDictionary.get("dictionaries/varied.dic")));

        assertThat(combined.getWords())
                .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow", "simplicity", "bend",
                                           "consideration", "pneumonoultramicroscopicsilicovolcanoconiosis");
    }

    @Test
    public void testCombineDuplicates() {
        final Dictionary combined = Dictionary.combine(Arrays.asList(
                Dictionary.BundledDictionary.get("dictionaries/simple.dic"),
                Dictionary.BundledDictionary.get("dictionaries/simple.dic")));

        assertThat(combined.getWords())
                .containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow");
    }


    @Test
    public void testEqualsContract() {
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
