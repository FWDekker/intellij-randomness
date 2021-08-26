package com.fwdekker.randomness.template

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.SymbolSetSettings
import icons.RandomnessIcons
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.random.Random


/**
 * Unit tests for [Template].
 */
object TemplateTest : Spek({
    lateinit var template: Template


    beforeEachTest {
        template = Template()
    }


    describe("icons") {
        it("returns the single scheme's icons if the single scheme has icons") {
            val scheme = IntegerScheme()
            template.schemes = listOf(scheme)

            assertThat(template.icons).isEqualTo(scheme.icons)
        }

        it("returns the default icons if the single scheme has no icons") {
            val scheme = DummyScheme()
            template.schemes = listOf(scheme)

            assertThat(template.icons).isEqualTo(RandomnessIcons.Data)
        }

        it("returns the default icons if no scheme is present") {
            template.schemes = emptyList()

            assertThat(template.icons).isEqualTo(RandomnessIcons.Data)
        }

        it("returns the default icons if multiple schemes are present") {
            template.schemes = listOf(DummyScheme(), DummyScheme())

            assertThat(template.icons).isEqualTo(RandomnessIcons.Data)
        }
    }


    describe("generateStrings") {
        it("throws an exception if the template is invalid") {
            template.schemes = listOf(DummyScheme.from(DummyScheme.INVALID_OUTPUT))

            assertThatThrownBy { template.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("generates empty strings if it contains no schemes") {
            template.schemes = emptyList()

            assertThat(template.generateStrings()).containsExactly("")
        }

        it("generates the single scheme's output") {
            val random = { Random(78) }

            val scheme = IntegerScheme()
            template.schemes = listOf(scheme)

            val schemeOutput = scheme.also { it.random = random() }.generateStrings(3)
            val templateOutput = template.also { it.random = random() }.generateStrings(3)

            assertThat(schemeOutput).containsExactlyElementsOf(templateOutput)
        }

        it("generates the concatenation of multiple schemes' outputs") {
            val random = { Random(624) }

            val schemeA = IntegerScheme(minValue = 461, maxValue = 664)
                .also { it.random = random() }
            val schemeB = LiteralScheme("-")
                .also { it.random = schemeA.random }
            val schemeC = IntegerScheme(minValue = 125, maxValue = 607)
                .also { it.random = schemeB.random }

            template.schemes = listOf(schemeA, schemeB, schemeC)
            template.random = random()

            val outputsA = schemeA.generateStrings(3)
            val outputsB = schemeB.generateStrings(3)
            val outputsC = schemeC.generateStrings(3)
            val combinedOutputs = outputsA.zip(outputsB) { a, b -> a + b }.zip(outputsC) { a, b -> a + b }

            assertThat(template.generateStrings(3)).containsExactlyElementsOf(combinedOutputs)
        }
    }

    describe("setSettingsState") {
        it("overwrites the symbol set settings of the contained schemes") {
            val newSettings = SettingsState(symbolSetSettings = SymbolSetSettings())
            val stringScheme = StringScheme(SymbolSetSettings())
            template.schemes = listOf(stringScheme)

            template.setSettingsState(newSettings)

            assertThat(stringScheme.symbolSetSettings).isSameAs(newSettings.symbolSetSettings)
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(Template().doValidate()).isNull()
        }

        it("passes for a template without schemes") {
            template.schemes = emptyList()

            assertThat(template.doValidate()).isNull()
        }

        it("fails if the template does not have a name") {
            template.name = ""

            assertThat(template.doValidate()).isEqualTo("Templates must have a name.")
        }

        it("fails if the single scheme is invalid") {
            template.schemes = listOf(DummyScheme.from(DummyScheme.INVALID_OUTPUT))

            assertThat(template.doValidate()).isEqualTo("Dummy > Invalid input!")
        }

        it("fails if one of multiple schemes is invalid") {
            template.schemes = listOf(
                DummyScheme(),
                DummyScheme(),
                DummyScheme.from(DummyScheme.INVALID_OUTPUT),
                DummyScheme()
            )

            assertThat(template.doValidate()).isEqualTo("Dummy > Invalid input!")
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            template.schemes = listOf(LiteralScheme("rubber"))

            val copy = template.deepCopy()
            (copy.schemes.first() as LiteralScheme).literal = "ribbon"

            assertThat((template.schemes.first() as LiteralScheme).literal).isEqualTo("rubber")
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            template.name = "become"
            template.schemes = listOf(LiteralScheme("quarrel"))

            val newTemplate = Template()
            newTemplate.copyFrom(template)

            assertThat(newTemplate)
                .isEqualTo(template)
                .isNotSameAs(template)
        }
    }
})
