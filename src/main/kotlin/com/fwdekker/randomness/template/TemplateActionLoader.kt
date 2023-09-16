package com.fwdekker.randomness.template

import com.fwdekker.randomness.PersistentSettings
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.DynamicActionConfigurationCustomizer


/**
 * Registers, replaces, and unregisters actions for the user's [Template]s so that they can be inserted using shortcuts.
 *
 * @property getTemplates shorthand to return all the user's stored [Template]s
 */
open class TemplateActionLoader(
    private val getTemplates: () -> List<Template> = { PersistentSettings.default.state.templates },
) : DynamicActionConfigurationCustomizer {
    /**
     * Registers the actions for all [Template]s in the user's [PersistentSettings].
     *
     * @param actionManager the manager to register actions through
     */
    override fun registerActions(actionManager: ActionManager) {
        getTemplates().forEach { registerAction(actionManager, it) }
    }

    /**
     * Unregisters the actions of all [Template]s in the user's [PersistentSettings].
     *
     * @param actionManager the manager to unregister actions through
     */
    override fun unregisterActions(actionManager: ActionManager) {
        getTemplates().forEach { unregisterAction(actionManager, it) }
    }


    /**
     * Registers, unregisters, and updates actions as appropriate for a transition from [oldList] to [newList].
     *
     * @param oldList the list of stored [Template]s before storing [newList]
     * @param newList the list of stored [Template]s after storing them
     * @param actionManager the manager with which actions are (de)registered
     */
    fun updateActions(
        oldList: List<Template>,
        newList: List<Template>,
        actionManager: ActionManager = ActionManager.getInstance(),
    ) {
        oldList.filterNot { it in newList }.forEach { unregisterAction(actionManager, it) }
        newList.forEach { registerAction(actionManager, it) }
    }


    /**
     * Returns all variant actions belonging to [template].
     *
     * @param template the template to get actions for
     * @return all variant actions belonging to [template]
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
     * Registers the actions associated with [template].
     *
     * @param actionManager the manager to register actions through
     * @param template the [Template] to register actions for
     */
    private fun registerAction(actionManager: ActionManager, template: Template) =
        getActions(template).forEach { (actionId, action) ->
            if (actionManager.getAction(actionId) == null)
                actionManager.registerAction(actionId, action)
            else
                actionManager.replaceAction(actionId, action)
        }

    /**
     * Unregisters the actions associated with [template].
     *
     * @param actionManager the manager to unregister actions through
     * @param template the [Template] to unregister actions for
     */
    private fun unregisterAction(actionManager: ActionManager, template: Template) =
        getActions(template).forEach { (actionId, _) -> actionManager.unregisterAction(actionId) }
}

/**
 * Constructor-less version of [TemplateActionLoader], as is required in `plugin.xml`.
 */
class DefaultTemplateActionLoader : TemplateActionLoader()
