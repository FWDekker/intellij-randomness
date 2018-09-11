package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.DataInsertAction;

import java.util.UUID;


/**
 * Generates a random type 4 UUID.
 */
public final class UuidInsertAction extends DataInsertAction {
    @Override
    protected String getName() {
        return "Insert UUID";
    }

    @Override
    protected String generateString() {
        return UUID.randomUUID().toString();
    }
}
