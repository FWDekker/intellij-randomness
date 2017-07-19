package com.fwdekker.randomness;

import java.util.ArrayList;
import java.util.RandomAccess;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for {@link NamedItem}.
 */
public final class NamedItemTest {
    @Test
    public void testName() {
        final NamedItem<Object> namedItem = new NamedItem<>("OI6d7AkKye", new Object());

        assertThat(namedItem.getName()).isEqualTo("OI6d7AkKye");
    }

    @Test
    public void testItem() {
        final Object item = new Object();

        final NamedItem<Object> namedItem = new NamedItem<>("mrOz7tVmoX", item);

        assertThat(namedItem.getItem()).isEqualTo(item);
    }

    @Test
    public void testToString() {
        final NamedItem<Object> namedItem = new NamedItem<>("2Z6oCfzfli", new Object());

        assertThat(namedItem.toString()).isEqualTo("2Z6oCfzfli");
    }


    @Test
    public void testEqualsSelf() {
        final NamedItem<Object> namedItem = new NamedItem<>("B4md9THj3y", new Object());

        assertThat(namedItem.equals(namedItem)).isTrue();
    }

    @Test
    @SuppressWarnings("PMD.EqualsNull") // Exactly what is being tested here
    public void testEqualsNull() {
        final NamedItem<Object> namedItem = new NamedItem<>("c5pgKxqS41", new Object());

        assertThat(namedItem.equals(null)).isFalse();
    }

    @Test
    public void testEqualsOtherClass() {
        final NamedItem<Object> namedItem = new NamedItem<>("lGdlNMLoyt", new Object());

        assertThat(namedItem.equals(new Object())).isFalse();
    }

    @Test
    public void testEqualsOtherType() {
        final ArrayList realItem = new ArrayList();
        final NamedItem<RandomAccess> namedItemA = new NamedItem<>("znKHUZqExm", realItem);
        final NamedItem<Cloneable> namedItemB = new NamedItem<>("znKHUZqExm", realItem);

        assertThat(namedItemA.equals(namedItemB)).isTrue();
    }

    @Test
    public void testEqualsOtherName() {
        final Object item = new Object();
        final NamedItem<Object> namedItemA = new NamedItem<>("osEjuXELqh", item);
        final NamedItem<Object> namedItemB = new NamedItem<>("BOGVSgfrjF", item);

        assertThat(namedItemA.equals(namedItemB)).isFalse();
    }

    @Test
    public void testEqualsOtherItem() {
        final NamedItem<Object> namedItemA = new NamedItem<>("iMmu1w7Doi", new Object());
        final NamedItem<Object> namedItemB = new NamedItem<>("iMmu1w7Doi", new Object());

        assertThat(namedItemA.equals(namedItemB)).isFalse();
    }

    @Test
    public void testEqualsSame() {
        final Object item = new Object();
        final NamedItem<Object> namedItemA = new NamedItem<>("zIIRUo8lPz", item);
        final NamedItem<Object> namedItemB = new NamedItem<>("zIIRUo8lPz", item);

        assertThat(namedItemA.equals(namedItemB)).isTrue();
    }


    @Test
    public void testHashCodeSelf() {
        final NamedItem<Object> namedItem = new NamedItem<>("2UCKZ9kZ7o", new Object());

        assertThat(namedItem.hashCode()).isEqualTo(namedItem.hashCode());
    }

    @Test
    public void testHashCodeOtherType() {
        final ArrayList realItem = new ArrayList();
        final NamedItem<RandomAccess> namedItemA = new NamedItem<>("vGi5P7o96T", realItem);
        final NamedItem<Cloneable> namedItemB = new NamedItem<>("vGi5P7o96T", realItem);

        assertThat(namedItemA.hashCode()).isEqualTo(namedItemB.hashCode());
    }

    @Test
    public void testHashCodeOtherName() {
        final Object item = new Object();
        final NamedItem<Object> namedItemA = new NamedItem<>("DpMSRx40TF", item);
        final NamedItem<Object> namedItemB = new NamedItem<>("jcnHjZQlOq", item);

        assertThat(namedItemA.hashCode()).isNotEqualTo(namedItemB.hashCode());
    }

    @Test
    public void testHashCodeOtherItem() {
        final NamedItem<Object> namedItemA = new NamedItem<>("iMmu1w7Doi", new Object());
        final NamedItem<Object> namedItemB = new NamedItem<>("iMmu1w7Doi", new Object());

        assertThat(namedItemA.hashCode()).isNotEqualTo(namedItemB.hashCode());
    }

    @Test
    public void testHashCodeSame() {
        final Object item = new Object();
        final NamedItem<Object> namedItemA = new NamedItem<>("hglHcOsYu0", item);
        final NamedItem<Object> namedItemB = new NamedItem<>("hglHcOsYu0", item);

        assertThat(namedItemA.hashCode()).isEqualTo(namedItemB.hashCode());
    }
}
