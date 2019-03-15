package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.Settings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;


/**
 * Contains settings for generating random UUIDs.
 */
@State(
    name = "UuidSettings",
    storages = @Storage("$APP_CONFIG$/randomness.xml")
)
public final class UuidSettings implements Settings<UuidSettings> {
    /**
     * The string that encloses the generated UUID on both sides.
     */
    private String enclosure = "\"";


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


    /**
     * Returns the string that encloses the generated UUID on both sides.
     *
     * @return the string that encloses the generated UUID on both sides
     */
    public String getEnclosure() {
        return enclosure;
    }

    /**
     * Sets the string that encloses the generated UUID on both sides.
     *
     * @param enclosure the string that encloses the generated UUID on both sides
     */
    public void setEnclosure(final String enclosure) {
        this.enclosure = enclosure;
    }
}
