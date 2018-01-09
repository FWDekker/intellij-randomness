package com.fwdekker.randomness;

import com.fwdekker.randomness.array.ArraySettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.util.ArrayList;
import java.util.List;


public abstract class InsertRandomSomethingArray extends InsertRandomSomething {
    private final ArraySettings arraySettings;
    private final InsertRandomSomething insertRandomSomething;


    /**
     * Constructs a new {@code InsertRandomSomethingArray} that uses the singleton {@code ArraySettings} instance.
     */
    public InsertRandomSomethingArray(final InsertRandomSomething insertRandomSomething) {
        this.arraySettings = ArraySettings.getInstance();
        this.insertRandomSomething = insertRandomSomething;
    }

    /**
     * Constructs a new {@code InsertRandomSomethingArray} that uses the given {@code ArraySettings} instance.
     *
     * @param arraySettings the settings to use for generating arrays
     */
    public InsertRandomSomethingArray(final ArraySettings arraySettings, final InsertRandomSomething insertRandomSomething) {
        this.arraySettings = arraySettings;
        this.insertRandomSomething = insertRandomSomething;
    }


    /**
     * Generates a random string based on the given {@link AnActionEvent}.
     * <p>
     * In particular, it selects whether to generate a single string or an array of strings.
     *
     * @param event the performed action
     * @return a random string based on the given {@link AnActionEvent}
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
