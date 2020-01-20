package com.fwdekker.randomness.array

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyInsertArrayAction
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [com.fwdekker.randomness.DataInsertArrayAction].
 */
object DataInsertArrayActionTest : Spek({
    val randomValue = "random_value"


    describe("generateString") {
        it("throws an exception if the count is empty") {
            val action = DummyInsertArrayAction(ArrayScheme(count = 0)) { randomValue }
            assertThatThrownBy { action.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Array cannot have fewer than 1 element.")
        }

        it("throws an exception if the count is negative") {
            val action = DummyInsertArrayAction(ArrayScheme(count = -3)) { randomValue }
            assertThatThrownBy { action.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Array cannot have fewer than 1 element.")
        }

        it("chunks the values according to the settings") {
            val randomArray = "[$randomValue, $randomValue]"
            assertThat(DummyInsertArrayAction(ArrayScheme(count = 2)) { randomValue }.generateStrings(4))
                .containsExactly(randomArray, randomArray, randomArray, randomArray)
        }
    }
})
