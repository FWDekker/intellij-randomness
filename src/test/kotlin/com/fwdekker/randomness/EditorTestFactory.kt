package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.guiRun
import com.intellij.ui.dsl.builder.MutableProperty
import io.kotest.assertions.withClue
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
 * Tests that the fields of a [SchemeEditor] are bound correctly.
 *
 * @param S the type of scheme in the editor
 * @param editor returns the editor to test
 * @param fields the field's name, a property for the field in the editor, and a property for the field in the scheme
 */
fun <S : Scheme> editorFieldsTestFactory(
    editor: () -> SchemeEditor<S>,
    fields: Map<String, Row3<() -> MutableProperty<Any?>, () -> MutableProperty<Any?>, Any?>>,
): TestFactory {
    return funSpec {
        context("bindings") {
            context("'apply' stores the editor's field in the scheme's field") {
                withData(fields) { (editorProperty, schemeProperty, value) ->
                    withClue("Pre: Scheme value should not be set value") { schemeProperty().get() shouldNotBe value }

                    guiRun { editorProperty().set(value) }
                    guiRun { editor().apply() }

                    withClue("Post: Scheme value should be set value") { schemeProperty().get() shouldBe value }
                }
            }

            context("'reset' loads the scheme's field into the editor's field") {
                withData(fields) { (editorProperty, schemeProperty, value) ->
                    withClue("Pre: Editor value should not be set value") { editorProperty().get() shouldNotBe value }

                    schemeProperty().set(value)
                    guiRun { editor().reset() }

                    withClue("Post: Editor value should be set value") { editorProperty().get() shouldBe value }
                }
            }

            context("'addChangeListener' is invoked when the editor's field is changed") {
                withData(fields) { (editorProperty, _, value) ->
                    var invoked = 0
                    editor().addChangeListener { invoked++ }

                    guiRun { editorProperty().set(value) }

                    withClue("Listener should have been invoked") { invoked shouldBeGreaterThanOrEqual 1 }
                }
            }
        }
    }
}
