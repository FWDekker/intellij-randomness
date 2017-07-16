package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.InsertRandomSomething;
import java.security.SecureRandom;
import java.util.Random;
import org.jetbrains.annotations.NotNull;


/**
 * Generates a random integer based on the settings in {@link IntegerSettings}.
 */
public final class InsertRandomInteger extends InsertRandomSomething {
    private static final Random RANDOM = new SecureRandom();

    private final IntegerSettings integerSettings;


    /**
     * Constructs a new {@code InsertRandomInteger} that uses the singleton {@code IntegerSettings} instance.
     */
    public InsertRandomInteger() {
        this.integerSettings = IntegerSettings.getInstance();
    }

    /**
     * Constructs a new {@code InsertRandomInteger} that uses the given {@code IntegerSettings} instance.
     *
     * @param integerSettings the settings to use for generating integers
     */
    InsertRandomInteger(@NotNull final IntegerSettings integerSettings) {
        this.integerSettings = integerSettings;
    }


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final int range = integerSettings.getMaxValue() - integerSettings.getMinValue();
        final int randomValue = integerSettings.getMinValue() + RANDOM.nextInt(range + 1);

        return Integer.toString(randomValue);
    }
}
