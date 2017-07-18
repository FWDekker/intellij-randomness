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
        final NamedItem namedItem = new NamedItem("OI6d7AkKye", new Object());

        assertThat(namedItem.getName()).isEqualTo("OI6d7AkKye");
    }

    @Test
    public void testItem() {
        final Object item = new Object();

        final NamedItem namedItem = new NamedItem("mrOz7tVmoX", item);

        assertThat(namedItem.getItem()).isEqualTo(item);
    }

    @Test
    public void testToString() {
        final NamedItem namedItem = new NamedItem("2Z6oCfzfli", new Object());

        assertThat(namedItem.toString()).isEqualTo("2Z6oCfzfli");
    }


    @Test
    public void testEqualsSelf() {
        final NamedItem namedItem = new NamedItem("B4md9THj3y", new Object());

        assertThat(namedItem.equals(namedItem)).isTrue();
    }

    @Test
    public void testEqualsNull() {
        final NamedItem namedItem = new NamedItem("c5pgKxqS41", new Object());

        assertThat(namedItem.equals(null)).isFalse();
    }

    @Test
    public void testEqualsOtherClass() {
        final NamedItem namedItem = new NamedItem("lGdlNMLoyt", new Object());

        assertThat(namedItem.equals(new Object())).isFalse();
    }

    @Test
    public void testEqualsOtherType() {
        final ArrayList realItem = new ArrayList();
        final RandomAccess itemA = realItem;
        final Cloneable itemB = realItem;
        final NamedItem namedItemA = new NamedItem("znKHUZqExm", itemA);
        final NamedItem namedItemB = new NamedItem("znKHUZqExm", itemB);

        assertThat(namedItemA.equals(namedItemB)).isTrue();
    }

    @Test
    public void testEqualsOtherName() {
        final Object item = new Object();
        final NamedItem namedItemA = new NamedItem("osEjuXELqh", item);
        final NamedItem namedItemB = new NamedItem("BOGVSgfrjF", item);

        assertThat(namedItemA.equals(namedItemB)).isFalse();
    }

    @Test
    public void testEqualsOtherItem() {
        final NamedItem namedItemA = new NamedItem("iMmu1w7Doi", new Object());
        final NamedItem namedItemB = new NamedItem("iMmu1w7Doi", new Object());

        assertThat(namedItemA.equals(namedItemB)).isFalse();
    }

    @Test
    public void testEqualsSame() {
        final Object item = new Object();
        final NamedItem namedItemA = new NamedItem("zIIRUo8lPz", item);
        final NamedItem namedItemB = new NamedItem("zIIRUo8lPz", item);

        assertThat(namedItemA.equals(namedItemB)).isTrue();
    }


    @Test
    public void testHashCodeSelf() {
        final NamedItem namedItem = new NamedItem("2UCKZ9kZ7o", new Object());

        assertThat(namedItem.hashCode()).isEqualTo(namedItem.hashCode());
    }

    @Test
    public void testHashCodeOtherType() {
        final ArrayList realItem = new ArrayList();
        final RandomAccess itemA = realItem;
        final Cloneable itemB = realItem;
        final NamedItem namedItemA = new NamedItem("vGi5P7o96T", itemA);
        final NamedItem namedItemB = new NamedItem("vGi5P7o96T", itemB);

        assertThat(namedItemA.hashCode()).isEqualTo(namedItemB.hashCode());
    }

    @Test
    public void testHashCodeOtherName() {
        final Object item = new Object();
        final NamedItem namedItemA = new NamedItem("DpMSRx40TF", item);
        final NamedItem namedItemB = new NamedItem("jcnHjZQlOq", item);

        assertThat(namedItemA.hashCode()).isNotEqualTo(namedItemB.hashCode());
    }

    @Test
    public void testHashCodeOtherItem() {
        final NamedItem namedItemA = new NamedItem("iMmu1w7Doi", new Object());
        final NamedItem namedItemB = new NamedItem("iMmu1w7Doi", new Object());

        assertThat(namedItemA.hashCode()).isNotEqualTo(namedItemB.hashCode());
    }

    @Test
    public void testHashCodeSame() {
        final Object item = new Object();
        final NamedItem namedItemA = new NamedItem("hglHcOsYu0", item);
        final NamedItem namedItemB = new NamedItem("hglHcOsYu0", item);

        assertThat(namedItemA.hashCode()).isEqualTo(namedItemB.hashCode());
    }
}
