package com.fwdekker.randomness.array;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link ArraySettings}.
 */
final class ArraySettingsTest {
    private ArraySettings arraySettings;


    @BeforeEach
    void beforeEach() {
        arraySettings = new ArraySettings();
    }


    @Test
    void testGetLoadState() {
        arraySettings.setCount(997);
        arraySettings.setBrackets("0fWx<i6jTJ");
        arraySettings.setSeparator("f3hu)Rxiz1");
        arraySettings.setSpaceAfterSeparator(false);

        final ArraySettings newArraySettings = new ArraySettings();
        newArraySettings.loadState(arraySettings.getState());

        assertThat(newArraySettings.getCount()).isEqualTo(997);
        assertThat(newArraySettings.getBrackets()).isEqualTo("0fWx<i6jTJ");
        assertThat(newArraySettings.getSeparator()).isEqualTo("f3hu)Rxiz1");
        assertThat(newArraySettings.isSpaceAfterSeparator()).isEqualTo(false);
    }

    @Test
    void testGetSetCount() {
        arraySettings.setCount(655);

        assertThat(arraySettings.getCount()).isEqualTo(655);
    }

    @Test
    void testGetSetBrackets() {
        arraySettings.setBrackets("RLevljrzf0");

        assertThat(arraySettings.getBrackets()).isEqualTo("RLevljrzf0");
    }

    @Test
    void testGetSetSeparator() {
        arraySettings.setBrackets("d2[tlXkGf{");

        assertThat(arraySettings.getBrackets()).isEqualTo("d2[tlXkGf{");
    }

    @Test
    void testGetSetSpaceAfterSeparator() {
        arraySettings.setSpaceAfterSeparator(false);

        assertThat(arraySettings.isSpaceAfterSeparator()).isEqualTo(false);
    }


    @Test
    void testArrayifyEmpty() {
        assertThat(arraySettings.arrayify(Collections.emptyList()))
                .isEqualTo("[]");
    }

    @Test
    void testArrayify() {
        arraySettings.setCount(4);
        arraySettings.setBrackets("@#");
        arraySettings.setSeparator(";;");
        arraySettings.setSpaceAfterSeparator(true);

        assertThat(arraySettings.arrayify(Arrays.asList("Garhwali", "Pattypan", "Troll")))
                .isEqualTo("@Garhwali;; Pattypan;; Troll#");
    }

    @Test
    void testArrayifyNoBrackets() {
        arraySettings.setCount(8);
        arraySettings.setBrackets("");
        arraySettings.setSeparator("h");
        arraySettings.setSpaceAfterSeparator(false);

        assertThat(arraySettings.arrayify(Arrays.asList("Antheia", "Cowbinds", "Cotutor")))
                .isEqualTo("AntheiahCowbindshCotutor");
    }
}
