package com.fwdekker.randomness.template

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.word.DictionarySettings
import com.fwdekker.randomness.word.WordScheme
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


    describe("typeIcon") {
        it("returns the single scheme's icon if the single scheme has an icon") {
            val scheme = IntegerScheme()
            template.schemes = listOf(scheme)

            assertThat(template.typeIcon).isEqualTo(scheme.typeIcon)
        }

        it("returns the default icon if the single scheme has no icon") {
            val scheme = DummyScheme()
            scheme.typeIcon = null

            template.schemes = listOf(scheme)

            assertThat(template.typeIcon).isEqualTo(Template.DEFAULT_ICON)
        }

        it("returns the default icon if no scheme is present") {
            template.schemes = emptyList()

            assertThat(template.typeIcon).isEqualTo(Template.DEFAULT_ICON)
        }

        it("returns the scheme's combined icon if multiple are present") {
            template.schemes = listOf(DummyScheme(), DummyScheme())

            assertThat(template.typeIcon.colors.size).isGreaterThan(1)
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
            val schemeB = StringScheme("-")
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
        it("overwrites the settings state of the contained schemes") {
            val newSettings = DictionarySettings()
            val wordScheme = WordScheme()
            template.schemes = listOf(wordScheme)

            template.setSettingsState(SettingsState(dictionarySettings = newSettings))

            assertThat(+wordScheme.dictionarySettings).isSameAs(newSettings)
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
            template.name = " "

            assertThat(template.doValidate()).isEqualTo("Assign a name to template '<empty>'.")
        }

        it("fails if the single scheme is invalid") {
            template.schemes = listOf(DummyScheme.from(DummyScheme.INVALID_OUTPUT))

            assertThat(template.doValidate()).isEqualTo("${DummyScheme.INVALID_OUTPUT} > Invalid input!")
        }

        it("fails if one of multiple schemes is invalid") {
            template.schemes = listOf(
                DummyScheme(),
                DummyScheme(),
                DummyScheme.from(DummyScheme.INVALID_OUTPUT),
                DummyScheme()
            )

            assertThat(template.doValidate()).isEqualTo("${DummyScheme.INVALID_OUTPUT} > Invalid input!")
        }

        it("fails if the decorator is invalid") {
            template.arrayDecorator.minCount = -160

            assertThat(template.doValidate()).isNotNull()
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            template.schemes = listOf(StringScheme("rubber"))
            template.arrayDecorator.maxCount = 857

            val copy = template.deepCopy()
            (copy.schemes.first() as StringScheme).pattern = "ribbon"
            copy.arrayDecorator.maxCount = 410

            assertThat((template.schemes.first() as StringScheme).pattern).isEqualTo("rubber")
            assertThat(template.arrayDecorator.maxCount).isEqualTo(857)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            template.name = "become"
            template.schemes = listOf(StringScheme("quarrel"))
            template.arrayDecorator.minCount = 820

            val newTemplate = Template()
            newTemplate.copyFrom(template)

            assertThat(newTemplate)
                .isEqualTo(template)
                .isNotSameAs(template)
        }
    }
})
