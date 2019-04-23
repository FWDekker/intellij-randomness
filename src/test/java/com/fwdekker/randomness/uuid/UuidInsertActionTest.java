package com.fwdekker.randomness.uuid;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


/**
 * Parameterized unit tests for {@link UuidInsertAction}.
 */
final class UuidInsertActionTest {
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Used as parameterized method source
    private static Collection<Object[]> provider() {
        return Arrays.asList(new Object[][]{
            {""},
            {"'"},
            {"Eglzfpf5"}
        });
    }


    @ParameterizedTest
    @MethodSource("provider")
    void testValue(final String enclosure) {
        final UuidSettings uuidSettings = new UuidSettings();
        uuidSettings.setEnclosure(enclosure);

        final UuidInsertAction insertRandomUuid = new UuidInsertAction(uuidSettings);
        final String generatedString = insertRandomUuid.generateString();

        assertThat(generatedString)
            .startsWith(enclosure)
            .endsWith(enclosure);

        final String generatedUuid = generatedString
            .replaceAll("^" + enclosure, "")
            .replaceAll(enclosure + "$", "");
        assertThatCode(() -> UUID.fromString(generatedUuid)).doesNotThrowAnyException();
    }
}
