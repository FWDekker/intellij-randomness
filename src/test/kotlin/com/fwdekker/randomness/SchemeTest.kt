package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySchemeDecorator
import icons.RandomnessIcons
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [Scheme].
 */
object SchemeTest : Spek({
    lateinit var scheme: DummyScheme


    beforeEachTest {
        scheme = DummyScheme()
    }


    describe("icon") {
        it("returns null if there are no icons") {
            scheme.icons = null

            assertThat(scheme.icon).isNull()
        }

        it("returns null if there are no icons, even if the decorator is enabled") {
            scheme.icons = null
            scheme.decorator = ArraySchemeDecorator(enabled = true)

            assertThat(scheme.icon).isNull()
        }

        it("returns the base icon if the decorator is disabled") {
            scheme.decorator = ArraySchemeDecorator(enabled = false)

            assertThat(scheme.icon).isEqualTo(RandomnessIcons.Data.Base)
        }

        it("returns the array icon if the decorator is enabled") {
            scheme.decorator = ArraySchemeDecorator(enabled = true)

            assertThat(scheme.icon).isEqualTo(RandomnessIcons.Data.Array)
        }
    }

    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            scheme.literals = listOf(DummyScheme.INVALID_OUTPUT)

            assertThatThrownBy { scheme.generateStrings() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Invalid input!")
        }

        it("returns undecorated strings if there is no decorator") {
            scheme.decorator.enabled = false

            assertThat(scheme.generateStrings(2))
                .containsExactly(DummyScheme.DEFAULT_OUTPUT, DummyScheme.DEFAULT_OUTPUT)
        }

        it("returns decorated strings if there is a decorator") {
            scheme.decorator.enabled = true
            scheme.decorator.count = 1

            assertThat(scheme.generateStrings(2))
                .containsExactly("[${DummyScheme.DEFAULT_OUTPUT}]", "[${DummyScheme.DEFAULT_OUTPUT}]")
        }
    }
})

