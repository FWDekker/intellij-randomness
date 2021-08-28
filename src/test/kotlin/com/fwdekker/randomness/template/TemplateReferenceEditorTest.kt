package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.array.ArraySchemeDecorator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.DefaultListModel


/**
 * GUI tests for [TemplateReferenceEditor].
 */
object TemplateReferenceEditorTest : Spek({
    lateinit var frame: FrameFixture

    lateinit var templateList: TemplateList
    lateinit var reference: TemplateReference
    lateinit var editor: TemplateReferenceEditor


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        templateList = TemplateList(
            mutableListOf(
                Template("cup", mutableListOf(DummyScheme())),
                Template("instead", mutableListOf(TemplateReference())),
                Template("gun", mutableListOf(DummyScheme()))
            )
        )

        reference = templateList.templates[1].schemes[0] as TemplateReference
        reference.templateList = Box({ templateList })
        reference.templateUuid = templateList.templates[0].uuid

        editor = GuiActionRunner.execute<TemplateReferenceEditor> { TemplateReferenceEditor(reference) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loadScheme") {
        it("selects the referenced template") {
            GuiActionRunner.execute {
                editor.loadState(
                    TemplateReference(templateList.templates[2].uuid)
                        .also { it.templateList = Box({ templateList }) }
                )
            }

            assertThat(frame.list().target().selectedValue).isEqualTo(templateList.templates[2])
        }

        it("selects the first template if the reference refers to null") {
            GuiActionRunner.execute {
                editor.loadState(TemplateReference().also { it.templateList = Box({ templateList }) })
            }

            assertThat(frame.list().target().selectedValue).isEqualTo(templateList.templates[0])
        }

        @Suppress("UNCHECKED_CAST") // I checked it myself!
        it("does not load the reference's parent as a selectable option") {
            assertThat((frame.list().target().model as DefaultListModel<Template>).elements().toList())
                .doesNotContain(reference.parent)
        }
    }

    describe("readScheme") {
        it("returns the original state if no editor changes are made") {
            assertThat(editor.readState()).isEqualTo(editor.originalState)
        }

        it("returns the editor's state") {
            GuiActionRunner.execute { frame.list().target().setSelectedValue(templateList.templates[2], false) }

            assertThat(editor.readState().template).isEqualTo(templateList.templates[2])
        }

        it("returns the loaded state if no editor changes are made") {
            GuiActionRunner.execute { frame.list().target().setSelectedValue(templateList.templates[2], false) }
            assertThat(editor.isModified()).isTrue()

            GuiActionRunner.execute { editor.loadState(editor.readState()) }
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
            assertThat(readState.decorator)
                .isEqualTo(editor.originalState.decorator)
                .isNotSameAs(editor.originalState.decorator)
        }

        it("retains the scheme's UUID") {
            assertThat(editor.readState().uuid).isEqualTo(editor.originalState.uuid)
        }
    }


    describe("addChangeListener") {
        it("invokes the listener if a field changes") {
            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.list().target().setSelectedValue(templateList.templates[2], false) }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(
                    TemplateReference(decorator = ArraySchemeDecorator(enabled = true))
                        .also { it.templateList = Box({ templateList }) }
                )
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayCount").target().value = 59 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})