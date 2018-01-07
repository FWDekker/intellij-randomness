package com.fwdekker.randomness.array;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link ArraySettings}.
 */
public final class ArraySettingsTest {
    private ArraySettings arraySettings;


    @Before
    public void beforeEach() {
        arraySettings = new ArraySettings();
    }


    @Test
    public void testGetComponentName() {
        assertThat(arraySettings.getComponentName()).isEqualTo("ArraySettings");
    }

    @Test
    public void testGetLoadState() {
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
    public void testGetSetCount() {
        arraySettings.setCount(655);

        assertThat(arraySettings.getCount()).isEqualTo(655);
    }

    @Test
    public void testGetSetBrackets() {
        arraySettings.setBrackets("RLevljrzf0");

        assertThat(arraySettings.getBrackets()).isEqualTo("RLevljrzf0");
    }

    @Test
    public void testGetSetSeparator() {
        arraySettings.setBrackets("d2[tlXkGf{");

        assertThat(arraySettings.getBrackets()).isEqualTo("d2[tlXkGf{");
    }

    @Test
    public void testGetSetSpaceAfterSeparator() {
        arraySettings.setSpaceAfterSeparator(false);

        assertThat(arraySettings.isSpaceAfterSeparator()).isEqualTo(false);
    }
}
