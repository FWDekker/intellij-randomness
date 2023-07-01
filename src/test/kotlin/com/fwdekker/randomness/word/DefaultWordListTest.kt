package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.io.IOException
import kotlin.system.measureNanoTime


/**
 * Unit tests for [DefaultWordList].
 */
object DefaultWordListTest : DescribeSpec({
    describe("words") {
        it("throws an exception if the file does not exist") {
            val list = DefaultWordList("throw", "word-lists/does-not-exist.txt")

            assertThatThrownBy { list.words }
                .isInstanceOf(IOException::class.java)
                .hasMessage(Bundle("word_list.error.file_not_found"))
        }

        it("returns an empty list of words if the file is empty") {
            val list = DefaultWordList("tailor", "word-lists/empty-list.txt")

            assertThat(list.words).isEmpty()
        }

        it("returns the list of words from the file") {
            val list = DefaultWordList("off", "word-lists/non-empty-list.txt")

            assertThat(list.words).containsExactly("lorem", "ipsum", "dolor")
        }

        it("does not return blank lines") {
            val list = DefaultWordList("wander", "word-lists/with-blank-lines.txt")

            assertThat(list.words).containsExactly("afraid", "dive", "snow", "enemy")
        }

        describe("caching") {
            it("throws an exception again if the words are read again") {
                val list = DefaultWordList("bound", "word-lists/does-not-exist.txt")

                assertThatThrownBy { list.words }.isInstanceOf(IOException::class.java)
                    .hasMessage(Bundle("word_list.error.file_not_found"))

                assertThatThrownBy { list.words }.isInstanceOf(IOException::class.java)
                    .hasMessage(Bundle("word_list.error.file_not_found"))
            }

            it("returns words quicker if read again from the same instance") {
                val list = DefaultWordList("height", "word-lists/timing-test-instance.txt")

                val firstTime = measureNanoTime { list.words }
                val secondTime = measureNanoTime { list.words }

                assertThat(secondTime).isLessThan(firstTime / 2)
            }

            it("returns words quicker if read again from another instance") {
                val firstList = DefaultWordList("charge", "word-lists/timing-test-global.txt")
                val firstTime = measureNanoTime { firstList.words }

                val secondList = DefaultWordList("charge", "word-lists/timing-test-global.txt")
                val secondTime = measureNanoTime { secondList.words }

                assertThat(secondTime).isLessThan(firstTime / 2)
            }
        }
    }
})
