package com.fwdekker.randomness.template

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataSettingsAction
import icons.RandomnessIcons


/**
 * All actions related to inserting template-based strings.
 */
class TemplateGroupAction : DataGroupAction(RandomnessIcons.Data.Base) {
    override val insertAction = TemplateInsertAction()
    override val insertRepeatAction = TemplateInsertAction.RepeatAction()
    override val settingsAction = TemplateSettingsAction()
}


/**
 * Inserts random arbitrary strings based on the template descriptor.
 *
 * @param scheme the scheme to use for generating template-based strings
 */
class TemplateInsertAction(private val scheme: TemplateSettings = TemplateSettings.default) :
    DataInsertAction(RandomnessIcons.Data.Base) {
    override val name = "Random Decimal"


    /**
     * Returns random template-based strings based on the descriptor.
     *
     * @param count the number of strings to generate
     * @return random template-based strings based on the descriptor
     */
    override fun generateStrings(count: Int) =
        scheme.templates.first().also { it.random = random }.generateStrings(count)

    /**
     * Inserts repeated random template-based strings.
     *
     * @param scheme the settings to use for generating strings
     */
    class RepeatAction(scheme: TemplateSettings = TemplateSettings.default) :
        DataInsertRepeatAction(TemplateInsertAction(scheme), RandomnessIcons.Data.Repeat) {
        override val name = "Random Repeated Template"
    }
}


/**
 * Controller for random string generation settings.
 *
 * @see TemplateSettings
 * @see TemplateSettingsEditor
 */
class TemplateSettingsAction : DataSettingsAction(RandomnessIcons.Data.Settings) {
    override val name = "Template Settings"

    override val configurableClass = TemplateSettingsConfigurable::class.java
}
