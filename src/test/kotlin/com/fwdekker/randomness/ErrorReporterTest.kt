package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain


/**
 * Unit tests for [ErrorReporter].
 */
object ErrorReporterTest : FunSpec({
    tags(NamedTag("IdeaFixture"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var reporter: ErrorReporter


    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        reporter = ErrorReporter()
    }

    afterNonContainer {
        ideaFixture.tearDown()
    }


    test("replaces whitespace with a plus") {
        reporter.getIssueUrl("lorem ipsum") shouldContain "lorem+ipsum"
    }

    test("escapes # and & in the additional info") {
        reporter.getIssueUrl("a#b") shouldNotContain "a#b"
        reporter.getIssueUrl("a&b") shouldNotContain "a&b"
    }

    test("asks the user to provide additional information if none was provided") {
        reporter.getIssueUrl(null) shouldContain "Please+describe+your+issue"
    }
})
