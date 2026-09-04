package com.fwdekker.randomness.testhelpers

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.fwdekker.randomness.getProperty
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.intellij.util.xmlb.XmlSerializer.serialize
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.util.xmlb.annotations.XCollection
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.tuple
import io.kotest.datatest.withTests
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe


/**
 * Unit tests for extension functions in `StateReflection`.
 */
object StateReflectionTest : FunSpec({
    tags(Tags.PLAIN)


    context("parameters") {
        withTests(
            mapOf(
                "returns an empty list if the constructor is empty and there are no fields" to
                    tuple(SimpleState(), emptyList()),
                "returns all constructor parameters" to
                    tuple(ParametersOnly(), listOf("foo")),
                "does not return the fields" to
                    tuple(FieldsOnly(), listOf()),
                "returns all constructor parameters, but not the fields" to
                    tuple(ParametersAndFields(), listOf("foo")),
                "returns the subclass' constructor parameters only" to
                    tuple(ParametersAndFieldsSub(), listOf("baz")),
            )
        ) { (state, parameters) ->
            state.parameters().callableNames() shouldContainExactlyInAnyOrder parameters
        }
    }

    context("properties") {
        withTests(
            mapOf(
                "returns an empty list if the constructor is empty and there are no fields" to
                    tuple(SimpleState(), emptyList()),
                "returns all constructor parameters" to
                    tuple(ParametersOnly(), listOf("foo")),
                "returns all fields" to
                    tuple(FieldsOnly(), listOf("foo", "bar")),
                "returns all constructor parameters and all fields" to
                    tuple(ParametersAndFields(), listOf("foo", "bar")),
                "returns all constructor parameters and all fields from both subclass and superclass" to
                    tuple(ParametersAndFieldsSub(), listOf("foo", "bar", "baz", "qux")),
            )
        ) { (state, parameters) ->
            val defaults = listOf("context", "uuid", "validators")
            state.properties().callableNames() shouldContainExactlyInAnyOrder defaults + parameters
        }
    }

    context("isTransient") {
        withTests(
            mapOf(
                // Constructor parameter
                "constructor parameter without annotation is not transient" to
                    tuple(TransientAnnotations::constructorNotTransient, false),
                "constructor parameter with `@Transient` is transient" to
                    tuple(TransientAnnotations::constructorTransient, true),
                "constructor parameter with `@field:Transient` is transient" to
                    tuple(TransientAnnotations::constructorFieldTransient, true),
                "constructor parameter with `@get:Transient` is transient" to
                    tuple(TransientAnnotations::constructorGetTransient, true),
                "constructor parameter with `@set:Transient` is transient" to
                    tuple(TransientAnnotations::constructorSetTransient, true),
                // Declared property
                "declared property without annotation is not transient" to
                    tuple(TransientAnnotations::declaredNotTransient, false),
                "declared property with `@Transient` is transient" to
                    tuple(TransientAnnotations::declaredTransient, true),
                "declared property with `@field:Transient` is transient" to
                    tuple(TransientAnnotations::declaredFieldTransient, true),
                "declared property with `@get:Transient` is transient" to
                    tuple(TransientAnnotations::declaredGetTransient, true),
                "declared property with `@set:Transient` is transient" to
                    tuple(TransientAnnotations::declaredSetTransient, true),
                // Inherited property with annotation
                "inherited property without annotation is not transient" to
                    tuple(TransientAnnotations::superNotTransient, false),
                "inherited property with `@Transient` is transient" to
                    tuple(TransientAnnotations::superTransient, true),
                "inherited property with `@field:Transient` is transient" to
                    tuple(TransientAnnotations::superFieldTransient, true),
                "inherited property with `@get:Transient` is transient" to
                    tuple(TransientAnnotations::superGetTransient, true),
                "inherited property with `@set:Transient` is transient" to
                    tuple(TransientAnnotations::superSetTransient, true),
                // Inherited property with annotation, override without annotation
                "inherited property with `@Transient` overridden without annotation is transient" to
                    tuple(TransientAnnotations::onlySuperTransient, true),
                "inherited property with `@field:Transient` overridden without annotation is transient" to
                    tuple(TransientAnnotations::onlySuperFieldTransient, true),
                "inherited property with `@get:Transient` overridden without annotation is transient" to
                    tuple(TransientAnnotations::onlySuperGetTransient, true),
                "inherited property with `@set:Transient` overridden without annotation is transient" to
                    tuple(TransientAnnotations::onlySuperSetTransient, true),
                // Inherited property without annotation, override with annotation
                "inherited property without annotation overridden with `@Transient` is transient" to
                    tuple(TransientAnnotations::onlySubTransient, true),
                "inherited property without annotation overridden with `@field:Transient` is transient" to
                    tuple(TransientAnnotations::onlySubFieldTransient, true),
                "inherited property without annotation overridden with `@get:Transient` is transient" to
                    tuple(TransientAnnotations::onlySubGetTransient, true),
                "inherited property without annotation overridden with `@set:Transient` is transient" to
                    tuple(TransientAnnotations::onlySubSetTransient, true),
                // Known cases
                "detects schemes' uuid field as non-transient" to
                    tuple(DummyScheme::uuid, false),
                "detects schemes' context field as transient" to
                    tuple(DummyScheme::context, true),
                "detects decorators' generator field as transient" to
                    tuple(DummyDecoratorScheme::generator, true),
            )
        ) { (property, isTransient) ->
            property.isTransient() shouldBe isTransient
        }
    }

    context("isSerialized") {
        val cases = mapOf(
            // No annotation
            "immutable primitive is not serialized" to
                tuple(TransientSerialized::valInt, false),
            "immutable `Scheme` is not serialized" to
                tuple(TransientSerialized::valScheme, false),
            "immutable `List` is not serialized" to
                tuple(TransientSerialized::valList, false),
            "immutable `MutableList` is not serialized" to
                tuple(TransientSerialized::valMutableList, false),
            "mutable primitive is serialized" to
                tuple(TransientSerialized::varInt, true),
            "mutable `Scheme` is serialized" to
                tuple(TransientSerialized::varScheme, true),
            "mutable `List` is serialized" to
                tuple(TransientSerialized::varList, true),
            "mutable `MutableList` is serialized" to
                tuple(TransientSerialized::varMutableList, true),
            // @OptionTag
            "immutable `List` with @OptionTag is serialized" to
                tuple(TransientSerialized::valListOptionTag, true),
            "immutable `MutableList` with @OptionTag is serialized" to
                tuple(TransientSerialized::valMutableListOptionTag, true),
            "mutable primitive with @OptionTag is serialized" to
                tuple(TransientSerialized::varIntOptionTag, true),
            "mutable `Scheme` with @OptionTag is serialized" to
                tuple(TransientSerialized::varSchemeOptionTag, true),
            "mutable `List` with @OptionTag is serialized" to
                tuple(TransientSerialized::varListOptionTag, true),
            "mutable `MutableList` with @OptionTag is serialized" to
                tuple(TransientSerialized::varMutableListOptionTag, true),
            // @XCollection
            "immutable `List` with @XCollection is serialized" to
                tuple(TransientSerialized::valListXCollection, true),
            "immutable `MutableList` with @XCollection is serialized" to
                tuple(TransientSerialized::valMutableListXCollection, true),
            "mutable `List` with @XCollection is serialized" to
                tuple(TransientSerialized::varListXCollection, true),
            "mutable `MutableList` with @XCollection is serialized" to
                tuple(TransientSerialized::varMutableListXCollection, true),
        )

        context("isSerialized method") {
            withTests(cases) { (property, isSerialized) ->
                property.isSerialized() shouldBe isSerialized
            }
        }

        context("is actually serialized") {
            withTests(cases) { (property, _) ->
                val xml = serialize(TransientSerialized())
                val xmlHasProperty = xml.getProperty(property.name) != null

                property.isSerialized() shouldBe xmlHasProperty
            }
        }
    }

    context("mutated") {
        withTests(
            mapOf(
                // Primitive(-ish)
                "null" to tuple(null, "foo"),
                "false" to tuple(false, true),
                "true" to tuple(true, false),
                "integer" to tuple(79, 80),
                "long" to tuple(448L, 449L),
                "float" to tuple(181.57f, 182.57f),
                "double" to tuple(995.67, 996.67),
                "string" to tuple("frank", "foo_frank"),
                "enum" to tuple(CapitalizationMode.UPPER, CapitalizationMode.LOWER),
                // Scheme
                "scheme with mutable parameters" to
                    tuple(SchemeMutable(0, "bar"), SchemeMutable(1, "foo_bar")),
                "scheme with immutable parameters" to
                    tuple(SchemeImmutable(0, "bar"), SchemeImmutable(0, "bar")),
                "scheme with val immutable list" to
                    tuple(SchemeValImmutableList(listOf(0)), SchemeValImmutableList(listOf(0))),
                "scheme with val mutable list" to
                    tuple(SchemeValMutableList(mutableListOf(0)), SchemeValMutableList(mutableListOf(0))),
                "scheme with var immutable list" to
                    tuple(SchemeVarImmutableList(listOf(0)), SchemeVarImmutableList(listOf(1, 0))),
                "scheme with var mutable list" to
                    tuple(SchemeVarMutableList(mutableListOf(0)), SchemeVarMutableList(mutableListOf(1, 0))),
                "scheme with some transient fields" to
                    tuple(SchemeTransient(foo = 0, bar = 0), SchemeTransient(foo = 0, bar = 1)),
                // List
                "string list" to tuple(listOf("a", "b"), listOf("foo_a", "foo_b", "foo")),
                "scheme list" to tuple(
                    listOf(IntegerScheme(), StringScheme()),
                    listOf(IntegerScheme().mutated(), StringScheme().mutated(), IntegerScheme())
                ),
            )
        ) { (before, after) ->
            before.mutated() shouldBe after
        }
    }
})


/**
 * A [com.fwdekker.randomness.State] that cannot [deepCopy]. Purely for tests.
 */
internal open class SimpleState : State() {
    override fun deepCopy(retainUuid: Boolean): State = error("Not implemented.")
}


internal class FieldsOnly : SimpleState() {
    var foo: Int = 0
    val bar: Int = 0
}

internal class ParametersOnly(var foo: Int = 0) : SimpleState()

internal class ParametersAndFields(var foo: Int = 0) : SimpleState() {
    var bar: Int = 0
}

internal open class ParametersSuper(var foo: Int = 0) : SimpleState() {
    var bar: Int = 0
}

internal class ParametersAndFieldsSub(var baz: Int = 0) : ParametersSuper() {
    var qux: Int = 0
}

internal open class TransientAnnotationsSuper {
    var superNotTransient: Int = 0

    @Transient
    var superTransient: Int = 0

    @field:Transient
    var superFieldTransient: Int = 0

    @get:Transient
    var superGetTransient: Int = 0

    @set:Transient
    var superSetTransient: Int = 0

    @Transient
    open var onlySuperTransient: Int = 0

    @field:Transient
    open var onlySuperFieldTransient: Int = 0

    @get:Transient
    open var onlySuperGetTransient: Int = 0

    @set:Transient
    open var onlySuperSetTransient: Int = 0

    open var onlySubTransient: Int = 0

    open var onlySubFieldTransient: Int = 0

    open var onlySubGetTransient: Int = 0

    open var onlySubSetTransient: Int = 0
}

internal class TransientAnnotations(
    var constructorNotTransient: Int = 0,
    @Transient var constructorTransient: Int = 0,
    @field:Transient var constructorFieldTransient: Int = 0,
    @get:Transient var constructorGetTransient: Int = 0,
    @set:Transient var constructorSetTransient: Int = 0,
) : TransientAnnotationsSuper() {
    var declaredNotTransient: Int = 0

    @Transient
    var declaredTransient: Int = 0

    @field:Transient
    var declaredFieldTransient: Int = 0

    @get:Transient
    var declaredGetTransient: Int = 0

    @set:Transient
    var declaredSetTransient: Int = 0

    override var onlySuperTransient: Int = 0

    override var onlySuperFieldTransient: Int = 0

    override var onlySuperGetTransient: Int = 0

    override var onlySuperSetTransient: Int = 0

    @Transient
    override var onlySubTransient: Int = 0

    @field:Transient
    override var onlySubFieldTransient: Int = 0

    @get:Transient
    override var onlySubGetTransient: Int = 0

    @set:Transient
    override var onlySubSetTransient: Int = 0
}

internal class TransientSerialized : SimpleState() {
    val valInt: Int = 0

    val valScheme: Scheme = IntegerScheme()

    val valList: List<String> = listOf("foo", "bar")

    val valMutableList: MutableList<String> = mutableListOf("foo", "bar")

    var varInt: Int = 0

    var varScheme: Scheme = IntegerScheme()

    var varList: List<String> = listOf("foo", "bar")

    var varMutableList: MutableList<String> = mutableListOf("foo", "bar")


    @get:OptionTag
    val valListOptionTag: List<String> = listOf("foo", "bar")

    @get:OptionTag
    val valMutableListOptionTag: MutableList<String> = mutableListOf("foo", "bar")

    @get:OptionTag
    var varIntOptionTag: Int = 0

    @get:OptionTag
    var varSchemeOptionTag: Scheme = IntegerScheme()

    @get:OptionTag
    var varListOptionTag: List<String> = listOf("foo", "bar")

    @get:OptionTag
    var varMutableListOptionTag: MutableList<String> = mutableListOf("foo", "bar")


    @get:XCollection
    val valListXCollection: List<String> = listOf("foo", "bar")

    @get:XCollection
    val valMutableListXCollection: MutableList<String> = mutableListOf("foo", "bar")

    @get:XCollection
    var varListXCollection: List<String> = listOf("foo", "bar")

    @get:XCollection
    var varMutableListXCollection: MutableList<String> = mutableListOf("foo", "bar")
}

internal data class SchemeMutable(var foo: Int = 0, var bar: String = "bar") : SimpleState()

internal data class SchemeImmutable(val foo: Int = 0, val bar: String = "bar") : SimpleState()

internal data class SchemeValImmutableList(val list: List<Int> = listOf(0)) : SimpleState()

internal data class SchemeValMutableList(val list: MutableList<Int> = mutableListOf(0)) : SimpleState()

internal data class SchemeVarImmutableList(var list: List<Int> = listOf(0)) : SimpleState()

internal data class SchemeVarMutableList(var list: MutableList<Int> = mutableListOf(0)) : SimpleState()

internal data class SchemeTransient(@Transient var foo: Int = 0, var bar: Int = 0) : SimpleState()
