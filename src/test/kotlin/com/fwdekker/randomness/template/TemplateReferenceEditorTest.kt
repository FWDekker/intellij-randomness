package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.getComboBoxItem
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [TemplateReferenceEditor].
 */
object TemplateReferenceEditorTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var templateList: TemplateList
    lateinit var reference: TemplateReference
    lateinit var editor: TemplateReferenceEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        templateList = TemplateList(
            listOf(
                Template("cup", listOf(DummyScheme())),
                Template("instead", listOf(TemplateReference())),
                Template("gun", listOf(DummyScheme()))
            )
        )

        reference = templateList.templates[1].schemes[0] as TemplateReference
        reference.templateList = Box({ templateList })
        reference.templateUuid = templateList.templates[0].uuid

        editor = guiGet { TemplateReferenceEditor(reference) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("selects the referenced template") {
            guiRun {
                editor.loadState(
                    TemplateReference(templateList.templates[2].uuid).also { it.templateList = Box({ templateList }) }
                )
            }

            frame.comboBox("template").requireSelection(templateList.templates[2].toString())
        }

        it("selects nothing if the reference refers to null") {
            guiRun { editor.loadState(TemplateReference().also { it.templateList = Box({ templateList }) }) }

            assertThat(frame.getComboBoxItem<Template>("template")).isNull()
        }

        it("does not load the reference's parent as a selectable option") {
            val box = frame.comboBox("template").target()
            val items = (0 until box.itemCount).map { box.getItemAt(it) as Template }

            assertThat(items).doesNotContain(reference.parent)
        }

        it("loads the scheme's quotation") {
            guiRun {
                reference.quotation = "'"
                editor.loadState(reference)
            }

            frame.comboBox("quotation").requireSelection("'")
        }

        it("loads the scheme's capitalization") {
            guiRun {
                reference.capitalization = CapitalizationMode.RETAIN
                editor.loadState(reference)
            }

            assertThat(frame.getComboBoxItem<CapitalizationMode>("capitalization")).isEqualTo(CapitalizationMode.RETAIN)
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default capitalization if unknown capitalization is selected") {
                guiRun {
                    reference.capitalization = CapitalizationMode.DUMMY
                    editor.loadState(reference)
                }

                assertThat(editor.readState().capitalization).isEqualTo(TemplateReference.DEFAULT_CAPITALIZATION)
            }
        }

        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            guiRun {
                frame.comboBox("template").target().selectedItem = templateList.templates[2]
                frame.comboBox("quotation").target().selectedItem = "`"
                frame.comboBox("capitalization").target().selectedItem = CapitalizationMode.RANDOM
            }

            val readScheme = editor.readState()
            assertThat(readScheme.template).isEqualTo(templateList.templates[2])
            assertThat(readScheme.quotation).isEqualTo("`")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.RANDOM)
        }

        it("returns the loaded state if no editor changes are made") {
            guiRun { frame.comboBox("template").target().selectedItem = templateList.templates[2] }
            assertThat(editor.isModified()).isTrue()

            guiRun { editor.loadState(editor.readState()) }
            assertThat(editor.isModified()).isFalse()

            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns a different instance from the loaded scheme") {
            val readState = editor.readState()

            assertThat(readState)
                .isEqualTo(editor.originalState)
                .isNotSameAs(editor.originalState)
            assertThat(+readState.templateList)
                .isSameAs(+editor.originalState.templateList)
            assertThat(readState.arrayDecorator)
                .isEqualTo(editor.originalState.arrayDecorator)
                .isNotSameAs(editor.originalState.arrayDecorator)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.comboBox("template").target().selectedItem = templateList.templates[2] }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            guiRun {
                editor.loadState(
                    TemplateReference(arrayDecorator = ArrayDecorator(enabled = true))
                        .also { it.templateList = Box({ templateList }) }
                )
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            guiRun { frame.spinner("arrayMinCount").target().value = 59 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
