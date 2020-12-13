package com.fwdekker.randomness

import com.intellij.openapi.diagnostic.IdeaLoggingEvent
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


        describe("issue url") {
            it("includes all components if they fit in the URL") {
                val url = reporter.getIssueUrl(
                    arrayOf(IdeaLoggingEvent("", Exception("EXCEPTION"))),
                    "ADDITIONAL_INFO"
                )

                assertThat(url)
                    .contains("ADDITIONAL_INFO")
                    .contains("EXCEPTION")
                    .contains("Randomness+version")
            }

            it("excludes the stacktraces if they are too long") {
                val url = reporter.getIssueUrl(
                    arrayOf(IdeaLoggingEvent("", Exception("EXCEPTION_${List(10000) { "A" }}"))),
                    "ADDITIONAL_INFO"
                )

                assertThat(url)
                    .contains("ADDITIONAL_INFO")
                    .doesNotContain("EXCEPTION")
                    .contains("Randomness+version")
            }

            it("excludes the additional info if they are too long") {
                val url = reporter.getIssueUrl(
                    arrayOf(IdeaLoggingEvent("", Exception("EXCEPTION"))),
                    "ADDITIONAL_INFO_${List(10000) { "A" }}"
                )

                assertThat(url)
                    .doesNotContain("ADDITIONAL_INFO")
                    .contains("EXCEPTION")
                    .contains("Randomness+version")
            }

            it("excludes both additional info and stacktraces if either is too long") {
                val url = reporter.getIssueUrl(
                    arrayOf(IdeaLoggingEvent("", Exception("EXCEPTION_${List(10000) { "A" }}"))),
                    "ADDITIONAL_INFO_${List(10000) { "A" }}"
                )

                assertThat(url)
                    .doesNotContain("ADDITIONAL_INFO")
                    .doesNotContain("EXCEPTION")
                    .contains("Randomness+version")
            }

            describe("encoding") {
                it("includes all parts that are not expanded due to encoding") {
                    val url = reporter.getIssueUrl(
                        arrayOf(IdeaLoggingEvent("", Exception("EXCEPTION"))),
                        "ADDITIONAL_INFO_${List(1000) { "A" }}"
                    )

                    assertThat(url)
                        .contains("ADDITIONAL_INFO")
                        .contains("EXCEPTION")
                        .contains("Randomness+version")
                }

                it("does not expand whitespace") {
                    val url = reporter.getIssueUrl(
                        arrayOf(IdeaLoggingEvent("", Exception("EXCEPTION"))),
                        "ADDITIONAL_INFO_${List(1000) { " " }}_" // Trailing `_` to prevent trimming
                    )

                    assertThat(url)
                        .contains("ADDITIONAL_INFO")
                        .contains("EXCEPTION")
                        .contains("Randomness+version")
                }

                it("excludes parts that are expanded") {
                    val url = reporter.getIssueUrl(
                        arrayOf(IdeaLoggingEvent("", Exception("EXCEPTION"))),
                        "ADDITIONAL_INFO_${List(2000) { "\n" }}"
                    )

                    assertThat(url)
                        .doesNotContain("ADDITIONAL_INFO")
                        .contains("EXCEPTION")
                        .contains("Randomness+version")
                }
            }
        }
    }
})
