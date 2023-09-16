package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.afterNonContainer
import com.fwdekker.randomness.beforeNonContainer
import com.fwdekker.randomness.editorApplyTestFactory
import com.fwdekker.randomness.editorFieldsTestFactory
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.isSelectedProp
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.textProp
import com.fwdekker.randomness.valueProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.dsl.builder.MutableProperty
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [UuidSchemeEditor].
 */
object UuidSchemeEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: UuidScheme
    lateinit var editor: UuidSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = UuidScheme()
        editor = guiGet { UuidSchemeEditor(scheme) }
        frame = showInFrame(editor.rootComponent)
    }

    afterNonContainer {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    include(editorApplyTestFactory { editor })

    include(
        editorFieldsTestFactory(
            { editor },
            mapOf(
                "type" to
                    row(
                        {
                            MutableProperty(
                                {
                                    when {
                                        frame.radioButton("type1").target().isSelected -> 1
                                        frame.radioButton("type4").target().isSelected -> 4
                                        else -> error("No radio button was selected.")
                                    }
                                },
                                { frame.radioButton("type${it as Int}").target().isSelected = true },
                            )
                        },
                        { editor.scheme::type.prop() },
                        1,
                    ),
                "isUppercase" to
                    row(
                        { frame.checkBox("isUppercase").isSelectedProp() },
                        { editor.scheme::isUppercase.prop() },
                        true,
                    ),
                "addDashes" to
                    row(
                        { frame.checkBox("addDashes").isSelectedProp() },
                        { editor.scheme::addDashes.prop() },
                        false,
                    ),
                "affixDecorator" to
                    row(
                        { frame.comboBox("affixDescriptor").textProp() },
                        { editor.scheme.affixDecorator::descriptor.prop() },
                        "[@]",
                    ),
                "arrayDecorator" to
                    row(
                        { frame.spinner("arrayMaxCount").valueProp() },
                        { editor.scheme.arrayDecorator::maxCount.prop() },
                        7,
                    ),
            )
        )
    )
})
