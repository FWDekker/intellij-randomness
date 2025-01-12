package com.fwdekker.randomness

import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor


/**
 * Tests the [State.deepCopy] method of the scheme created by [createState].
 */
fun <S : State> stateDeepCopyTestFactory(createState: () -> S) =
    funSpec {
        context("deepCopy") {
            lateinit var scheme: S


            beforeNonContainer {
                scheme = createState()
            }


            test("equals old instance") {
                scheme.deepCopy() shouldBe scheme
            }

            test("is independent of old instance") {
                val copy = scheme.deepCopy()

                scheme.uuid = "other"

                copy.uuid shouldNotBe scheme.uuid
            }

            test("retains uuid if chosen") {
                scheme.deepCopy(true).uuid shouldBe scheme.uuid
            }

            test("replaces uuid if chosen") {
                scheme.deepCopy(false).uuid shouldNotBe scheme.uuid
            }
        }
    }

/**
 * Tests that [PersistentSettings] serializes and deserializes [S] correctly without losing user configuration.
 */
fun <S : Scheme> schemeSerializationTestFactory(createScheme: () -> S) =
    funSpec {
        context("(de)serialization") {
            fun validate(scheme: Scheme) {
                val original = PersistentSettings()
                original.settings.templateList.templates.apply {
                    clear()
                    add(scheme.asTemplate())
                }

                val copy = PersistentSettings().apply { loadState(original.state) }
                copy.settings shouldBe original.settings
            }


            test("retains default values") {
                validate(createScheme())
            }

            test("retains changed values, including those in decorators") {
                validate(createScheme().mutate())
            }
        }
    }


/**
 * Mutates every single field of `this` [Scheme] in some way.
 */
@Suppress(
    "UNCHECKED_CAST",
    "detekt:CognitiveComplexMethod",
    "detekt:CyclomaticComplexMethod",
    "detekt:NestedBlockDepth",
)
fun <S : Scheme> S.mutate(): S {
    val msg = { prop: KProperty<*> -> "Property '${prop.name}' has unknown type '${prop.returnType}'." }
    val params = this::class.primaryConstructor!!.parameters.map { it.name!! }.toSet()

    this::class.memberProperties.filter { it.name in params }
        .forEach { prop ->
            val oldValue = prop.getter.call(this)

            if (prop is KMutableProperty<*>) {
                // Overwrite mutable properties
                prop.setter.call(
                    this,
                    when (oldValue) {
                        is Boolean -> !oldValue
                        is Int -> oldValue.inc()
                        is Long -> oldValue.inc()
                        is Float -> oldValue.inc()
                        is Double -> oldValue.inc()
                        is String -> "foo$oldValue"
                        is String? -> "foo"
                        is CapitalizationMode -> CapitalizationMode.entries.let { it[(oldValue.ordinal + 1) % it.size] }
                        is List<*> ->
                            if (oldValue[0] is String) oldValue.map { it as String + "bar" } + "bar"
                            else error(msg(prop))

                        else -> error(msg(prop))
                    }
                )
            } else {
                // Mutate immutable properties in-place
                when (oldValue) {
                    is Scheme -> oldValue.mutate()
                    is MutableList<*> ->
                        if (oldValue[0] is Scheme)
                            (oldValue as MutableList<Scheme>)
                                .onEach { it.mutate() }
                                .add(IntegerScheme())
                        else error(msg(prop))

                    else -> error(msg(prop))
                }
            }
        }

    return this
}

/**
 * Returns a [Template] containing `this` [Scheme]; if `this` is a [DecoratorScheme], `this` is put inside another
 * [Scheme].
 */
fun <S : Scheme> S.asTemplate(): Template =
    if (this is Template) this
    else Template(
        "My Template",
        mutableListOf(
            if (this is DecoratorScheme) {
                val name = this::class.simpleName!!.replaceFirstChar { it.lowercaseChar() }
                val const = IntegerScheme::class.primaryConstructor!!
                const.callBy(mapOf(const.parameters.single { it.name == name } to this))
            } else {
                this
            }
        )
    )


/**
 * Tests that [mutate] actually changes fields.
 */
object MutateTest : FunSpec({
    context("mutate") {
        test("mutates every field of IntegerScheme and its decorators") {
            val before = IntegerScheme()
            val after = IntegerScheme().mutate()

            after shouldNotBe before

            after.minValue shouldNotBe before.minValue
            after.maxValue shouldNotBe before.maxValue
            after.base shouldNotBe before.base
            after.isUppercase shouldNotBe before.isUppercase
            after.groupingSeparatorEnabled shouldNotBe before.groupingSeparatorEnabled
            after.groupingSeparator shouldNotBe before.groupingSeparator

            after.fixedLengthDecorator shouldNotBe before.fixedLengthDecorator
            after.fixedLengthDecorator.enabled shouldNotBe before.fixedLengthDecorator.enabled
            after.fixedLengthDecorator.length shouldNotBe before.fixedLengthDecorator.length
            after.fixedLengthDecorator.filler shouldNotBe before.fixedLengthDecorator.filler

            after.affixDecorator shouldNotBe before.affixDecorator
            after.affixDecorator.enabled shouldNotBe before.affixDecorator.enabled
            after.affixDecorator.descriptor shouldNotBe before.affixDecorator.descriptor

            after.arrayDecorator shouldNotBe before.arrayDecorator
            after.arrayDecorator.enabled shouldNotBe before.arrayDecorator.enabled
            after.arrayDecorator.minCount shouldNotBe before.arrayDecorator.minCount
            after.arrayDecorator.maxCount shouldNotBe before.arrayDecorator.maxCount
            after.arrayDecorator.separatorEnabled shouldNotBe before.arrayDecorator.separatorEnabled
            after.arrayDecorator.separator shouldNotBe before.arrayDecorator.separator

            after.arrayDecorator.affixDecorator shouldNotBe before.arrayDecorator.affixDecorator
            after.arrayDecorator.affixDecorator.enabled shouldNotBe before.arrayDecorator.affixDecorator.enabled
            after.arrayDecorator.affixDecorator.descriptor shouldNotBe before.arrayDecorator.affixDecorator.descriptor
        }
    }
})
