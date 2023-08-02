package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.shouldValidateAsBundle
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.UUIDVersion
import io.kotest.matchers.string.beLowerCase
import io.kotest.matchers.string.beUUID
import io.kotest.matchers.string.beUpperCase
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain


/**
 * Unit tests for [UuidScheme].
 */
object UuidSchemeTest : FunSpec({
    tags(NamedTag("Scheme"))


    test("generateStrings") {
        test("returns type-1 uuid") {
            UuidScheme(type = 1).generateStrings()[0] should beUUID(version = UUIDVersion.V1, considerNilValid = false)
        }

        test("returns type-4 uuid") {
            UuidScheme(type = 4).generateStrings()[0] should beUUID(version = UUIDVersion.V4, considerNilValid = false)
        }

        test("returns uppercase string") {
            UuidScheme(isUppercase = true).generateStrings()[0] should beUpperCase()
        }

        test("returns lowercase string") {
            UuidScheme(isUppercase = false).generateStrings()[0] should beLowerCase()
        }

        test("returns string with dashes") {
            UuidScheme(isUppercase = true).generateStrings()[0] shouldContain "-"
        }

        test("returns string without dashes") {
            UuidScheme(isUppercase = true).generateStrings()[0] shouldNotContain "-"
        }

        test("applies decorators in order affix, array") {
            UuidScheme(
                affixDecorator = AffixDecorator(enabled = true, descriptor = "#@"),
                arrayDecorator = ArrayDecorator(enabled = true, minCount = 3, maxCount = 3),
            ).generateStrings()[0].count { it == '#' } shouldBe 3
        }
    }

    test("doValidate") {
        forAll(
            table(
                //@formatter:off
                headers("description", "scheme", "validation"),
                row("succeeds for default state", UuidScheme(), null),
                row("fails for unsupported type", UuidScheme(type = 14), "uuid.error.unknown_type"),
                row("fails if affix decorator is invalid", UuidScheme(affixDecorator = AffixDecorator(descriptor = """\""")), ""),
                row("fails if array decorator is invalid", UuidScheme(arrayDecorator = ArrayDecorator(minCount = -539)), ""),
                //@formatter:on
            )
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        lateinit var scheme: UuidScheme


        beforeEach {
            scheme = UuidScheme()
        }


        test("equals old instance") {
            scheme.deepCopy() shouldBe scheme
        }

        test("is independent of old instance") {
            val copy = scheme.deepCopy()

            scheme.type = 503

            copy.type shouldNotBe scheme.type
        }

        test("retains uuid if chosen") {
            scheme.deepCopy(true).uuid shouldBe scheme.uuid
        }

        test("replaces uuid if chosen") {
            scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
        }
    }
})
