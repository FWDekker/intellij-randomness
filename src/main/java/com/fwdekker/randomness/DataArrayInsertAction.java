package com.fwdekker.randomness;

import com.fwdekker.randomness.array.ArraySettings;

import java.util.ArrayList;
import java.util.List;


/**
 * Inserts a randomly generated array of strings at the positions of the event's editor's carets.
 */
public abstract class DataArrayInsertAction extends DataInsertAction {
    private final ArraySettings arraySettings;
    private final DataInsertAction dataInsertAction;


    /**
     * Constructs a new {@code DataArrayInsertAction} that uses the singleton {@code ArraySettings} instance.
     *
     * @param dataInsertAction the action to generate data with
     */
    public DataArrayInsertAction(final DataInsertAction dataInsertAction) {
        this.arraySettings = ArraySettings.getInstance();
        this.dataInsertAction = dataInsertAction;
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
            strings.add(dataInsertAction.generateString());
        }

        return arraySettings.arrayify(strings);
    }
}
