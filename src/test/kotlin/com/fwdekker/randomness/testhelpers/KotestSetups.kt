package com.fwdekker.randomness.testhelpers

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.scopes.ContainerScope
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager


/**
 * Installs the [FailOnThreadViolationRepaintManager] on this scope, and uninstalls it at the end of this scope.
 *
 * @param addTags `true` if and only if tags should be added
 */
fun TestConfiguration.useEdtViolationDetection(addTags: Boolean = true) {
    if (addTags) tags(Tags.SWING)


    beforeEach {
        FailOnThreadViolationRepaintManager.install()
    }

    afterEach {
        FailOnThreadViolationRepaintManager.uninstall()
    }
}

/**
 * Sets up a bare [IdeaTestFixture] for each single test in this scope, and tears it down at the end of each such test.
 *
 * Use this instead of [useSharedBareIdeaFixture] if you need strict isolation between the fixtures of each test.
 */
fun TestConfiguration.useIsolatedBareIdeaFixture() {
    tags(Tags.IDEA_FIXTURE)


    lateinit var ideaFixture: IdeaTestFixture


    useEdtViolationDetection(addTags = true)

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterEach {
        ideaFixture.tearDown()
    }
}

/**
 * Like [useIsolatedBareIdeaFixture], except only one instance is created and torn down for the entire current spec.
 *
 * Use this if you only need the IDEA fixture to be initialized, but don't need strict isolation between the fixtures of
 * each test.
 */
fun TestConfiguration.useSharedBareIdeaFixture() {
    tags(Tags.IDEA_FIXTURE)


    lateinit var ideaFixture: IdeaTestFixture


    useEdtViolationDetection(addTags = true)

    beforeSpec {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterSpec {
        ideaFixture.tearDown()
    }
}

/**
 * Like [useSharedBareIdeaFixture], except the fixture is shared only with the current context.
 *
 * No tag is added because tags cannot be changed after entering the context.
 */
fun ContainerScope.useCtxSharedBareIdeaFixture() {
    FailOnThreadViolationRepaintManager.install()

    val ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
    ideaFixture.setUp()

    afterScope {
        ideaFixture.tearDown()
        FailOnThreadViolationRepaintManager.uninstall()
    }
}
