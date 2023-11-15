package com.fwdekker.randomness.testhelpers

import io.kotest.core.NamedTag


/**
 * Tags for Kotest-based tags for filtering tests.
 *
 * See the project's README for more information.
 */
object Tags {
    /**
     * Tests for [com.fwdekker.randomness.SchemeEditor]s.
     */
    val EDITOR = NamedTag("Editor")

    /**
     * Tests that rely on setting up an IDE fixture.
     */
    val IDEA_FIXTURE = NamedTag("IdeaFixture")

    /**
     * Tests that use the IntelliJ UI test robot.
     */
    val INTELLIJ_ROBOT = NamedTag("IntelliJRobot")

    /**
     * Tests for [com.fwdekker.randomness.Scheme]s.
     */
    val SCHEME = NamedTag("Scheme")

    /**
     * Tests that rely on accessing Swing components.
     */
    val SWING = NamedTag("Swing")
}
