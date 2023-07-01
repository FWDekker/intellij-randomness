package com.fwdekker.randomness

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.util.MissingResourceException


/**
 * Unit tests for [Bundle].
 */
object BundleTest : DescribeSpec({
    describe("get (no arguments)") {
        it("throws an exception if the key could not be found") {
            assertThatThrownBy { Bundle("this_key_does_not_exist") }.isInstanceOf(MissingResourceException::class.java)
        }

        it("returns the string at the given key") {
            assertThat(Bundle("string.title")).isEqualTo("String")
        }
    }

    describe("get (vararg)") {
        it("ignores unnecessary arguments") {
            assertThat(Bundle("string.title", "tap", "bring")).isEqualTo("String")
        }

        it("inserts the given arguments into the string") {
            assertThat(Bundle("preview.invalid", "sweep")).isEqualTo("Settings are invalid:\nsweep")
        }
    }
})
