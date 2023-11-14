package com.fwdekker.randomness.template

import com.fwdekker.randomness.Settings
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.DynamicActionConfigurationCustomizer


/**
 * Registers, replaces, and unregisters actions for the user's [Template]s so that they can be inserted using shortcuts.
 *
 * @param getTemplates shorthand to return all the user's stored [Template]s
 */
open class TemplateActionLoader(
    private val getTemplates: () -> List<Template> = { Settings.DEFAULT.templates },
) : DynamicActionConfigurationCustomizer {
    /**
     * Registers the actions for all [Template]s in the user's [Settings] using [actionManager].
     */
    override fun registerActions(actionManager: ActionManager) {
        getTemplates().forEach { registerAction(actionManager, it) }
    }

    /**
     * Unregisters the actions of all [Template]s in the user's [Settings] using [actionManager].
     */
    override fun unregisterActions(actionManager: ActionManager) {
        getTemplates().forEach { unregisterAction(actionManager, it) }
    }


    /**
     * Registers, unregisters, and updates actions as appropriate for a transition from [oldList] to [newList] using
     * [actionManager].
     */
    fun updateActions(
        oldList: List<Template>,
        newList: List<Template>,
        actionManager: ActionManager = ActionManager.getInstance(),
    ) {
        val newUuidList = newList.map { it.uuid }
        oldList.filterNot { it.uuid in newUuidList }.forEach { unregisterAction(actionManager, it) }
        newList.forEach { registerAction(actionManager, it) }
    }


    /**
     * Returns all variant actions belonging to [template].
     */
    private fun getActions(template: Template) =
        mapOf(
            template.actionId to TemplateInsertAction(template, array = false, repeat = false),
            "${template.actionId}.array" to TemplateInsertAction(template, array = true, repeat = false),
            "${template.actionId}.repeat" to TemplateInsertAction(template, array = false, repeat = true),
            "${template.actionId}.repeat.array" to TemplateInsertAction(template, array = true, repeat = true),
            "${template.actionId}.settings" to TemplateSettingsAction(template)
        )

    /**
     * Registers the actions associated with [template] using [actionManager].
     */
    private fun registerAction(actionManager: ActionManager, template: Template) =
        getActions(template).forEach { (actionId, action) ->
            if (actionManager.getAction(actionId) == null)
                actionManager.registerAction(actionId, action)
            else
                actionManager.replaceAction(actionId, action)
        }

    /**
     * Unregisters the actions associated with [template] using [actionManager].
     */
    private fun unregisterAction(actionManager: ActionManager, template: Template) =
        getActions(template).forEach { (actionId, _) -> actionManager.unregisterAction(actionId) }
}

/**
 * Constructor-less version of [TemplateActionLoader], as is required in `plugin.xml`.
 */
class DefaultTemplateActionLoader : TemplateActionLoader()
