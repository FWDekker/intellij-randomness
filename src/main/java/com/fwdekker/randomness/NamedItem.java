package com.fwdekker.randomness;

import java.util.Objects;


/**
 * Associates an object with a string.
 *
 * @param <T> the type of the object
 */
public final class NamedItem<T> {
    /**
     * The name of the item.
     */
    private final String name;
    /**
     * The item.
     */
    private final T item;


    /**
     * Constructs a new {@code NamedItem}.
     *
     * @param name the name of the item
     * @param item the item
     */
    public NamedItem(final String name, final T item) {
        this.name = name;
        this.item = item;
    }


    /**
     * Returns the name of the item.
     *
     * @return the name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the item.
     *
     * @return the item
     */
    public T getItem() {
        return item;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final NamedItem<?> that = (NamedItem<?>) other;
        return this.name.equals(that.name) && this.item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, item);
    }
}
