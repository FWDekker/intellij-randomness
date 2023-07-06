package com.fwdekker.randomness.array

import com.fwdekker.randomness.DataGenerationException
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy


/**
 * Unit tests for [BracketsDescriptor].
 */
object BracketsDescriptorTest : DescribeSpec({
    data class Param(
        val description: String,
        val descriptor: String,
        val isValid: Boolean,
        val output: String,
    )


    val data = "DATA"
    val tests = listOf(
        Param("empty descriptor", "", true, data),
        Param("a descriptor without @ or \\", "txEcxUhK", true, "txEcxUhK${data}txEcxUhK"),
        Param("a descriptor with @ without \\", "IwO@kCWtGUB", true, "IwO${data}kCWtGUB"),
        Param("a descriptor with multiple @s without \\", "cqQOZV@NZh@H", true, "cqQOZV${data}NZh${data}H"),
        Param("a descriptor without @ with an escaped \\", "ixjM\\\\Vlzh", true, "ixjM\\Vlzh${data}ixjM\\Vlzh"),
        Param("a descriptor with an escaped \\@", "BCDs\\@MTe", true, "BCDs@MTe${data}BCDs@MTe"),
        Param("a descriptor with an escaped character other than @ or \\", "Hf\\bP", true, "HfbP${data}HfbP"),
        Param(
            "a descriptor with escaped @, \\, and other, and multiple @s",
            "I\\Pnv@OH\\\\gil@wKa@Kh\\@cx",
            true,
            "IPnv${data}OH\\gil${data}wKa${data}Kh@cx"
        ),
        Param("a descriptor with an escaped \\ at the end", "xHH@LrZ\\\\", true, "xHH${data}LrZ\\"),
        Param("a descriptor with an unescaped \\ at the end", "NcMtGvIpC\\", false, "invalid-case"),
    )


    describe("doValidate") {
        tests.forEach { (description, descriptor, isValid, _) ->
            it("returns ${if (isValid) "null" else "non-null"} for $description") {
                if (isValid)
                    assertThat(BracketsDescriptor(descriptor).doValidate()).isNull()
                else
                    assertThat(BracketsDescriptor(descriptor).doValidate()).isNotNull
            }
        }
    }

    describe("interpolate") {
        tests.forEach { (description, descriptor, isValid, output) ->
            if (isValid)
                it("generates the given string for $description") {
                    assertThat(BracketsDescriptor(descriptor).interpolate(data)).isEqualTo(output)
                }
            else
                it("throws an exception for $description") {
                    assertThatThrownBy { BracketsDescriptor(descriptor).interpolate(data) }
                        .isInstanceOf(DataGenerationException::class.java)
                }
        }
    }
})
