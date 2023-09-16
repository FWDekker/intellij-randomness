package com.fwdekker.randomness.template

import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot


/**
 * Unit tests for [TemplateActionLoader].
 */
object TemplateActionLoaderTest : FunSpec({
    tags(NamedTag("IdeaFixture"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var actionManager: ActionManager
    lateinit var templates: MutableList<Template>
    lateinit var loader: TemplateActionLoader


    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        actionManager = ActionManager.getInstance()
        templates = mutableListOf()
        loader = TemplateActionLoader { templates }
    }

    afterNonContainer {
        ideaFixture.tearDown()
    }


    context("registerActions") {
        test("registers multiple insert action variants and a settings action") {
            val template = Template()
            templates += template

            loader.registerActions(actionManager)

            val actions = actionManager.getActionsWithPrefix(template.actionId)
            actions.filterIsInstance<TemplateInsertAction>() shouldHaveAtLeastSize 2
            actions.filterIsInstance<TemplateSettingsAction>() shouldNot beEmpty()
        }

        test("registers actions for each template") {
            val template1 = Template()
            val template2 = Template()
            templates += listOf(template1, template2)

            loader.registerActions(actionManager)

            actionManager.getActionsWithPrefix(template1.actionId) shouldNot beEmpty()
            actionManager.getActionsWithPrefix(template2.actionId) shouldNot beEmpty()
        }
    }

    context("unregisterActions") {
        test("unregisters actions for each template") {
            val template = Template()
            templates += template
            loader.registerActions(actionManager)

            loader.unregisterActions(actionManager)

            actionManager.getActionsWithPrefix(template.actionId) should beEmpty()
        }
    }

    context("updateActions") {
        test("registers actions of initial templates") {
            val template = Template()

            loader.updateActions(emptyList(), listOf(template))

            actionManager.getActionsWithPrefix(template.actionId) shouldNot beEmpty()
        }

        test("registers actions of new templates") {
            val template1 = Template()
            templates += template1
            loader.registerActions(actionManager)

            val template2 = Template()
            loader.updateActions(listOf(template1), listOf(template1, template2))

            actionManager.getActionsWithPrefix(template1.actionId) shouldNot beEmpty()
            actionManager.getActionsWithPrefix(template2.actionId) shouldNot beEmpty()
        }

        test("unregisters actions of now-removed templates") {
            val template1 = Template()
            val template2 = Template()
            templates += listOf(template1, template2)
            loader.registerActions(actionManager)

            loader.updateActions(listOf(template1, template2), emptyList())

            actionManager.getActionsWithPrefix(template1.actionId) should beEmpty()
            actionManager.getActionsWithPrefix(template2.actionId) should beEmpty()
        }

        test("re-registers actions of updated templates") {
            val template = Template()
            templates += template
            loader.registerActions(actionManager)
            val oldActions = actionManager.getActionsWithPrefix(template.actionId)

            loader.updateActions(listOf(template), emptyList())

            actionManager.getActionsWithPrefix(template.actionId) shouldNotContainAnyOf oldActions
        }
    }
})


/**
 * Returns all actions with an id that starts with [prefix].
 */
private fun ActionManager.getActionsWithPrefix(prefix: String) = getActionIdList(prefix).map { getAction(it) }
