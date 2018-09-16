package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.DataInsertAction;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


/**
 * Generates a random type 4 UUID.
 */
public final class UuidInsertAction extends DataInsertAction {
    private final UuidSettings uuidSettings;


    /**
     * Constructs a new {@code UuidInsertAction} that uses the singleton {@code UuidSettings} instance.
     */
    public UuidInsertAction() {
        this.uuidSettings = UuidSettings.getInstance();
    }

    /**
     * Constructs a new {@code UuidInsertAction} that uses the given {@code UuidSettings} instance.
     *
     * @param uuidSettings the settings to use for generating UUIDs
     */
    UuidInsertAction(final @NotNull UuidSettings uuidSettings) {
        this.uuidSettings = uuidSettings;
    }


    @Override
    protected String getName() {
        return "Insert UUID";
    }

    @Override
    protected String generateString() {
        return uuidSettings.getEnclosure() + UUID.randomUUID().toString() + uuidSettings.getEnclosure();
    }


    /**
     * Inserts an array of UUIDs.
     */
    public final class ArrayAction extends DataInsertAction.ArrayAction {
        /**
         * Constructs a new {@code ArrayAction} for UUIDs.
         */
        public ArrayAction() {
            super(UuidInsertAction.this);
        }


        @Override
        protected String getName() {
            return "Insert UUID Array";
        }
    }
}
