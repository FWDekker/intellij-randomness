package com.fwdekker.randomness

import com.fwdekker.randomness.template.TemplateList
import com.fwdekker.randomness.template.TemplateSettings


/**
 * Contains references to various [Settings] objects.
 *
 * @property templateList The template list.
 */
data class SettingsState(
    var templateList: TemplateList = TemplateList(),
) : State() {
    override fun doValidate() = templateList.doValidate()

    override fun copyFrom(other: State) {
        require(other is SettingsState) { Bundle("shared.error.cannot_copy_from_different_type") }

        uuid = other.uuid
        templateList.copyFrom(other.templateList)
        templateList.applySettingsState(this)
    }

    /**
     * Returns a deep copy of this state and the contained [Settings] instances.
     *
     * Additionally invokes [TemplateList.applySettingsState] to ensure that all copied templates and schemes use the
     * new instances.
     *
     * @param retainUuid `false` if and only if the copy should have a different, new [uuid]
     * @return a deep copy of this scheme
     */
    override fun deepCopy(retainUuid: Boolean) =
        copy(templateList = templateList.deepCopy(retainUuid = retainUuid)).also {
            if (retainUuid) it.uuid = uuid

            it.templateList.applySettingsState(it)
        }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The persistent [SettingsState] instance.
         */
        val default: SettingsState by lazy { SettingsState(TemplateSettings.default.state) }
    }
}
