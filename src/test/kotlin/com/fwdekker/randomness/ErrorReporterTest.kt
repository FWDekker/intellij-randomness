package com.fwdekker.randomness

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat


/**
 * Unit tests for [ErrorReporter].
 */
object ErrorReporterTest : DescribeSpec({
    describe("error reporter") {
        lateinit var ideaFixture: IdeaTestFixture
        lateinit var reporter: ErrorReporter


        beforeEach {
            ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
            ideaFixture.setUp()

            reporter = ErrorReporter()
        }

        afterEach {
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
