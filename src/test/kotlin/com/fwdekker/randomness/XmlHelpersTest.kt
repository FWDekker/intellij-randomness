package com.fwdekker.randomness

import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.template.TemplateList
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.uuid.UuidScheme
import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.xmlb.XmlSerializer.serialize
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.jdom.Element


/**
 * Unit tests for extension functions in `XmlHelpersTest`.
 */
object XmlHelpersTest : FunSpec({
    context("addProperty") {
        test("adds the property with the given name") {
            val element = JDOMUtil.load("<tag></tag>")
            element.getMultiProperty("needle") should beEmpty()

            element.addProperty("needle")

            element.getMultiProperty("needle") should haveSize(1)
        }

        test("adds the property with the given name even if it already exists") {
            val element = JDOMUtil.load("""<tag><content name="needle" /></tag>""")
            element.getMultiProperty("needle") should haveSize(1)

            element.addProperty("needle")

            element.getMultiProperty("needle") should haveSize(2)
        }

        test("adds the property with the given name and value") {
            val element = JDOMUtil.load("<tag></tag>")
            element.getPropertyValue("needle") should beNull()

            element.addProperty("needle", "value")

            element.getPropertyValue("needle") shouldBe "value"
        }
    }

    context("getProperty") {
        test("returns the child with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="needle" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getProperty("needle")?.name shouldBe "content2"
        }

        test("returns `null` if no children have the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="noodle" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getProperty("needle")?.name should beNull()
        }

        test("returns `null` if multiple children have the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="needle" />
                    <content3 name="needle" />
                    <content4 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getProperty("needle")?.name should beNull()
        }
    }

    context("getMultiProperty") {
        test("returns the single child with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="needle" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getMultiProperty("needle").map { it.name } shouldContainExactly listOf("content2")
        }

        test("returns `null` if no children have the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="noodle" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getMultiProperty("needle") should beEmpty()
        }

        test("returns all children that have the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="needle" />
                    <content3 name="needle" />
                    <content4 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getMultiProperty("needle").map { it.name } shouldContainExactly listOf("content2", "content3")
        }
    }

    context("getPropertyByPath") {
        test("returns the child with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="needle" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getPropertyByPath("needle")?.name shouldBe "content2"
        }

        test("returns the singular child if `null` is given") {
            val element = JDOMUtil.load("<tag><child /></tag>")

            element.getPropertyByPath(null)?.name shouldBe "child"
        }

        test("returns the element at the given path") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="needle">
                        <wrapper>
                            <contentA name="prong">
                                <target />
                            </contentA>
                            <contentB name="undesired" />
                        </wrapper>
                    </content2>
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getPropertyByPath("needle", null, "prong", null)?.name shouldBe "target"
        }

        test("returns `null` if no element matches the path") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" />
                    <content2 name="needle">
                        <wrapper>
                            <contentA name="prong">
                                <target />
                            </contentA>
                            <contentB name="undesired" />
                        </wrapper>
                    </content2>
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getPropertyByPath("wrong", null, "prong", null)?.name should beNull()
        }
    }

    context("getPropertyValue") {
        test("returns value of the property with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="desired" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getPropertyValue("needle") shouldBe "desired"
        }

        test("returns `null` if the property does not exist") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" value="undesired" />
                    <content2 name="noodle" value="valet" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getPropertyValue("needle") should beNull()
        }

        test("returns `null` if the property exists multiple times") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" value="undesired" />
                    <content2 name="needle" value="valet" />
                    <content3 name="needle" value="voodoo" />
                    <content4 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getPropertyValue("needle") should beNull()
        }

        test("returns `null` if the property has no value") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getPropertyValue("needle") should beNull()
        }
    }

    context("setPropertyValue") {
        test("adds the property if it does not exist") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getPropertyValue("needle") should beNull()

            element.setPropertyValue("needle", "value")

            element.getPropertyValue("needle") shouldBe "value"
        }

        test("changes the property's value") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="value" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getPropertyValue("needle") shouldBe "value"

            element.setPropertyValue("needle", "new-value")

            element.getPropertyValue("needle") shouldBe "new-value"
        }

        test("adds a value to the property if it does not have one") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getPropertyValue("needle") should beNull()

            element.setPropertyValue("needle", "value")

            element.getPropertyValue("needle") shouldBe "value"
        }

        test("does nothing if multiple children have the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="valet" />
                    <content name="needle" value="voodoo" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )

            val copy = JDOMUtil.load(JDOMUtil.write(element))
            element.setPropertyValue("needle", "new-value")

            JDOMUtil.write(element) shouldBe JDOMUtil.write(copy)
        }
    }

    context("renameProperty") {
        test("renames the property with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="desired" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getPropertyValue("needle") shouldBe "desired"
            element.getPropertyValue("new-needle") should beNull()

            element.renameProperty("needle", "new-needle")

            element.getPropertyValue("needle") should beNull()
            element.getPropertyValue("new-needle") shouldBe "desired"
        }

        test("renames all properties with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="desired1" />
                    <content attribute="value" />
                    <content name="needle" value="desired2" />
                </tag>
                """.trimIndent()
            )
            element.getMultiProperty("needle") should haveSize(2)
            element.getMultiProperty("new-needle") should beEmpty()

            element.renameProperty("needle", "new-needle")

            element.getMultiProperty("needle") should beEmpty()
            element.getMultiProperty("new-needle") should haveSize(2)
        }

        test("does nothing if no property has the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )

            val copy = JDOMUtil.load(JDOMUtil.write(element))
            element.renameProperty("needle", "new-needle")

            JDOMUtil.write(element) shouldBe JDOMUtil.write(copy)
        }

        test("throws an error if the new name is already in use") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="desired1" />
                    <content attribute="value" />
                    <content name="new-needle" value="desired2" />
                </tag>
                """.trimIndent()
            )

            shouldThrow<IllegalArgumentException> { element.renameProperty("needle", "new-needle") }
        }
    }

    context("removeProperty") {
        test("removes the property with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="desired" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getPropertyValue("needle") shouldBe "desired"
            element.getPropertyValue("wrong") shouldBe "undesired"

            element.removeProperty("needle")

            element.getProperty("needle") should beNull()
            element.getPropertyValue("wrong") shouldBe "undesired"
        }

        test("removes all properties with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="needle" value="foo" />
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="bar" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getMultiProperty("needle") should haveSize(2)
            element.getPropertyValue("wrong") shouldBe "undesired"

            element.removeProperty("needle")

            element.getMultiProperty("needle") should beEmpty()
            element.getPropertyValue("wrong") shouldBe "undesired"
        }

        test("does nothing if no property with the given name exists") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="also_wrong" value="also_undesired" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )

            val copy = JDOMUtil.load(JDOMUtil.write(element))
            element.removeProperty("needle")

            JDOMUtil.write(element) shouldBe JDOMUtil.write(copy)
        }
    }


    context("accessors") {
        lateinit var settings: Settings
        lateinit var xml: Element


        beforeNonContainer {
            settings = Settings(
                templateList = TemplateList(
                    mutableListOf(
                        Template(name = "Foo", schemes = mutableListOf(IntegerScheme(), StringScheme())),
                        Template(name = "Bar", schemes = mutableListOf(UuidScheme())),
                    )
                )
            )
            xml = serialize(settings)
        }


        test("getTemplateList") {
            val xmlUuid = xml.getTemplateList()?.getPropertyValue("uuid")
            val realUuid = settings.templateList.uuid

            xmlUuid shouldBe realUuid
        }

        test("getTemplates") {
            val xmlUuids = xml.getTemplates().map { it.getPropertyValue("uuid") }
            val realUuids = settings.templates.map { it.uuid }
            realUuids shouldNot beEmpty()

            xmlUuids shouldContainExactlyInAnyOrder realUuids
        }

        test("getSchemes") {
            val xmlUuids = xml.getSchemes().map { it.getPropertyValue("uuid") }
            val realUuids = settings.templates.flatMap { it.schemes }.map { it.uuid }
            realUuids shouldNot beEmpty()

            xmlUuids shouldContainExactlyInAnyOrder realUuids
        }

        test("getDecorators") {
            val xmlUuids = xml.getDecorators().map { it.getPropertyValue("uuid") }
            val realUuids = settings.templates
                .flatMap { it.schemes }
                .flatMap { it.decorators }
                .flatMap { listOf(it) + it.decorators }
                .map { it.uuid }
            realUuids shouldNot beEmpty()

            xmlUuids shouldContainExactlyInAnyOrder realUuids
        }
    }
})
