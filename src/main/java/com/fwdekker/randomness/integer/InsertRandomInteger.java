package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.array.ArraySettings;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;


/**
 * Generates a random integer based on the settings in {@link IntegerSettings}.
 */
public final class InsertRandomInteger extends InsertRandomSomething {
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
     * @param arraySettings   the settings to use for generating arrays
     * @param integerSettings the settings to use for generating integers
     */
    InsertRandomInteger(final @NotNull ArraySettings arraySettings, final @NotNull IntegerSettings integerSettings) {
        super(arraySettings);

        this.integerSettings = integerSettings;
    }


    @Override
    protected String getName() {
        return "Insert Random Integer";
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new IntegerSettingsAction();
    }

    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    @Override
    public String generateString() {
        final long randomValue = ThreadLocalRandom.current()
                .nextLong(integerSettings.getMinValue(), integerSettings.getMaxValue() + 1);

        return convertToString(randomValue);
    }


    /**
     * Returns a nicely formatted representation of a long.
     *
     * @param integer a {@code long}
     * @return a nicely formatted representation of a long
     */
    private String convertToString(final long integer) {
        final DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(integerSettings.getGroupingSeparator() != '\0');

        final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(integerSettings.getGroupingSeparator());
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setDecimalFormatSymbols(symbols);

        return format.format(integer);
    }
}
