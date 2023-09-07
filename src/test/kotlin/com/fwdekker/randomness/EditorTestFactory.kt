package com.fwdekker.randomness

import com.intellij.ui.dsl.builder.MutableProperty
import io.kotest.core.factory.TestFactory
import io.kotest.core.spec.style.funSpec
import io.kotest.data.Row3
import io.kotest.datatest.withData
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


/**
 * Tests that the [editor] makes no changes by default.
 */
fun editorApplyTestFactory(editor: () -> SchemeEditor<*>) = funSpec {
    context("apply") {
        test("makes no changes by default") {
            val before = editor().scheme.deepCopy(retainUuid = true)

            guiRun { editor().apply() }

            before shouldBe editor().scheme
        }
    }
}

/**
 * Tests that the [SchemeEditor.apply] method
 */
fun <S : Scheme, T : Any?> editorFieldsTestFactory(
    editor: () -> SchemeEditor<S>,
    fields: Map<String, Row3<() -> MutableProperty<T>, () -> MutableProperty<T>, T>>,
): TestFactory {
    return funSpec {
        context("bindings") {
            context("'apply' stores the editor's field in the scheme's field") {
                withData(fields) { (editorProperty, schemeProperty, value) ->
                    schemeProperty().get() shouldNotBe value

                    guiRun { editorProperty().set(value) }
                    guiRun { editor().apply() }

                    schemeProperty().get() shouldBe value
                }
            }

            context("'reset' loads the scheme's field into the editor's field") {
                withData(fields) { (editorProperty, schemeProperty, value) ->
                    guiGet { editorProperty().get() } shouldNotBe value

                    schemeProperty().set(value)
                    guiRun { editor().reset() }

                    guiGet { editorProperty().get() } shouldBe value
                }
            }

            context("'addChangeListener' is invoked when the editor's field is changed") {
                withData(fields) { (editorProperty, _, value) ->
                    var invoked = 0
                    editor().addChangeListener { invoked++ }

                    guiRun { editorProperty().set(value) }

                    invoked shouldBeGreaterThanOrEqual 1
                }
            }
        }
    }
}
