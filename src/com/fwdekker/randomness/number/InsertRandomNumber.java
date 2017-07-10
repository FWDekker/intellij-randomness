package com.fwdekker.randomness.number;

import com.fwdekker.randomness.InsertRandomSomething;

import java.util.Random;


/**
 * Generates a random number based on the settings in {@link NumberSettings}.
 */
final class InsertRandomNumber extends InsertRandomSomething {
    private static final Random RANDOM = new Random();


    /**
     * Returns a random number between the minimum and maximum value, inclusive.
     *
     * @return a random number between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final int range = NumberSettings.getMaxValue() - NumberSettings.getMinValue();
        final int randomValue = NumberSettings.getMinValue() + RANDOM.nextInt(range + 1);

        return Integer.toString(randomValue);
    }
}
