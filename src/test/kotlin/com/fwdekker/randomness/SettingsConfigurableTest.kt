package com.fwdekker.randomness

import com.intellij.openapi.options.ConfigurationException
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [SettingsConfigurable].
 */
object SettingsConfigurableTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var scheme: DummyScheme
    lateinit var editor: DummySchemeEditor
    lateinit var configurable: DummySettingsConfigurable
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        configurable = DummySettingsConfigurable()
        GuiActionRunner.execute { configurable.createComponent() }
        editor = configurable.editor as DummySchemeEditor
        scheme = editor.originalScheme
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        ideaFixture.tearDown()
        frame.cleanUp()
    }


    describe("display name") {
        it("returns the correct display name") {
            assertThat(configurable.displayName).isEqualTo("Dummy")
        }
    }

    describe("saving modifications") {
        it("accepts correct settings") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "for" }

            configurable.apply()

            assertThat(editor.originalScheme.literals).containsExactly("for")
        }

        it("rejects incorrect settings") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = DummyScheme.INVALID_OUTPUT }

            assertThatThrownBy { configurable.apply() }.isInstanceOf(ConfigurationException::class.java)
        }
    }

    describe("modification detection") {
        it("is initially unmodified") {
            assertThat(configurable.editor.isModified()).isFalse()
            assertThat(configurable.editor.doValidate()).isNull()
            assertThat(configurable.isModified).isFalse()
        }

        it("detects a single modification") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "rush" }

            assertThat(configurable.isModified).isTrue()
        }

        it("declares itself modified if settings are invalid, even though no modifications have been made") {
            // Ground truth: `isModified` is false after reloading valid settings
            GuiActionRunner.execute { editor.loadScheme() }
            assertThat(configurable.isModified).isFalse()

            // Actual test: `isModified` is true after reloading invalid settings
            val invalidSettings = DummyScheme.from(DummyScheme.INVALID_OUTPUT)
            GuiActionRunner.execute { editor.loadScheme(invalidSettings) }

            require(!editor.isModified()) { "Editor is incorrectly marked as modified." }
            assertThat(configurable.isModified).isTrue()
        }

        it("ignores an undone modification") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "vowel" }
            assertThat(configurable.isModified).isTrue()

            GuiActionRunner.execute { frame.textBox("literals").target().text = scheme.literals.joinToString(",") }

            assertThat(configurable.isModified).isFalse()
        }

        it("ignores saved modifications") {
            GuiActionRunner.execute { frame.textBox("literals").target().text = "salt" }
            assertThat(configurable.isModified).isTrue()

            configurable.apply()

            assertThat(configurable.isModified).isFalse()
        }
    }

    describe("resets") {
        it("resets all fields to their initial values") {
            GuiActionRunner.execute {
                frame.textBox("literals").target().text = "for"

                configurable.reset()
            }

            assertThat(frame.textBox("literals").target().text).isEqualTo(DummyScheme.DEFAULT_OUTPUT)
        }

        it("is no longer marked as modified after a reset") {
            GuiActionRunner.execute {
                frame.textBox("literals").target().text = "rice"

                configurable.reset()
            }

            assertThat(configurable.isModified).isFalse()
        }
    }
})
