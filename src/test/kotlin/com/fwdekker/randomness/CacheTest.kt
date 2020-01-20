package com.fwdekker.randomness

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [Cache].
 */
object CacheTest : Spek({
    lateinit var cache: Cache<String, Any>


    beforeEachTest {
        cache = Cache { Any() }
    }


    describe("get") {
        it("returns the same value if requested twice") {
            val a1 = cache.get("a")
            val a2 = cache.get("a")

            assertThat(a2).isSameAs(a1)
        }

        it("returns different values if different keys are requested") {
            val a = cache.get("a")
            val b = cache.get("b")

            assertThat(b).isNotSameAs(a)
        }

        it("returns different values if the cache is not used") {
            val a1 = cache.get("a")
            val a2 = cache.get("a", false)

            assertThat(a2).isNotSameAs(a1)
        }

        it("returns the newest value when the cache is overridden") {
            cache.get("a")
            val a2 = cache.get("a", false)
            val a3 = cache.get("a")

            assertThat(a3).isSameAs(a2)
        }
    }

    describe("clear") {
        it("returns different values if the cache is cleared in between") {
            val a1 = cache.get("a")
            cache.clear()
            val a2 = cache.get("a")

            assertThat(a2).isNotSameAs(a1)
        }
    }
})
