package com.fwdekker.randomness

import icons.RandomnessIcons
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [State].
 */
object StateTest : Spek({
    lateinit var state: DummyState


    beforeEachGroup {
        state = DummyState()
    }


    describe("icon") {
        it("returns null if the icons are null") {
            state.icons = null

            assertThat(state.icon).isNull()
        }

        it("returns the base icon if the icons are not null") {
            state.icons = RandomnessIcons.Uuid

            assertThat(state.icon).isEqualTo(RandomnessIcons.Uuid.Base)
        }
    }
})


/**
 * Dummy implementation of [State].
 *
 * @property name The configurable name of the dummy state.
 */
private data class DummyState(override var name: String = DEFAULT_NAME) : State() {
    override var icons: RandomnessIcons? = null


    override fun deepCopy() = copy()
}
