package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;


/**
 * Contains settings for generating random UUIDs.
 */
@State(
        name = "IntegerSettings",
        name = "UuidSettings",
        storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class UuidSettings extends Settings implements PersistentStateComponent<UuidSettings> {
    /**
     * Returns the singleton {@code UuidSettings} instance.
     *
     * @return the singleton {@code UuidSettings} instance
     */
    public static UuidSettings getInstance() {
        return ServiceManager.getService(UuidSettings.class);
    }

    @Override
    public UuidSettings getState() {
        return this;
    }

    @Override
    public void loadState(final @NotNull UuidSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
