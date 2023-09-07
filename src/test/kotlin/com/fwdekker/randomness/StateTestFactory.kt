package com.fwdekker.randomness

import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


/**
 * Tests the [State.deepCopy] method of the scheme created by [createScheme].
 */
fun <S : State> stateDeepCopyTestFactory(createScheme: () -> S) =
    funSpec {
        context("deepCopy") {
            lateinit var scheme: S


            beforeNonContainer {
                scheme = createScheme()
            }


            test("equals old instance") {
                scheme.deepCopy() shouldBe scheme
            }

            test("is independent of old instance") {
                val copy = scheme.deepCopy()

                scheme.uuid = "other"

                copy.uuid shouldNotBe scheme.uuid
            }

            test("retains uuid if chosen") {
                scheme.deepCopy(true).uuid shouldBe scheme.uuid
            }

            test("replaces uuid if chosen") {
                scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
            }
        }
    }
