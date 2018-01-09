package com.fwdekker.randomness;

import com.fwdekker.randomness.array.ArraySettings;
import java.util.ArrayList;
import java.util.List;


/**
 * Inserts a randomly generated array of strings at the positions of the event's editor's carets.
 */
public abstract class InsertRandomSomethingArray extends InsertRandomSomething {
    private final ArraySettings arraySettings;
    private final InsertRandomSomething insertRandomSomething;


    /**
     * Constructs a new {@code InsertRandomSomethingArray} that uses the singleton {@code ArraySettings} instance.
     *
     * @param insertRandomSomething the action to generate data with
     */
    public InsertRandomSomethingArray(final InsertRandomSomething insertRandomSomething) {
        this.arraySettings = ArraySettings.getInstance();
        this.insertRandomSomething = insertRandomSomething;
    }

    /**
     * Constructs a new {@code InsertRandomSomethingArray} that uses the given {@code ArraySettings} instance.
     *
     * @param insertRandomSomething the action to generate data with
     * @param arraySettings         the settings to use for generating arrays
     */
    public InsertRandomSomethingArray(final InsertRandomSomething insertRandomSomething,
                                      final ArraySettings arraySettings) {
        this.arraySettings = arraySettings;
        this.insertRandomSomething = insertRandomSomething;
    }


    /**
     * Generates a random array of strings.
     *
     * @return a random array of strings
     */
    @Override
    protected final String generateString() {
        final List<String> strings = new ArrayList<>();

        for (int i = 0; i < arraySettings.getCount(); i++) {
            strings.add(insertRandomSomething.generateString());
        }

        return arraySettings.arrayify(strings);
    }
}
