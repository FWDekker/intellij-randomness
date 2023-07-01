package com.fwdekker.randomness.ui

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import javax.swing.text.DocumentFilter
import javax.swing.text.PlainDocument


/**
 * Unit tests for [MaxLengthDocumentFilter].
 */
object MaxLengthDocumentFilterTest : DescribeSpec({
    lateinit var document: PlainDocument
    lateinit var filter: DocumentFilter
    val getText = { document.getText(0, document.length) }


    beforeEach {
        document = PlainDocument()
        filter = MaxLengthDocumentFilter(3)
        document.documentFilter = filter
    }


    describe("constructor") {
        it("throws an exception if `maxLength` is negative") {
            assertThatThrownBy { MaxLengthDocumentFilter(-3) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Maximum length must be a positive number, but was '-3'.")
        }
    }


    describe("insert") {
        it("does nothing if the string is `null`") {
            document.insertString(0, "qib", null)
            document.insertString(0, null, null)

            assertThat(getText()).isEqualTo("qib")
        }

        describe("without overwrite") {
            it("inserts a string of length 0 at offset 0") {
                document.insertString(0, "", null)

                assertThat(getText()).isEqualTo("")
            }

            it("inserts a string of length 0 at offset 1") {
                document.insertString(0, "p", null)
                document.insertString(1, "", null)

                assertThat(getText()).isEqualTo("p")
            }

            it("inserts a string of length 1 into an empty document") {
                document.insertString(0, "w", null)

                assertThat(getText()).isEqualTo("w")
            }

            it("inserts multiple strings after each other") {
                document.insertString(0, "c", null)
                document.insertString(1, "n", null)
                document.insertString(2, "s", null)

                assertThat(getText()).isEqualTo("cns")
            }

            it("inserts a string at the start") {
                document.insertString(0, "lh", null)
                document.insertString(0, "u", null)

                assertThat(getText()).isEqualTo("ulh")
            }

            it("inserts a string in the middle") {
                document.insertString(0, "sc", null)
                document.insertString(1, "f", null)

                assertThat(getText()).isEqualTo("sfc")
            }

            it("inserts a string of maximum length into an empty document") {
                document.insertString(0, "usj", null)

                assertThat(getText()).isEqualTo("usj")
            }
        }

        describe("with overwrite") {
            it("overwrites the last character if a single character is inserted after the end") {
                document.insertString(0, "gqs", null)
                document.insertString(3, "h", null)

                assertThat(getText()).isEqualTo("gqh")
            }

            it("overwrites the last character if multiple characters are inserted after the end") {
                document.insertString(0, "yir", null)
                document.insertString(3, "fps", null)

                assertThat(getText()).isEqualTo("yis")
            }

            it("overwrites the first character") {
                document.insertString(0, "sfb", null)
                document.insertString(0, "m", null)

                assertThat(getText()).isEqualTo("mfb")
            }

            it("overwrites the first two characters") {
                document.insertString(0, "dkq", null)
                document.insertString(0, "av", null)

                assertThat(getText()).isEqualTo("avq")
            }

            it("overwrites the last two characters") {
                document.insertString(0, "gqz", null)
                document.insertString(1, "sp", null)

                assertThat(getText()).isEqualTo("gsp")
            }

            it("overwrites all characters") {
                document.insertString(0, "jyl", null)
                document.insertString(0, "dxh", null)

                assertThat(getText()).isEqualTo("dxh")
            }

            it("overwrites the last character if multiple characters are inserted at offset 2") {
                document.insertString(0, "amj", null)
                document.insertString(2, "vwr", null)

                assertThat(getText()).isEqualTo("amr")
            }

            it("inserts the last `maxLength` characters of the string into an empty document") {
                document.insertString(0, "qauguw", null)

                assertThat(getText()).isEqualTo("qaw")
            }

            it("inserts the characters that still fit at the start and overwrites the remainder") {
                document.insertString(0, "yr", null)
                document.insertString(0, "qn", null)

                assertThat(getText()).isEqualTo("qnr")
            }

            it("inserts the characters that still fit in the middle and overwrites the remainder") {
                document.insertString(0, "ih", null)
                document.insertString(1, "pq", null)

                assertThat(getText()).isEqualTo("ipq")
            }

            it("inserts the characters that still fit, overwrites the remainder, and inserts the last character") {
                document.insertString(0, "pmt", null)
                document.insertString(1, "deo", null)

                assertThat(getText()).isEqualTo("pdo")
            }
        }
    }
})


/**
 * Unit tests for [MinMaxLengthDocumentFilter].
 */
object MinMaxLengthDocumentFilterTest : DescribeSpec({
    lateinit var document: PlainDocument
    lateinit var filter: DocumentFilter
    val getText = { document.getText(0, document.length) }


    beforeEach {
        document = PlainDocument()
        filter = MinMaxLengthDocumentFilter(2, 5)
        document.documentFilter = filter
    }


    describe("constructor") {
        it("throws an exception if `minLength` is greater than `maxLength`") {
            assertThatThrownBy { MinMaxLengthDocumentFilter(4, 2) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Minimum length must be less than or equal to maximum length.")
        }
    }


    describe("insert") {
        it("inserts new characters while the length is below range") {
            GuiActionRunner.execute { document.insertString(0, "yaw", null) }

            assertThat(getText()).isEqualTo("yaw")
        }
    }

    describe("remove") {
        it("removes characters if the result's length is in range") {
            GuiActionRunner.execute { document.insertString(0, "gpck", null) }

            GuiActionRunner.execute { document.remove(1, 2) }

            assertThat(getText()).isEqualTo("gk")
        }

        it("does not remove characters if the result is too short") {
            GuiActionRunner.execute { document.insertString(0, "gpck", null) }

            GuiActionRunner.execute { document.remove(1, 2) }

            assertThat(getText()).isEqualTo("gk")
        }

        it("removes the last characters in the selection if removing more would put result's length out of range") {
            GuiActionRunner.execute { document.insertString(0, "pqmn", null) }

            GuiActionRunner.execute { document.remove(0, 3) }

            assertThat(getText()).isEqualTo("pn")
        }

        it("removes characters if that puts the result's length into range") {
            document.documentFilter = null
            GuiActionRunner.execute { document.insertString(0, "ownqokkbp", null) }
            document.documentFilter = filter

            GuiActionRunner.execute { document.remove(3, 6) }

            assertThat(getText()).isEqualTo("own")
        }
    }
})
