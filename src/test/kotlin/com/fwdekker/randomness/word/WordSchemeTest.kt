package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


/**
 * Unit tests for [WordScheme].
 */
object WordSchemeTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "output"),
                row("returns a word", WordScheme(words = listOf("word")), "word"),
                row("returns a word with whitespace", WordScheme(words = listOf("x y")), "x y"),
                row("capitalizes the word", WordScheme(words = listOf("word"), capitalization = CapitalizationMode.UPPER), "WORD"),
                row("applies decorators in order affix, array", WordScheme(words = listOf("word"), affixDecorator = AffixDecorator(enabled = true, descriptor = "'"), arrayDecorator = ArrayDecorator(enabled = true)), "['word', 'word', 'word']"),
                //@formatter:on
            )
        ) { _, scheme, output -> scheme.generateStrings()[0] shouldBe output }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", WordScheme(), null),
                row("fails if word list is empty", WordScheme(words = emptyList()), "word.error.empty_word_list"),
                row("fails if affix decorator is invalid", WordScheme(affixDecorator = AffixDecorator(descriptor = """\""")), ""),
                row("fails if array decorator is invalid", WordScheme(arrayDecorator = ArrayDecorator(minCount = -24)), ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: WordScheme


        beforeEach {
            scheme = WordScheme()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.words += "old-word"

            copy.words shouldNotBe scheme.words
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
