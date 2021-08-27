package com.fwdekker.randomness

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [Box].
 */
object BoxTest : Spek({
    describe("box") {
        it("returns the generator's value when de-referenced") {
            assertThat(+Box({ "needle" })).isEqualTo("needle")
        }

        it("returns the generator's value when de-referenced again") {
            val box = Box({ "urgent" })

            +box

            assertThat(+box).isEqualTo("urgent")
        }

        it("returns the assigned value when de-referenced") {
            val box = Box({ "official" })

            box += "anyhow"

            assertThat(+box).isEqualTo("anyhow")
        }

        it("returns the assigned value when de-referenced after the generator has been invoked") {
            val box = Box({ "regard" })

            +box
            box += "path"

            assertThat(+box).isEqualTo("path")
        }

        it("returns the last value that was assigned") {
            val box = Box({ "house" })

            box += "move"
            box += "distance"

            assertThat(+box).isEqualTo("distance")
        }

        it("retains the assigned state when copied") {
            val box = Box({ "breath" })

            box += "harden"

            assertThat(+box.copy()).isEqualTo("harden")
        }

        it("creates an independent reference when copied") {
            data class StringHolder(var value: String)

            val box = Box({ StringHolder("story") })

            val copy = box.copy()
            copy += StringHolder("hunt")

            assertThat((+box).value).isEqualTo("story")
            assertThat((+copy).value).isEqualTo("hunt")
        }

        it("retains the reference to the same value when copied") {
            data class StringHolder(var value: String)

            val holder = StringHolder("correct")
            val box = Box({ holder })

            val copy = box.copy()
            (+copy).value = "maybe"

            assertThat((+box).value).isEqualTo("maybe")
            assertThat((+copy).value).isEqualTo("maybe")
        }

        it("does not deep-copy the generator") {
            var isInvoked = 0
            val box = Box({
                isInvoked++
                "treasury"
            })

            val copy = box.copy()
            +copy

            assertThat(isInvoked).isEqualTo(1)
        }
    }
})
