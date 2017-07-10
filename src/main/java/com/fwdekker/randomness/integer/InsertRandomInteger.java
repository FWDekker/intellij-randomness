package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.InsertRandomSomething;

import java.util.Random;


/**
 * Generates a random integer based on the settings in {@link IntegerSettings}.
 */
final class InsertRandomInteger extends InsertRandomSomething {
    private static final Random RANDOM = new Random();


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final int range = IntegerSettings.getMaxValue() - IntegerSettings.getMinValue();
        final int randomValue = IntegerSettings.getMinValue() + RANDOM.nextInt(range + 1);

        return Integer.toString(randomValue);
    }
}
