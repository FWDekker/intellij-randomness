package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.intellij.openapi.util.JDOMUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldStartWith
import java.lang.module.ModuleDescriptor.Version
import java.net.URL


/**
 * Unit tests for [PersistentSettings].
 */
object PersistentSettingsTest : FunSpec({
    lateinit var settings: PersistentSettings

    fun getTestConfig(path: String): URL = javaClass.getResource(path)!!


    beforeNonContainer {
        settings = PersistentSettings()
    }


    context("version upgrades") {
        context("input validation") {
            test("fails if the target version is below v3.0.0") {
                val stored = JDOMUtil.load("""<component><option name="version" value="3.0.0"/></component>""")

                shouldThrow<IllegalArgumentException> { settings.upgrade(stored, Version.parse("2.0.0")) }
                    .message shouldStartWith "Unsupported upgrade target version"
            }

            test("fails if the stored version below v3.0.0") {
                val stored = JDOMUtil.load("""<component><option name="version" value="2.0.0"/></component>""")

                shouldThrow<IllegalArgumentException> { settings.upgrade(stored) }
                    .message shouldStartWith "Unsupported Randomness config version"
            }

            test("fails if the stored version is missing") {
                val stored = JDOMUtil.load("""<component></component>""")

                shouldThrow<IllegalArgumentException> { settings.upgrade(stored) }
                    .message shouldStartWith "Missing version number"
            }
        }

        context("generic behaviour") {
            test("bumps the version number if the target version is higher than the stored version") {
                val stored = JDOMUtil.load("""<component><option name="version" value="9.9.8"/></component>""")
                stored.getPropertyValue("version") shouldBe "9.9.8"

                val patched = settings.upgrade(stored, Version.parse("9.9.9"))
                patched.getPropertyValue("version") shouldBe "9.9.9"
            }

            test("does not bump the version number if the target version is lower than the stored version") {
                val stored = JDOMUtil.load("""<component><option name="version" value="9.9.8"/></component>""")
                stored.getPropertyValue("version") shouldBe "9.9.8"

                val patched = settings.upgrade(stored, Version.parse("9.9.7"))
                patched.getPropertyValue("version") shouldBe "9.9.8"
            }

            test("applies multiple upgrades in sequence if needed") {
                val stored = JDOMUtil.load(getTestConfig("/settings-upgrades/v3.1.0-v3.3.5.xml"))
                stored.getSchemes().single().run {
                    getPropertyValue("type") shouldBe "1"
                    getProperty("version") should beNull()
                }
                stored.getDecorators() shouldNot beEmpty()
                stored.getDecorators().forEach { it.getProperty("generator") shouldNot beNull() }

                val patched = settings.upgrade(stored, Version.parse("3.3.5"))
                patched.getSchemes().single().run {
                    getProperty("type") should beNull()
                    getPropertyValue("version") shouldBe "1"
                }
                patched.getDecorators() shouldNot beEmpty()
                patched.getDecorators().forEach { it.getProperty("generator") should beNull() }
            }

            test("upgrades only up to the specified version") {
                val stored = JDOMUtil.load(getTestConfig("/settings-upgrades/v3.1.0-v3.3.5.xml"))
                stored.getSchemes().single().run {
                    getPropertyValue("type") shouldBe "1"
                    getProperty("version") should beNull()
                }
                stored.getDecorators() shouldNot beEmpty()
                stored.getDecorators().forEach { it.getProperty("generator") shouldNot beNull() }

                val patched = settings.upgrade(stored, Version.parse("3.2.0"))
                patched.getSchemes().single().run {
                    getProperty("type") should beNull()
                    getPropertyValue("version") shouldBe "1"
                }
                stored.getDecorators() shouldNot beEmpty()
                stored.getDecorators().forEach { it.getProperty("generator") shouldNot beNull() }
            }
        }

        context("specific upgrades") {
            context("v3.1.0 to v3.2.0") {
                test("replaces `type` with `version` in all `UuidScheme`s") {
                    val stored = JDOMUtil.load(getTestConfig("/settings-upgrades/v3.1.0-v3.2.0.xml"))
                    stored.getSchemes().single().run {
                        getPropertyValue("type") shouldBe "1"
                        getProperty("version") should beNull()
                    }

                    val patched = settings.upgrade(stored, Version.parse("3.2.0"))
                    patched.getSchemes().single().run {
                        getProperty("type") should beNull()
                        getPropertyValue("version") shouldBe "1"
                    }
                }
            }

            context("v3.3.4 to v3.3.5") {
                test("removes `generator` fields from all decorators") {
                    val stored = JDOMUtil.load(getTestConfig("/settings-upgrades/v3.3.4-v3.3.5.xml"))
                    stored.getDecorators() shouldNot beEmpty()
                    stored.getDecorators().forEach { it.getProperty("generator") shouldNot beNull() }

                    val patched = settings.upgrade(stored, Version.parse("3.3.5"))
                    patched.getDecorators() shouldNot beEmpty()
                    patched.getDecorators().forEach { it.getProperty("generator") should beNull() }
                }
            }
        }
    }
})
