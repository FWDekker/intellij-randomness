package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.intellij.openapi.util.JDOMUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
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
        test("fails for versions below v3.0.0") {
            val xml = JDOMUtil.load("""<component><option name="version" value="2.0.0"/></component>""")

            shouldThrow<IllegalStateException> { settings.loadState(xml) }
        }

        context("v3.1.0 to v3.2.0") {
            test("replaces `type` with `version` in all `UuidScheme`s") {
                val xml = JDOMUtil.load(getTestConfig("/settings-upgrades/v3.1.0-v3.2.0.xml"))
                xml.getSchemes().single().run {
                    getPropertyValue("type") shouldBe "1"
                    getProperty("version") should beNull()
                }

                settings.loadState(xml)

                settings.state.getSchemes().single().run {
                    getProperty("type") should beNull()
                    getPropertyValue("version") shouldBe "1"
                }
            }
        }
    }
})
