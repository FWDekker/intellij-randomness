package com.fwdekker.randomness.template

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [TemplateActionLoader].
 */
class TemplateActionLoaderTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var actionManager: ActionManager


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        actionManager = ActionManager.getInstance()
    }

    afterEachTest {
        ideaFixture.tearDown()
    }


    describe("registerActions") {
        it("registers actions for each template") {
            val template = Template(name = "Snow")
            TemplateSettings.default.loadState(TemplateList(listOf(template)))

            TemplateActionLoader().registerActions(actionManager)

            assertThat(actionManager.getAction(template.actionId)).isNotNull()
        }
    }

    describe("unregisterActions") {
        it("unregisters actions for each template") {
            val template = Template(name = "Kick")
            TemplateSettings.default.loadState(TemplateList(listOf(template)))

            TemplateActionLoader().registerActions(actionManager)
            assertThat(actionManager.getAction(template.actionId)).isNotNull()
            TemplateActionLoader().unregisterActions(actionManager)

            assertThat(actionManager.getAction(template.actionId)).isNull()
        }
    }

    describe("updateActions") {
        it("registers actions of initial templates") {
            val template = Template(name = "Fine")

            TemplateActionLoader().updateActions(emptySet(), setOf(template))

            assertThat(actionManager.getAction(template.actionId)).isNotNull()
        }

        it("registers actions of new templates") {
            val template1 = Template(name = "Ease")
            val template2 = Template(name = "Holy")

            TemplateActionLoader().updateActions(emptySet(), setOf(template1))
            TemplateActionLoader().updateActions(setOf(template1, template2), setOf(template2))

            assertThat(actionManager.getAction(template2.actionId)).isNotNull()
        }

        it("unregisters actions of now-removed templates") {
            val template = Template(name = "Fall")

            TemplateActionLoader().updateActions(emptySet(), setOf(template))
            TemplateActionLoader().updateActions(setOf(template), emptySet())

            assertThat(actionManager.getAction(template.actionId)).isNull()
        }

        it("reregisters actions of updated templates") {
            val template = Template(name = "Pain")

            TemplateActionLoader().updateActions(emptySet(), setOf(template))
            val action = actionManager.getAction(template.actionId)
            TemplateActionLoader().updateActions(setOf(template), setOf(template))

            assertThat(actionManager.getAction(template.actionId)).isNotEqualTo(action)
        }
    }
})
