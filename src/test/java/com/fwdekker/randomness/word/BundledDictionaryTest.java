package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link Dictionary.BundledDictionary}.
 */
final class BundledDictionaryTest {
    @Test
    void testInitDoesNotExist() {
        assertThatThrownBy(() -> Dictionary.BundledDictionary.Companion.get("invalid_resource", true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Failed to read dictionary into memory.")
            .hasNoCause();
    }

    @Test
    void testInitEmpty() {
        assertThatThrownBy(() -> Dictionary.BundledDictionary.Companion.get("dictionaries/empty.dic", true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Dictionary must be non-empty.")
            .hasNoCause();
    }

    @Test
    void testInitTwiceEquals() {
        final Dictionary dictionaryA = Dictionary.BundledDictionary.Companion.get("dictionaries/varied.dic", true);
        final Dictionary dictionaryB = Dictionary.BundledDictionary.Companion.get("dictionaries/varied.dic", true);

        assertThat(dictionaryA).isSameAs(dictionaryB);
    }

    @Test
    void testInitTwiceNoCacheEqualButNotSame() {
        final Dictionary dictionaryA = Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", true);
        final Dictionary dictionaryB = Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", false);

        assertThat(dictionaryB).isEqualTo(dictionaryA);
        assertThat(dictionaryB).isNotSameAs(dictionaryA);
    }

    @Test
    void testInitNoCacheStoresAnyway() {
        final Dictionary dictionaryA = Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", false);
        final Dictionary dictionaryB = Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", true);

        assertThat(dictionaryB).isSameAs(dictionaryA);
    }


    @Test
    void testValidateInstanceSuccess() {
        final Dictionary dictionary = Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", true);

        final ValidationInfo validationInfo = dictionary.validate();

        assertThat(validationInfo).isNull();
    }

    @Test
    void testValidateStaticSuccess() {
        final ValidationInfo validationInfo =
            Dictionary.BundledDictionary.Companion.validate("dictionaries/simple.dic");

        assertThat(validationInfo).isNull();
    }

    @Test
    void testValidateStaticFileDoesNotExist() {
        final ValidationInfo validationInfo = Dictionary.BundledDictionary.Companion.validate("invalid_resource");

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary resource for invalid_resource no longer exists.");
        assertThat(validationInfo.component).isNull();
    }

    @Test
    void testValidateStaticFileEmpty() {
        final ValidationInfo validationInfo = Dictionary.BundledDictionary.Companion.validate("dictionaries/empty.dic");

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.message).isEqualTo("The dictionary resource for empty.dic is empty.");
        assertThat(validationInfo.component).isNull();
    }


    @Test
    void testToString() {
        final Dictionary dictionary = Dictionary.BundledDictionary.Companion.get("dictionaries/simple.dic", true);

        assertThat(dictionary.toString()).isEqualTo("[bundled] simple.dic");
    }
}
