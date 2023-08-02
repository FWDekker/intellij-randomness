package com.fwdekker.randomness

import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs


/**
 * Unit tests for [State].
 */
object StateTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("uuid") {
        test("generates a different UUID for each instance") {
            DummyState().uuid shouldNotBe DummyState().uuid
        }
    }

    test("context") {
        test("uses the applied context") {
            val context = Settings()
            val original = DummyState()

            original.applyContext(context)

            +original.context shouldBeSameInstanceAs context
        }
    }


    test("deepCopy") {
        test("retains UUID if indicated") {
            val original = DummyState()

            val copy = original.deepCopy(retainUuid = true)

            copy.uuid shouldBe original.uuid
        }

        test("does not retain UUID if not indicated") {
            val original = DummyState()

            val copy = original.deepCopy(retainUuid = false)

            copy.uuid shouldNotBe original.uuid
        }

        test("copies the context") {
            val context = Settings()
            val original = DummyState()
            original.applyContext(context)

            val copy = original.deepCopy()

            +copy.context shouldBeSameInstanceAs context
        }

        test("deep-copies fields") {
            val original = DummyState()
            val copy = original.deepCopy()

            copy.list.add(0)

            original.list shouldHaveSize 0
            copy.list shouldHaveSize 1
        }
    }

    test("copyFrom") {
        test("copies the UUID") {
            val original = DummyState()
            val other = DummyState()

            original.copyFrom(other)

            original.uuid shouldBe other.uuid
        }

        test("copies fields") {
            val original = DummyState(mutableListOf(0))
            val other = DummyState(mutableListOf(1))

            original.copyFrom(other)

            original.list shouldContainExactly listOf(1)
        }

        test("deep-copies fields") {
            val original = DummyState(mutableListOf(0))
            val other = DummyState(mutableListOf(1))

            original.copyFrom(other)
            other.list.add(2)

            original.list shouldContainExactly listOf(1)
        }

        test("copies the context") {
            val context = Settings()
            val original = DummyState()
            val other = DummyState()
            other.applyContext(context)

            original.copyFrom(other)

            +original.context shouldBeSameInstanceAs context
        }
    }
})
