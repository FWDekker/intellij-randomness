package com.fwdekker.randomness

import com.intellij.openapi.util.JDOMUtil
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe


/**
 * Unit tests for extension functions in `XmlHelpersTest`.
 */
object XmlHelpersTest : FunSpec({
    context("getElements") {
        test("returns all contained elements") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 />
                    <content2></content2>
                    <content3 attribute="value" />
                    <content4 attribute="value"><tag /></content4>
                </tag>
                """.trimIndent()
            )

            val elements = element.getElements()

            elements.map { it.name } shouldContainExactly listOf("content1", "content2", "content3", "content4")
        }
    }

    context("getContentByName") {
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

            element.getContentByName("needle")?.name shouldBe "content2"
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

            element.getContentByName("needle")?.name should beNull()
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

            element.getContentByName("needle")?.name should beNull()
        }
    }

    context("getAttributeValueByName") {
        test("returns value attribute of the child with the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="value" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getAttributeValueByName("needle") shouldBe "value"
        }

        test("returns `null` if no children have the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" value="undesired" />
                    <content2 name="noodle" value="valet" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getAttributeValueByName("needle") should beNull()
        }

        test("returns `null` if multiple children have the given name") {
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

            element.getAttributeValueByName("needle") should beNull()
        }

        test("returns `null` if the child does not have a value attribute") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )

            element.getAttributeValueByName("needle") should beNull()
        }
    }

    context("setAttributeValueByName") {
        test("changes the child's value to the given value") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" value="value" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getAttributeValueByName("needle") shouldBe "value"

            element.setAttributeValueByName("needle", "new-value")

            element.getAttributeValueByName("needle") shouldBe "new-value"
        }

        test("adds an attribute if the child does not have a value attribute") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content name="wrong" value="undesired" />
                    <content name="needle" />
                    <content attribute="value" />
                </tag>
                """.trimIndent()
            )
            element.getAttributeValueByName("needle") should beNull()

            element.setAttributeValueByName("needle", "new-value")

            element.getAttributeValueByName("needle") shouldBe "new-value"
        }

        test("does nothing if no children have the given name") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 name="wrong" value="undesired" />
                    <content2 name="noodle" value="valet" />
                    <content3 attribute="value" />
                </tag>
                """.trimIndent()
            )

            val copy = JDOMUtil.load(JDOMUtil.write(element, ""))
            element.setAttributeValueByName("needle", "new-value")

            JDOMUtil.write(element, "") shouldBe JDOMUtil.write(copy, "")
        }

        test("does nothing if multiple children have the given name") {
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

            val copy = JDOMUtil.load(JDOMUtil.write(element, ""))
            element.setAttributeValueByName("needle", "new-value")

            JDOMUtil.write(element, "") shouldBe JDOMUtil.write(copy, "")
        }
    }

    context("getSingleContent") {
        test("returns the singular child") {
            val element = JDOMUtil.load("<tag><child /></tag>")

            element.getSingleContent()?.name shouldBe "child"
        }

        test("returns `null` if there are no children") {
            val element = JDOMUtil.load("<tag></tag>")

            element.getSingleContent() should beNull()
        }

        test("returns `null` if there are multiple children") {
            val element = JDOMUtil.load(
                """
                <tag>
                    <content1 />
                    <content2 />
                </tag>
                """.trimIndent()
            )

            element.getSingleContent() should beNull()
        }
    }

    context("getContentByPath") {
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

            element.getContentByPath("needle")?.name shouldBe "content2"
        }

        test("returns the singular child if `null` is given") {
            val element = JDOMUtil.load("<tag><child /></tag>")

            element.getContentByPath(null)?.name shouldBe "child"
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

            element.getContentByPath("needle", null, "prong", null)?.name shouldBe "target"
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

            element.getContentByPath("wrong", null, "prong", null)?.name should beNull()
        }
    }
})
