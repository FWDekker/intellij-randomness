package com.fwdekker.randomness.template

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.DynamicActionConfigurationCustomizer


/**
 * Registers and unregisters actions for the user's [Template]s so that they can be inserted using shortcuts.
 */
object TemplateActionLoader : DynamicActionConfigurationCustomizer {
    /**
     * Shorthand to return all the user's stored [Template]s.
     */
    private val templates: List<Template>
        get() = TemplateSettings.default.state.templates


    /**
     * Registers the actions for all [Template]s in the user's [TemplateSettings].
     *
     * @param actionManager the manager to register actions through
     */
    override fun registerActions(actionManager: ActionManager) {
        templates.forEach { registerAction(actionManager, it) }
    }

    /**
     * Unregisters the actions of all [Template]s in the user's [TemplateSettings].
     *
     * @param actionManager the manager to unregister actions through
     */
    override fun unregisterActions(actionManager: ActionManager) {
        templates.forEach { unregisterAction(actionManager, it) }
    }


    /**
     * Registers, unregisters, and updates actions as appropriate for a transition from [oldList] to [newList].
     *
     * @param oldList the list of stored [Template]s before storing [newList]
     * @param newList the list of stored [Template]s after storing them
     */
    fun updateActions(oldList: Set<Template>, newList: Set<Template>) {
        val actionManager = ActionManager.getInstance()

        val newUuids = newList.map { it.uuid }
        oldList.filterNot { it.uuid in newUuids }.forEach {
            if (actionManager.getAction(it.actionId) != null)
                actionManager.unregisterAction(it.actionId)
        }

        newList.forEach {
            if (actionManager.getAction(it.actionId) == null)
                registerAction(actionManager, it)
            else
                replaceAction(actionManager, it)
        }
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
            actionManager.registerAction(actionId, action)
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

    /**
     * Replaces the actions associated with [template].
     *
     * @param actionManager the manager to replace actions through
     * @param template the [Template] to replace actions for
     */
    private fun replaceAction(actionManager: ActionManager, template: Template) =
        getActions(template).forEach { (actionId, action) -> actionManager.replaceAction(actionId, action) }
}
