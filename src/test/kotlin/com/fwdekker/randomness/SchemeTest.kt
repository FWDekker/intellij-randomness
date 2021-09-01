package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArrayDecorator
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


    describe("uuid") {
        it("assigns different UUIDs for different instances") {
            assertThat(DummyScheme().uuid).isNotEqualTo(DummyScheme().uuid)
        }

        it("changes the scheme's UUID when it is copied with `retainUuid` set to false") {
            assertThat(scheme.deepCopy(retainUuid = false).uuid).isNotEqualTo(scheme.uuid)
        }

        it("does not change the scheme's UUID when it is copied with `retainUuid` set to true") {
            assertThat(scheme.deepCopy(retainUuid = true).uuid).isEqualTo(scheme.uuid)
        }

        it("changes the scheme's UUID to the other's UUID when copied from another instance") {
            val other = DummyScheme()

            scheme.copyFrom(other)

            assertThat(scheme.uuid).isEqualTo(other.uuid)
        }
    }

    describe("icon") {
        it("returns null if there are no icons") {
            scheme.icons = null

            assertThat(scheme.icon).isNull()
        }

        it("returns null if there are no icons, even if the decorator is enabled") {
            scheme.icons = null
            scheme.arrayDecorator = ArrayDecorator(enabled = true)

            assertThat(scheme.icon).isNull()
        }

        it("returns the base icon if the decorator is disabled") {
            scheme.arrayDecorator = ArrayDecorator(enabled = false)

            assertThat(scheme.icon).isEqualTo(RandomnessIcons.Data.Base)
        }

        it("returns the array icon if the decorator is enabled") {
            scheme.arrayDecorator = ArrayDecorator(enabled = true)

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
            scheme.arrayDecorator.enabled = false

            assertThat(scheme.generateStrings(2))
                .containsExactly(DummyScheme.DEFAULT_OUTPUT, DummyScheme.DEFAULT_OUTPUT)
        }

        it("returns decorated strings if there is a decorator") {
            scheme.arrayDecorator.enabled = true
            scheme.arrayDecorator.minCount = 1
            scheme.arrayDecorator.maxCount = 1

            assertThat(scheme.generateStrings(2))
                .containsExactly("[${DummyScheme.DEFAULT_OUTPUT}]", "[${DummyScheme.DEFAULT_OUTPUT}]")
        }

        it("applies decorators on each other in ascending order") {
            scheme.decorators = listOf(
                ArrayDecorator(enabled = true, minCount = 2, maxCount = 2),
                ArrayDecorator(enabled = true, minCount = 3, maxCount = 3),
            )
            scheme.literals = listOf("save")

            assertThat(scheme.generateStrings(1)).containsExactly("[[save, save], [save, save], [save, save]]")
        }
    }
})

