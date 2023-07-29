package com.fwdekker.randomness

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat


/**
 * Unit tests for [MutableBox].
 */
object BoxTest : DescribeSpec({
    describe("box") {
        it("returns the generator's value when de-referenced") {
            assertThat(+MutableBox({ "needle" })).isEqualTo("needle")
        }

        it("returns the generator's value when de-referenced again") {
            val box = MutableBox({ "urgent" })

            +box

            assertThat(+box).isEqualTo("urgent")
        }

        it("returns the assigned value when de-referenced") {
            val box = MutableBox({ "official" })

            box += "anyhow"

            assertThat(+box).isEqualTo("anyhow")
        }

        it("returns the assigned value when de-referenced after the generator has been invoked") {
            val box = MutableBox({ "regard" })

            +box
            box += "path"

            assertThat(+box).isEqualTo("path")
        }

        it("returns the last value that was assigned") {
            val box = MutableBox({ "house" })

            box += "move"
            box += "distance"

            assertThat(+box).isEqualTo("distance")
        }

        it("retains the assigned state when copied") {
            val box = MutableBox({ "breath" })

            box += "harden"

            assertThat(+box.copy()).isEqualTo("harden")
        }

        it("creates an independent reference when copied") {
            data class StringHolder(var value: String)

            val box = MutableBox({ StringHolder("story") })

            val copy = box.copy()
            copy += StringHolder("hunt")

            assertThat((+box).value).isEqualTo("story")
            assertThat((+copy).value).isEqualTo("hunt")
        }

        it("retains the reference to the same value when copied") {
            data class StringHolder(var value: String)

            val holder = StringHolder("correct")
            val box = MutableBox({ holder })

            val copy = box.copy()
            (+copy).value = "maybe"

            assertThat((+box).value).isEqualTo("maybe")
            assertThat((+copy).value).isEqualTo("maybe")
        }

        it("does not deep-copy the generator") {
            var isInvoked = 0
            val box = MutableBox({
                isInvoked++
                "treasury"
            })

            val copy = box.copy()
            +copy

            assertThat(isInvoked).isEqualTo(1)
        }
    }
})
