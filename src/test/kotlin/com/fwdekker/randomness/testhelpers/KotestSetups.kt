package com.fwdekker.randomness.testhelpers

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.TestConfiguration
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager


/**
 * Installs the [FailOnThreadViolationRepaintManager] on this scope, and uninstalls it at the end of this scope.
 */
fun TestConfiguration.useEdtViolationDetection() {
    tags(Tags.SWING)


    beforeSpec {
        FailOnThreadViolationRepaintManager.install()
    }

    afterSpec {
        FailOnThreadViolationRepaintManager.uninstall()
    }
}

/**
 * Sets up a bare [IdeaTestFixture] for each single test in this scope, and tears it down at the end of each such test.
 */
fun TestConfiguration.useBareIdeaFixture() {
    tags(Tags.IDEA_FIXTURE)


    lateinit var ideaFixture: IdeaTestFixture


    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()
    }

    afterNonContainer {
        ideaFixture.tearDown()
    }
}
