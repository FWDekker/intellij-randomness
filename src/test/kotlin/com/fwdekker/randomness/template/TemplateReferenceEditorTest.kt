package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.array.ArrayDecorator
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import javax.swing.DefaultListModel


/**
 * GUI tests for [TemplateReferenceEditor].
 */
object TemplateReferenceEditorTest : DescribeSpec({
    lateinit var frame: FrameFixture

    lateinit var templateList: TemplateList
    lateinit var reference: TemplateReference
    lateinit var editor: TemplateReferenceEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
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

        editor = GuiActionRunner.execute<TemplateReferenceEditor> { TemplateReferenceEditor(reference) }
        frame = Containers.showInFrame(editor.rootComponent)
    }

    afterEach {
        frame.cleanUp()
    }


    describe("loadState") {
        it("selects the referenced template") {
            GuiActionRunner.execute {
                editor.loadState(
                    TemplateReference(templateList.templates[2].uuid)
                        .also { it.templateList = Box({ templateList }) }
                )
            }

            assertThat(frame.list().target().selectedValue).isEqualTo(templateList.templates[2])
        }

        it("selects nothing if the reference refers to null") {
            GuiActionRunner.execute {
                editor.loadState(TemplateReference().also { it.templateList = Box({ templateList }) })
            }

            assertThat(frame.list().target().selectedValue).isNull()
        }

        @Suppress("UNCHECKED_CAST") // I checked it myself!
        it("does not load the reference's parent as a selectable option") {
            assertThat((frame.list().target().model as DefaultListModel<Template>).elements().toList())
                .doesNotContain(reference.parent)
        }

        it("loads the scheme's quotation") {
            GuiActionRunner.execute {
                reference.quotation = "'"
                editor.loadState(reference)
            }

            frame.radioButton("quotationNone").requireSelected(false)
            frame.radioButton("quotationSingle").requireSelected(true)
            frame.radioButton("quotationDouble").requireSelected(false)
            frame.radioButton("quotationBacktick").requireSelected(false)
            frame.panel("quotationCustom").radioButton().requireSelected(false)
        }

        it("loads the scheme's custom quotation") {
            GuiActionRunner.execute {
                reference.customQuotation = "nL"
                editor.loadState(reference)
            }

            frame.panel("quotationCustom").textBox().requireText("nL")
        }

        it("selects the scheme's custom quotation") {
            GuiActionRunner.execute {
                reference.quotation = "5"
                reference.customQuotation = "5"
                editor.loadState(reference)
            }

            frame.panel("quotationCustom").radioButton().requireSelected()
        }

        it("loads the scheme's capitalization") {
            GuiActionRunner.execute {
                reference.capitalization = CapitalizationMode.RETAIN
                editor.loadState(reference)
            }

            frame.radioButton("capitalizationRetain").requireSelected(true)
            frame.radioButton("capitalizationLower").requireSelected(false)
            frame.radioButton("capitalizationUpper").requireSelected(false)
            frame.radioButton("capitalizationRandom").requireSelected(false)
            frame.radioButton("capitalizationSentence").requireSelected(false)
            frame.radioButton("capitalizationFirstLetter").requireSelected(false)
        }
    }

    describe("readState") {
        describe("defaults") {
            it("returns default quotation if no quotation is selected") {
                GuiActionRunner.execute {
                    reference.quotation = "unsupported"
                    editor.loadState(reference)
                }

                assertThat(editor.readState().quotation).isEqualTo(TemplateReference.DEFAULT_QUOTATION)
            }

            it("returns default capitalization if unknown capitalization is selected") {
                GuiActionRunner.execute {
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
            GuiActionRunner.execute {
                frame.list().target().setSelectedValue(templateList.templates[2], false)
                frame.radioButton("quotationBacktick").target().isSelected = true
                frame.radioButton("capitalizationRandom").target().isSelected = true
            }

            val readScheme = editor.readState()
            assertThat(readScheme.template).isEqualTo(templateList.templates[2])
            assertThat(readScheme.quotation).isEqualTo("`")
            assertThat(readScheme.capitalization).isEqualTo(CapitalizationMode.RANDOM)
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

            GuiActionRunner.execute { frame.list().target().setSelectedValue(templateList.templates[2], false) }

            assertThat(listenerInvoked).isTrue()
        }

        it("invokes the listener if the array decorator changes") {
            GuiActionRunner.execute {
                editor.loadState(
                    TemplateReference(arrayDecorator = ArrayDecorator(enabled = true))
                        .also { it.templateList = Box({ templateList }) }
                )
            }

            var listenerInvoked = false
            editor.addChangeListener { listenerInvoked = true }

            GuiActionRunner.execute { frame.spinner("arrayMinCount").target().value = 59 }

            assertThat(listenerInvoked).isTrue()
        }
    }
})
