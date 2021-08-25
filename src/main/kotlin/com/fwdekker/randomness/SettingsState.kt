package com.fwdekker.randomness

import com.fwdekker.randomness.string.SymbolSetSettings
import com.fwdekker.randomness.template.TemplateList
import com.fwdekker.randomness.template.TemplateSettings
import com.fwdekker.randomness.word.DictionarySettings


/**
 * Contains references to various [Settings] objects.
 *
 * @property templateList The template list.
 * @property symbolSetSettings The symbol set settings.
 * @property dictionarySettings The dictionary settings.
 */
data class SettingsState(
    var templateList: TemplateList = TemplateList(emptyList()),
    var symbolSetSettings: SymbolSetSettings = SymbolSetSettings(emptyMap()),
    var dictionarySettings: DictionarySettings = DictionarySettings(emptySet())
) : State() {
    override fun doValidate() =
        templateList.doValidate() ?: symbolSetSettings.doValidate() ?: dictionarySettings.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(
            templateList = templateList.deepCopy(retainUuid = retainUuid),
            symbolSetSettings = symbolSetSettings.deepCopy(retainUuid = retainUuid),
            dictionarySettings = dictionarySettings.deepCopy(retainUuid = retainUuid)
        ).also { if (retainUuid) it.uuid = uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The persistent `SettingsState` instance.
         */
        val default by lazy {
            SettingsState(
                TemplateSettings.default.state,
                SymbolSetSettings.default,
                DictionarySettings.default
            )
        }
    }
}
