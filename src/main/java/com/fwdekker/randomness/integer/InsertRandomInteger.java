package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.InsertRandomSomething;

import java.security.SecureRandom;
import java.util.Random;


/**
 * Generates a random integer based on the settings in {@link IntegerSettings}.
 */
final class InsertRandomInteger extends InsertRandomSomething {
    private static final Random RANDOM = new SecureRandom();

    private final IntegerSettings integerSettings = IntegerSettings.getInstance();


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
