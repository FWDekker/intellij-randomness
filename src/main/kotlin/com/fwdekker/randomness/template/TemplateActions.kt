package com.fwdekker.randomness.template

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataInsertRepeatArrayAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArraySettings
import icons.RandomnessIcons


/**
 * All actions related to inserting template-based strings.
 */
class TemplateGroupAction : DataGroupAction(RandomnessIcons.Data.Base) {
    override val insertAction = TemplateInsertAction()
    override val insertArrayAction = TemplateInsertAction.ArrayAction()
    override val insertRepeatAction = TemplateInsertAction.RepeatAction()
    override val insertRepeatArrayAction = TemplateInsertAction.RepeatArrayAction()
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
        scheme.templates.entries.first().value.also { it.random = random }.generateStrings(count)

    /**
     * Inserts an array-like string of template-based strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class ArrayAction(
        arrayScheme: ArraySettings = ArraySettings.default,
        scheme: TemplateSettings = TemplateSettings.default
    ) : DataInsertArrayAction(arrayScheme, TemplateInsertAction(scheme), RandomnessIcons.Data.Array) {
        override val name = "Random Template Array"
    }

    /**
     * Inserts repeated random template-based strings.
     *
     * @param scheme the settings to use for generating strings
     */
    class RepeatAction(scheme: TemplateSettings = TemplateSettings.default) :
        DataInsertRepeatAction(TemplateInsertAction(scheme), RandomnessIcons.Data.Repeat) {
        override val name = "Random Repeated Template"
    }

    /**
     * Inserts repeated array-like strings of template-based strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class RepeatArrayAction(
        arrayScheme: ArraySettings = ArraySettings.default,
        scheme: TemplateSettings = TemplateSettings.default
    ) : DataInsertRepeatArrayAction(ArrayAction(arrayScheme, scheme), RandomnessIcons.Data.RepeatArray) {
        override val name = "Random Repeated Template Array"
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
