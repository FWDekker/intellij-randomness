package com.fwdekker.randomness.word

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.fail
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Returns `true` if the current operating system is Windows.
 *
 * @return `true` if the current operating system is Windows
 */
fun isWindows() = System.getProperty("os.name").toLowerCase().contains("win")


/**
 * Unit tests for [Dictionaries][Dictionary].
 */
object DictionaryTest : Spek({
    describe("BundledDictionary") {
        describe("validation") {
            it("passes if the file exists") {
                val dictionary = BundledDictionary.cache.get("dictionaries/simple.dic", false)

                assertThat(dictionary.isValid()).isTrue()
                assertThatCode { dictionary.validate() }.doesNotThrowAnyException()
            }

            it("fails if the file does not exist") {
                val dictionary = BundledDictionary.cache.get("does_not_exist.dic", false)

                assertThat(dictionary.isValid()).isFalse()
                assertThatThrownBy { dictionary.validate() }
                    .isInstanceOf(InvalidDictionaryException::class.java)
                    .hasMessage("File not found.")
            }
        }

        describe("words") {
            it("fails if the file does not exist") {
                val dictionary = BundledDictionary.cache.get("does_not_exist.dic", false)

                assertThatThrownBy { dictionary.words }
                    .isInstanceOf(InvalidDictionaryException::class.java)
                    .hasMessage("File not found.")
            }

            it("is an empty list if the dictionary is empty") {
                val dictionary = BundledDictionary.cache.get("dictionaries/empty.dic", false)

                assertThat(dictionary.words).isEmpty()
            }

            it("contains the words of a non-empty dictionary file") {
                val dictionary = BundledDictionary.cache.get("dictionaries/simple.dic", false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow")
            }

            it("does not contain duplicate words") {
                val dictionary = BundledDictionary.cache.get("dictionaries/duplicates.dic", false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("dog", "woof", "cat", "meow")
            }

            it("ignores empty lines") {
                val dictionary = BundledDictionary.cache.get("dictionaries/empty-lines.dic", false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("woof", "meow")
            }

            it("ignores commented lines") {
                val dictionary = BundledDictionary.cache.get("dictionaries/comments.dic", false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("cat", "mouse", "tree")
            }
        }

        describe("toString") {
            it("returns a human-readable string of the dictionary's filename") {
                val dictionary = BundledDictionary.cache.get("dictionaries/simple.dic", false)

                assertThat(dictionary.toString()).isEqualTo("dictionaries/simple.dic")
            }

            it("works even if the file does not exist") {
                val dictionary = BundledDictionary.cache.get("does_not_exist.dic", false)

                assertThat(dictionary.toString()).isEqualTo("does_not_exist.dic")
            }
        }

        describe("equals + hash code") {
            it("equals itself") {
                val dictionary = BundledDictionary.cache.get("dictionary.dic")

                assertThat(dictionary).isEqualTo(dictionary)
                assertThat(dictionary.hashCode()).isEqualTo(dictionary.hashCode())
            }

            it("equals a dictionary with the same filename") {
                val dictionary1 = BundledDictionary.cache.get("dictionary.dic")
                val dictionary2 = BundledDictionary.cache.get("dictionary.dic")

                assertThat(dictionary1).isEqualTo(dictionary2)
                assertThat(dictionary1.hashCode()).isEqualTo(dictionary2.hashCode())
            }

            it("does not equal a user dictionary with the same filename") {
                val dictionary1 = BundledDictionary.cache.get("dictionary.dic")
                val dictionary2 = UserDictionary.cache.get("dictionary.dic")

                assertThat(dictionary1).isNotEqualTo(dictionary2)
            }

            it("does not equal a different object") {
                val dictionary = BundledDictionary.cache.get("dictionary.dic")
                val other = Any()

                assertThat(dictionary).isNotEqualTo(other)
            }
        }
    }

    describe("UserDictionary") {
        val tempFileHelper = TempFileHelper()


        afterGroup {
            tempFileHelper.cleanUp()
        }


        describe("validation") {
            it("passes if the file exists") {
                val dictionaryFile = tempFileHelper.createFile("ladder\nkempt\npork", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.isValid()).isTrue()
                assertThatCode { dictionary.validate() }.doesNotThrowAnyException()
            }

            it("fails if the file does not exist") {
                val dictionary = UserDictionary.cache.get("does_not_exist.dic", false)

                assertThat(dictionary.isValid()).isFalse()
                assertThatThrownBy { dictionary.validate() }
                    .isInstanceOf(InvalidDictionaryException::class.java)
                    .hasMessage("File not found.")
            }

            it("fails if the file is deleted after construction of the dictionary") {
                val dictionaryFile = tempFileHelper.createFile("blizzard\nflames\ninvest", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                if (!dictionaryFile.delete())
                    fail("Failed to delete file as part of test.")

                assertThat(dictionary.isValid()).isFalse()
                assertThatThrownBy { dictionary.validate() }
                    .isInstanceOf(InvalidDictionaryException::class.java)
                    .hasMessage("File not found.")
            }

            it("fails if the file exists but cannot be accessed") {
                if (isWindows()) return@it // setReadable does not work in Windows

                val dictionaryFile = tempFileHelper.createFile("ladder\nkempt\npork", ".dic")
                    .also { it.setReadable(false) }
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.isValid()).isFalse()
                assertThatThrownBy { dictionary.validate() }
                    .isInstanceOf(InvalidDictionaryException::class.java)
                    .hasMessage("File unreadable.")
            }
        }

        describe("words") {
            it("fails if the file does not exist") {
                val dictionary = UserDictionary.cache.get("does_not_exist.dic", false)

                assertThatThrownBy { dictionary.words }
                    .isInstanceOf(InvalidDictionaryException::class.java)
                    .hasMessage("File not found.")
            }

            it("is an empty list if the dictionary is empty") {
                val dictionaryFile = tempFileHelper.createFile("", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.words).isEmpty()
            }

            it("contains the words of a non-empty dictionary file") {
                val dictionaryFile = tempFileHelper.createFile("batmen\njollity\nbolts", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("batmen", "jollity", "bolts")
            }

            it("does not contain duplicate words") {
                val dictionaryFile = tempFileHelper.createFile("dolphins\nmappings\ndolphins\nflat", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("dolphins", "mappings", "flat")
            }

            it("ignores empty lines") {
                val dictionaryFile = tempFileHelper.createFile("\n\nwoof\nmeow\n\n\n", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("woof", "meow")
            }

            it("ignores commented lines") {
                val dictionaryFile = tempFileHelper.createFile("# Comment\nflaming\nlove\n# Destiny\n", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.words).containsExactlyInAnyOrder("flaming", "love")
            }
        }

        describe("toString") {
            it("returns a human-readable string of the dictionary's filename") {
                val dictionaryFile = tempFileHelper.createFile("mode\ndepeche", ".dic")
                val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

                assertThat(dictionary.toString()).endsWith(".dic")
            }

            it("works even if the file does not exist") {
                val dictionary = UserDictionary.cache.get("does_not_exist.dic", false)

                assertThat(dictionary.toString()).isEqualTo("does_not_exist.dic")
            }
        }

        describe("equals + hash code") {
            it("equals itself") {
                val dictionary = UserDictionary.cache.get("dictionary.dic")

                assertThat(dictionary).isEqualTo(dictionary)
                assertThat(dictionary.hashCode()).isEqualTo(dictionary.hashCode())
            }

            it("equals a dictionary with the same filename") {
                val dictionary1 = UserDictionary.cache.get("dictionary.dic")
                val dictionary2 = UserDictionary.cache.get("dictionary.dic")

                assertThat(dictionary1).isEqualTo(dictionary2)
                assertThat(dictionary1.hashCode()).isEqualTo(dictionary2.hashCode())
            }

            it("does not equal a user dictionary with the same filename") {
                val dictionary1 = UserDictionary.cache.get("dictionary.dic")
                val dictionary2 = BundledDictionary.cache.get("dictionary.dic")

                assertThat(dictionary1).isNotEqualTo(dictionary2)
            }

            it("does not equal a different object") {
                val dictionary = UserDictionary.cache.get("dictionary.dic")
                val other = Any()

                assertThat(dictionary).isNotEqualTo(other)
            }
        }
    }
})

/**
 * Unit tests for [DictionaryReference]s.
 */
object DictionaryReferenceTest : Spek({
    val tempFileHelper = TempFileHelper()


    beforeEachTest {
        BundledDictionary.cache.clear()
        UserDictionary.cache.clear()
    }

    afterGroup {
        tempFileHelper.cleanUp()
    }


    describe("instantiation") {
        it("creates a bundled dictionary reference") {
            val dictionary = BundledDictionary.cache.get("bundled.dic")

            val reference = DictionaryReference.to(dictionary)

            assertThat(reference.isBundled).isTrue()
            assertThat(reference.filename).isEqualTo("bundled.dic")
            assertThat(reference.referent).isEqualTo(dictionary)
        }

        it("creates a user dictionary reference") {
            val dictionary = UserDictionary.cache.get("user.dic")

            val reference = DictionaryReference.to(dictionary)

            assertThat(reference.isBundled).isFalse()
            assertThat(reference.filename).isEqualTo("user.dic")
            assertThat(reference.referent).isEqualTo(dictionary)
        }

        it("fails for other dictionary types") {
            val dictionary = object : Dictionary {
                override val words = setOf("word")

                override fun validate() = Unit
            }

            assertThatThrownBy { DictionaryReference.to(dictionary) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage(DictionaryReference.DICTIONARY_CAST_EXCEPTION)
        }
    }

    describe("validation") {
        it("fails for an invalid dictionary") {
            if (isWindows()) return@it // setReadable does not work in Windows

            val dictionaryFile = tempFileHelper.createFile("contents\n", ".dic")
                .also { it.setReadable(false) }
            val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)
            val reference = DictionaryReference.to(dictionary)

            assertThat(reference.isValid()).isFalse()
        }

        it("no longer fails for a now-valid dictionary") {
            if (isWindows()) return@it // setReadable does not work in Windows

            val dictionaryFile = tempFileHelper.createFile("contents\n", ".dic")
                .also { it.setReadable(false) }
            val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)
            val reference = DictionaryReference.to(dictionary)

            assertThat(reference.isValid()).isFalse()

            dictionaryFile.setReadable(true)

            assertThat(reference.isValid()).isTrue()
        }
    }
})
