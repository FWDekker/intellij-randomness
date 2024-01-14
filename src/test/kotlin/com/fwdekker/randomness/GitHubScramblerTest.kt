package com.fwdekker.randomness

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.string.shouldStartWith


/**
 * Unit tests for [GitHubScrambler].
 */
object GitHubScramblerTest : FunSpec({
    test("fetches a valid token") {
        GitHubScrambler.getToken() shouldStartWith "github_"
    }

    test("scrambles and unscrambles") {
        GitHubScrambler.unscramble(GitHubScrambler.scramble("input")) shouldBeEqual "input"
    }
})
