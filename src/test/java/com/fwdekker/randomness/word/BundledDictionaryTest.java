package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link Dictionary.BundledDictionary}.
 */
public final class BundledDictionaryTest {
    @Test
    public void testConstructorDoesNotExist() {
        assertThatThrownBy(() -> Dictionary.BundledDictionary.get("invalid resource"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to read dictionary into memory.")
                .hasNoCause();
    }

    @Test
    public void testValidateSuccess() {
        final Dictionary dictionary = Dictionary.BundledDictionary.get("dictionaries/simple.dic");

        final ValidationInfo validationInfo = dictionary.validate();

        assertThat(validationInfo).isNull();
    }

    @Test
    public void testToString() {
        final Dictionary dictionary = Dictionary.BundledDictionary.get("dictionaries/simple.dic");

        assertThat(dictionary.toString()).isEqualTo("[bundled] simple.dic");
    }
}
