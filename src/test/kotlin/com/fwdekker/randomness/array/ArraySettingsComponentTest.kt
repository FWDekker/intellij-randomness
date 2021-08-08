package com.fwdekker.randomness.array

import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * GUI tests for [ArraySchemeDecoratorEditor].
 */
object ArraySettingsComponentTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var arraySchemeDecorator: ArraySchemeDecorator
    lateinit var arraySettingsComponent: ArraySchemeDecoratorEditor
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        arraySchemeDecorator = ArraySchemeDecorator()
            .apply {
                currentScheme.count = 6
                currentScheme.brackets = "[]"
                currentScheme.separator = ","
                currentScheme.isSpaceAfterSeparator = false
            }

        arraySettingsComponent =
            GuiActionRunner.execute<ArraySchemeDecoratorEditor> { ArraySchemeDecoratorEditor(arraySchemeDecorator) }
        frame = showInFrame(arraySettingsComponent.rootComponent)
    }

    afterEachTest {
        ideaFixture.tearDown()
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' count") {
            frame.spinner("count").requireValue(6)
        }

        it("loads the settings' brackets") {
            frame.radioButton("bracketsNone").requireSelected(false)
            frame.radioButton("bracketsSquare").requireSelected(true)
            frame.radioButton("bracketsCurly").requireSelected(false)
            frame.radioButton("bracketsRound").requireSelected(false)
        }

        it("loads the settings' separator") {
            frame.radioButton("separatorComma").requireSelected(true)
            frame.radioButton("separatorSemicolon").requireSelected(false)
            frame.radioButton("separatorNewline").requireSelected(false)
        }

        it("loads the settings' settings for using a space after separator") {
            frame.checkBox("spaceAfterSeparator").requireSelected(false)
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("count").target().value = 642
                frame.radioButton("bracketsCurly").target().isSelected = true
                frame.radioButton("separatorSemicolon").target().isSelected = true
                frame.checkBox("spaceAfterSeparator").target().isSelected = false
            }

            val scheme = ArraySchemeDecorator()
            arraySettingsComponent.saveScheme(scheme)

            assertThat(scheme.count).isEqualTo(642)
            assertThat(scheme.brackets).isEqualTo("{}")
            assertThat(scheme.separator).isEqualTo(";")
            assertThat(scheme.isSpaceAfterSeparator).isEqualTo(false)
        }
    }

    describe("input handling") {
        it("truncates decimals in the count") {
            GuiActionRunner.execute { frame.spinner("count").target().value = 983.24f }

            frame.spinner("count").requireValue(983)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { arraySettingsComponent.loadSettings(ArraySchemeDecorator()) }

            assertThat(arraySettingsComponent.doValidate()).isNull()
        }

        describe("count") {
            it("fails for a count of 0") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 0 }

                val validationInfo = arraySettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
                assertThat(validationInfo?.message).isEqualTo("The count should be greater than or equal to 1.")
            }

            it("passes for a count of 1") {
                GuiActionRunner.execute { frame.spinner("count").target().value = 1 }

                assertThat(arraySettingsComponent.doValidate()).isNull()
            }

            it("fails for a negative count") {
                GuiActionRunner.execute { frame.spinner("count").target().value = -172 }

                val validationInfo = arraySettingsComponent.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("count").target())
                assertThat(validationInfo?.message).isEqualTo("The count should be greater than or equal to 1.")
            }
        }
    }
})
