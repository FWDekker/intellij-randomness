package com.fwdekker.randomness.testhelpers

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeEditor
import com.intellij.openapi.application.edtWriteAction
import com.intellij.ui.dsl.builder.MutableProperty
import io.kotest.assertions.withClue
import io.kotest.core.Tuple3
import io.kotest.core.factory.TestFactory
import io.kotest.core.spec.style.funSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


/**
 * Returns tests verifying that the [editor] makes no changes by default.
 */
fun editorApplyTests(editor: () -> SchemeEditor<*>) =
    funSpec {
        context("apply") {
            test("makes no changes by default") {
                val before = editor().scheme.deepCopy(retainUuid = true)

                edtWriteAction { editor().apply() }

                before shouldBe editor().scheme
            }
        }
    }

/**
 * Returns tests verifying that the fields of a [SchemeEditor] are bound correctly.
 *
 * @param S the type of scheme in the editor
 * @param editor returns the editor to test
 * @param fields maps the fields' names to a lambda that gives a property for the field in the editor, a property for
 * the field in the scheme and a value to set into either field
 */
fun <S : Scheme> editorFieldsTests(
    editor: () -> SchemeEditor<S>,
    fields: Map<String, () -> Tuple3<MutableProperty<Any?>, MutableProperty<Any?>, Any?>>,
): TestFactory =
    funSpec {
        context("bindings") {
            context("'apply' stores the editor's field in the scheme's field") {
                withTests(fields) { tuple ->
                    val (editorProperty, schemeProperty, value) = tuple()

                    withClue("Pre: Scheme value should not be set value") { schemeProperty.get() shouldNotBe value }

                    edtWriteAction { editorProperty.set(value) }
                    edtWriteAction { editor().apply() }

                    withClue("Post: Scheme value should be set value") { schemeProperty.get() shouldBe value }
                }
            }

            context("'reset' loads the scheme's field into the editor's field") {
                withTests(fields) { tuple ->
                    val (editorProperty, schemeProperty, value) = tuple()

                    withClue("Pre: Editor value should not be set value") { editorProperty.get() shouldNotBe value }

                    schemeProperty.set(value)
                    edtWriteAction { editor().reset() }

                    withClue("Post: Editor value should be set value") { editorProperty.get() shouldBe value }
                }
            }

            context("'addChangeListener' is invoked when the editor's field is changed") {
                withTests(fields) { tuple ->
                    val (editorProperty, _, value) = tuple()

                    var invoked = 0
                    editor().addChangeListener { invoked++ }

                    edtWriteAction { editorProperty.set(value) }

                    withClue("Listener should have been invoked") { invoked shouldBeGreaterThanOrEqual 1 }
                }
            }
        }
    }
