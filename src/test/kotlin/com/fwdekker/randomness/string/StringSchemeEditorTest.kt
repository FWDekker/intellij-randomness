package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.afterNonContainer
import com.fwdekker.randomness.beforeNonContainer
import com.fwdekker.randomness.editorApplyTestFactory
import com.fwdekker.randomness.editorFieldsTestFactory
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.isSelectedProp
import com.fwdekker.randomness.itemProp
import com.fwdekker.randomness.prop
import com.fwdekker.randomness.textProp
import com.fwdekker.randomness.valueProp
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [StringSchemeEditor].
 */
object StringSchemeEditorTest : FunSpec({
    tags(NamedTag("Editor"), NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var scheme: StringScheme
    lateinit var editor: StringSchemeEditor


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        scheme = StringScheme()
        editor = guiGet { StringSchemeEditor(scheme) }
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
                "pattern" to
                    row(
                        { frame.textBox("pattern").textProp() },
                        { editor.scheme::pattern.prop() },
                        "[a-z]{3,4}",
                    ),
                "isRegex" to
                    row(
                        { frame.checkBox("isRegex").isSelectedProp() },
                        { editor.scheme::isRegex.prop() },
                        false,
                    ),
                "removeLookAlikeCharacters" to
                    row(
                        { frame.checkBox("removeLookAlikeCharacters").isSelectedProp() },
                        { editor.scheme::removeLookAlikeSymbols.prop() },
                        true,
                    ),
                "capitalization" to
                    row(
                        { frame.comboBox("capitalization").itemProp() },
                        { editor.scheme::capitalization.prop() },
                        CapitalizationMode.RANDOM,
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
