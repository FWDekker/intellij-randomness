package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.IOException


/**
 * Unit tests for [DefaultWordList].
 */
class DefaultWordListTest : Spek({
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
    }
})
