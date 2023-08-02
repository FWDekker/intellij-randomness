package com.fwdekker.randomness

import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.template.TemplateList
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs


/**
 * Unit tests for [Settings].
 */
object SettingsTest : FunSpec({
    test("doValidate") {
        forAll(
            //@formatter:off
            table(
                headers("description", "scheme", "validation"),
                row("succeeds for default state", Settings(), null),
                row("fails if template list is invalid", Settings(TemplateList(mutableListOf(Template("Duplicate"), Template("Duplicate")))), ""),
            )
            //@formatter:on
        ) { _, scheme, validation -> scheme shouldValidateAsBundle validation }
    }

    test("deepCopy") {
        test("deep-copies the template list") {
            val settings = Settings(TemplateList(mutableListOf(Template("old"))))

            val copy = settings.deepCopy()
            copy.templates[0].name = "new"

            settings.templates[0].name shouldBe "old"
        }
    }

    test("copyFrom") {
        test("cannot copy from another type") {
            val settings = Settings()

            shouldThrow<IllegalArgumentException> { settings.copyFrom(DummyState()) }
                .message shouldBe "Cannot copy from different type."
        }

        test("deep-copies the template list") {
            val settings = Settings(TemplateList(mutableListOf(Template("old"))))
            val other = Settings(TemplateList(mutableListOf(Template("new"))))

            settings.copyFrom(other)

            settings.templateList shouldNotBeSameInstanceAs other.templateList
            settings.templates[0].name shouldBe "new"
        }
    }
})

// TODO: Where do I leave this?
///**
// * Unit tests for [PersistentSettings].
// */
//object PersistentSettingsTest : FunSpec({
//    lateinit var ideaFixture: IdeaTestFixture
//    lateinit var scheme: DummyScheme
//    lateinit var editor: DummySchemeEditor
//    lateinit var configurable: DummyStateConfigurable
//    lateinit var frame: FrameFixture
//
//
//    beforeContainer {
//        FailOnThreadViolationRepaintManager.install()
//    }
//
//    beforeEach {
//        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
//        ideaFixture.setUp()
//
//        configurable = DummyStateConfigurable()
//        guiExecute { configurable.createComponent() }
//        editor = configurable.editor as DummySchemeEditor
//        scheme = editor.originalState
//        frame = Containers.showInFrame(editor.rootComponent)
//    }
//
//    afterEach {
//        frame.cleanUp()
//        ideaFixture.tearDown()
//    }
//
//
//    test("display name") {
//        test("returns the correct display name") {
//            assertThat(configurable.displayName).isEqualTo("Dummy")
//        }
//    }
//
//    test("saving modifications") {
//        test("accepts correct settings") {
//            guiExecute { frame.textBox("literals").target().text = "for" }
//
//            configurable.apply()
//
//            assertThat(editor.originalState.literals).containsExactly("for")
//        }
//
//        test("rejects incorrect settings") {
//            guiExecute { frame.textBox("literals").target().text = DummyScheme.INVALID_OUTPUT }
//
//            assertThatThrownBy { configurable.apply() }.isInstanceOf(ConfigurationException::class.java)
//        }
//    }
//
//    test("modification detection") {
//        test("is initially unmodified") {
//            assertThat(configurable.editor.isModified()).isFalse()
//            assertThat(configurable.editor.doValidate()).isNull()
//            assertThat(configurable.isModified).isFalse()
//        }
//
//        test("detects a single modification") {
//            guiExecute { frame.textBox("literals").target().text = "rush" }
//
//            assertThat(configurable.isModified).isTrue()
//        }
//
//        test("declares itself modified if settings are invalid, even though no modifications have been made") {
//            // Ground truth: `isModified` is `false` after reloading valid settings
//            guiExecute { editor.loadState() }
//            assertThat(configurable.isModified).isFalse()
//
//            // Actual test: `isModified` is `true` after reloading invalid settings
//            val invalidSettings = DummyScheme.from(DummyScheme.INVALID_OUTPUT)
//            guiExecute { editor.loadState(invalidSettings) }
//
//            require(!editor.isModified()) { "Editor is incorrectly marked as modified." }
//            assertThat(configurable.isModified).isTrue()
//        }
//
//        test("ignores an undone modification") {
//            guiExecute { frame.textBox("literals").target().text = "vowel" }
//            assertThat(configurable.isModified).isTrue()
//
//            guiExecute { frame.textBox("literals").target().text = scheme.literals.joinToString(",") }
//
//            assertThat(configurable.isModified).isFalse()
//        }
//
//        test("ignores saved modifications") {
//            guiExecute { frame.textBox("literals").target().text = "salt" }
//            assertThat(configurable.isModified).isTrue()
//
//            configurable.apply()
//
//            assertThat(configurable.isModified).isFalse()
//        }
//    }
//
//    test("resets") {
//        test("resets all fields to their initial values") {
//            guiExecute {
//                frame.textBox("literals").target().text = "for"
//
//                configurable.reset()
//            }
//
//            assertThat(frame.textBox("literals").target().text).isEqualTo(DummyScheme.DEFAULT_OUTPUT)
//        }
//
//        test("is no longer marked as modified after a reset") {
//            guiExecute {
//                frame.textBox("literals").target().text = "rice"
//
//                configurable.reset()
//            }
//
//            assertThat(configurable.isModified).isFalse()
//        }
//    }
//})
