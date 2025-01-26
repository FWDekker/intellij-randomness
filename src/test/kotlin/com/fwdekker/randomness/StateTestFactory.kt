package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.intellij.util.xmlb.XmlSerializer.deserialize
import com.intellij.util.xmlb.XmlSerializer.serialize
import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


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
        fun <T> reserialize(any: Any, clz: Class<T>): T =
            deserialize(serialize(deserialize(serialize(any), clz)), clz)


        context("(de)serialization") {
            test("mutation is not a no-op") {
                createScheme().mutated() shouldNotBe createScheme()
            }

            test("retains default values") {
                val scheme = createScheme()
                reserialize(createScheme(), scheme::class.java) shouldBe scheme
            }

            test("retains modified values") {
                val scheme = createScheme().mutated()
                reserialize(scheme, scheme::class.java) shouldBe scheme
            }

            test("serializes exactly the properties for which `isSerialized` holds") {
                val scheme = createScheme()
                val xml = serialize(scheme)

                val beanProps = scheme.properties().filter { it.isSerialized() }.map { it.name }
                    .map { it.removePrefix("is").lowerCaseFirst() }
                val xmlProps = xml.children.mapNotNull { it.getAttribute("name")?.value }

                xmlProps shouldContainExactlyInAnyOrder beanProps
            }
        }
    }
