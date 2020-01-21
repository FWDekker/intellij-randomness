package com.fwdekker.randomness.array

import com.fwdekker.randomness.DataQuickSwitchSchemeAction
import com.fwdekker.randomness.DataSettingsAction
import icons.RandomnessIcons


/**
 * Controller for random array generation settings.
 *
 * @see ArraySettings
 * @see ArraySettingsComponent
 */
class ArraySettingsAction : DataSettingsAction() {
    override val name = "Array Settings"

    override val configurableClass = ArraySettingsConfigurable::class.java


    /**
     * Opens a popup to allow the user to quickly switch to the selected scheme.
     *
     * @param settings the settings containing the schemes that can be switched between
     */
    class ArrayQuickSwitchSchemeAction(settings: ArraySettings = ArraySettings.default) :
        DataQuickSwitchSchemeAction<ArrayScheme>(settings, RandomnessIcons.Data.QuickSwitchScheme) {
        override val name = "Quick Switch Array Scheme"
    }
}
