package com.fwdekker.randomness

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [ErrorReporter].
 */
object ErrorReporterTest : Spek({
    describe("error reporter") {
        lateinit var ideaFixture: IdeaTestFixture
        lateinit var reporter: ErrorReporter


        beforeEachTest {
            ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
            ideaFixture.setUp()

            reporter = ErrorReporter()
        }

        afterEachTest {
            ideaFixture.tearDown()
        }


        it("replaces whitespace with a plus") {
            assertThat(reporter.getIssueUrl("safe verb")).contains("safe+verb")
        }

        it("escapes # and & in the additional info") {
            assertThat(reporter.getIssueUrl("a&b")).doesNotContain("a&b")
            assertThat(reporter.getIssueUrl("a#b")).doesNotContain("a#b")
        }

        it("asks the user to provide additional information if none was provided") {
            assertThat(reporter.getIssueUrl(null)).contains("Please+describe+your+issue")
        }
    }
})
