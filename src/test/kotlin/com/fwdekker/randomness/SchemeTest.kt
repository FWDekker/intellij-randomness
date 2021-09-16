package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArrayDecorator
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
        it("returns null if the type icon is null") {
            scheme.typeIcon = null

            assertThat(scheme.icon).isNull()
        }

        it("uses the type icon as a basis for the icon") {
            assertThat((scheme.icon as OverlayedIcon).base).isEqualTo(scheme.typeIcon)
        }

        it("does not add icon overlays if the scheme's decorators have no icons") {
            scheme.arrayDecorator.enabled = false

            assertThat(scheme.icon!!.overlays).isEmpty()
        }

        it("adds the scheme's decorators' icons") {
            scheme.arrayDecorator.enabled = true

            assertThat(scheme.icon!!.overlays).containsExactly(scheme.arrayDecorator.icon)
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

