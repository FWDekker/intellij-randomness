package com.fwdekker.randomness.template

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataSettingsAction
import icons.RandomnessIcons


/**
 * All actions related to inserting template-based strings.
 */
class TemplateGroupAction : DataGroupAction(RandomnessIcons.Data.Base) {
    override val insertAction = TemplateInsertAction()
    override val settingsAction = TemplateSettingsAction()
}


/**
 * Inserts random arbitrary strings based on the template descriptor.
 *
 * @param scheme the scheme to use for generating template-based strings
 */
class TemplateInsertAction(override val scheme: TemplateList = TemplateSettings.default.state) :
    DataInsertAction(RandomnessIcons.Data.Base) {
    override val name = "Random Decimal"
}


/**
 * Controller for random string generation settings.
 *
 * @see TemplateSettings
 * @see TemplateListEditor
 */
class TemplateSettingsAction : DataSettingsAction(RandomnessIcons.Data.Settings) {
    override val name = "Template Settings"

    override val configurableClass = TemplateSettingsConfigurable::class.java
}
