package com.fwdekker.randomness.word

import com.fwdekker.randomness.TempFileHelper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.fail
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.Locale


/**
 * Returns `true` if the current operating system is Windows.
 *
 * @return `true` if the current operating system is Windows
 */
private fun isWindows() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")


/**
 * Unit tests for [Dictionaries][Dictionary].
 */
object DictionaryTest : Spek({
    beforeEachTest {
        BundledDictionary.clearCache()
        UserDictionary.clearCache()
    }


    describe("BundledDictionary") {
        describe("words") {
            it("fails if the file does not exist") {
                val dictionary = BundledDictionary("does_not_exist.dic")

                assertThatThrownBy { dictionary.words }
                    .isInstanceOf(InvalidDictionaryException::class.java)
                    .hasMessage("File not found.")
            }

            it("is an empty list if the dictionary is empty") {
                val dictionary = BundledDictionary("dictionaries/empty.dic")

                assertThat(dictionary.words).isEmpty()
            }

            it("contains the words of a non-empty dictionary file") {
                val dictionary = BundledDictionary("dictionaries/simple.dic")

                assertThat(dictionary.words).containsExactlyInAnyOrder("a", "the", "dog", "woof", "cat", "meow")
            }

            it("does not contain duplicate words") {
                val dictionary = BundledDictionary("dictionaries/duplicates.dic")

                assertThat(dictionary.words).containsExactlyInAnyOrder("dog", "woof", "cat", "meow")
            }

            it("ignores empty lines") {
                val dictionary = BundledDictionary("dictionaries/empty-lines.dic")

                assertThat(dictionary.words).containsExactlyInAnyOrder("woof", "meow")
            }

            it("ignores commented lines") {
                val dictionary = BundledDictionary("dictionaries/comments.dic")

                assertThat(dictionary.words).containsExactlyInAnyOrder("cat", "mouse", "tree")
            }
        }

        describe("toString") {
            it("returns a human-readable string of the dictionary's filename") {
                val dictionary = BundledDictionary("dictionaries/simple.dic")

                assertThat(dictionary.toString()).isEqualTo("dictionaries/simple.dic")
            }

            it("works even if the file does not exist") {
                val dictionary = BundledDictionary("does_not_exist.dic")

                assertThat(dictionary.toString()).isEqualTo("does_not_exist.dic")
            }
        }

        describe("equals + hash code") {
            it("equals itself") {
                val dictionary = BundledDictionary("dictionary.dic")

                assertThat(dictionary).isEqualTo(dictionary)
                assertThat(dictionary.hashCode()).isEqualTo(dictionary.hashCode())
            }

            it("equals a dictionary with the same filename") {
                val dictionary1 = BundledDictionary("dictionary.dic")
                val dictionary2 = BundledDictionary("dictionary.dic")

                assertThat(dictionary1).isEqualTo(dictionary2)
                assertThat(dictionary1.hashCode()).isEqualTo(dictionary2.hashCode())
            }

            it("does not equal a bundled dictionary with a different filename") {
                val dictionary1 = BundledDictionary("dictionary1.dic")
                val dictionary2 = BundledDictionary("dictionary2.dic")

                assertThat(dictionary1).isNotEqualTo(dictionary2)
            }

            it("does not equal a user dictionary with the same filename") {
                val dictionary1 = BundledDictionary("dictionary.dic")
                val dictionary2 = UserDictionary("dictionary.dic")

                assertThat(dictionary1).isNotEqualTo(dictionary2)
            }

            it("does not equal a different object") {
                val dictionary = BundledDictionary("dictionary.dic")
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


        describe("words") {
            describe("validation") {
                it("fails if the file does not exist") {
                    val dictionary = UserDictionary("does_not_exist.dic")

                    assertThatThrownBy { dictionary.words }
                        .isInstanceOf(InvalidDictionaryException::class.java)
                        .hasMessage("File not found.")
                }

                it("fails if the file is deleted after construction of the dictionary") {
                    val file = tempFileHelper.createFile("blizzard\nflames\ninvest", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    if (!file.delete())
                        fail("Failed to delete file as part of test.")

                    assertThatThrownBy { dictionary.words }
                        .isInstanceOf(InvalidDictionaryException::class.java)
                        .hasMessage("File not found.")
                }

                it("fails if the file exists but cannot be accessed") {
                    if (isWindows()) return@it // setReadable does not work in Windows

                    val file = tempFileHelper.createFile("ladder\nkempt\npork", ".dic")
                        .also { it.setReadable(false) }
                    val dictionary = UserDictionary(file.absolutePath)

                    assertThatThrownBy { dictionary.words }
                        .isInstanceOf(InvalidDictionaryException::class.java)
                        .hasMessage("File unreadable.")
                }
            }

            describe("parsing") {
                it("is an empty list if the dictionary is empty") {
                    val file = tempFileHelper.createFile("", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    assertThat(dictionary.words).isEmpty()
                }

                it("contains the words of a non-empty dictionary file") {
                    val file = tempFileHelper.createFile("batmen\njollity\nbolts", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    assertThat(dictionary.words).containsExactlyInAnyOrder("batmen", "jollity", "bolts")
                }

                it("does not contain duplicate words") {
                    val file = tempFileHelper.createFile("dolphins\nmappings\ndolphins\nflat", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    assertThat(dictionary.words).containsExactlyInAnyOrder("dolphins", "mappings", "flat")
                }

                it("ignores empty lines") {
                    val file = tempFileHelper.createFile("\n\nwoof\nmeow\n\n\n", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    assertThat(dictionary.words).containsExactlyInAnyOrder("woof", "meow")
                }

                it("ignores commented lines") {
                    val file = tempFileHelper.createFile("# Comment\nflaming\nlove\n# Destiny\n", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    assertThat(dictionary.words).containsExactlyInAnyOrder("flaming", "love")
                }
            }

            describe("caching") {
                it("returns different words after the filename has been changed") {
                    val file1 = tempFileHelper.createFile("grass\nthank", ".dic")
                    val file2 = tempFileHelper.createFile("serve\nleaf", ".dic")
                    val dictionary = UserDictionary(file1.absolutePath)

                    dictionary.words
                    dictionary.filename = file2.absolutePath

                    assertThat(dictionary.words).containsExactly("serve", "leaf")
                }

                it("returns the same words even if the file is changed") {
                    val file = tempFileHelper.createFile("nest\nthose", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    dictionary.words
                    file.writeText("worth\nearn")

                    assertThat(dictionary.words).containsExactly("nest", "those")
                }

                it("returns the same words as another instance even if the file is changed") {
                    val file = tempFileHelper.createFile("annoy\nregular", ".dic")
                    val dictionary1 = UserDictionary(file.absolutePath)
                    val dictionary2 = UserDictionary(file.absolutePath)

                    dictionary1.words
                    file.writeText("furnish\nthan")

                    assertThat(dictionary2.words).containsExactly("annoy", "regular")
                }

                it("returns the updated words from the file after the cache is cleared") {
                    val file = tempFileHelper.createFile("seem\ngod", ".dic")
                    val dictionary = UserDictionary(file.absolutePath)

                    dictionary.words
                    file.writeText("safe\nonly")
                    UserDictionary.clearCache()

                    assertThat(dictionary.words).containsExactly("safe", "only")
                }
            }
        }

        describe("toString") {
            it("returns a human-readable string of the dictionary's filename") {
                val file = tempFileHelper.createFile("mode\ndepeche", ".dic")
                val dictionary = UserDictionary(file.absolutePath)

                assertThat(dictionary.toString()).endsWith(".dic")
            }

            it("works even if the file does not exist") {
                val dictionary = UserDictionary("does_not_exist.dic")

                assertThat(dictionary.toString()).isEqualTo("does_not_exist.dic")
            }
        }

        describe("equals + hash code") {
            it("equals itself") {
                val dictionary = UserDictionary("dictionary.dic")

                assertThat(dictionary).isEqualTo(dictionary)
                assertThat(dictionary.hashCode()).isEqualTo(dictionary.hashCode())
            }

            it("equals a dictionary with the same filename") {
                val dictionary1 = UserDictionary("dictionary.dic")
                val dictionary2 = UserDictionary("dictionary.dic")

                assertThat(dictionary1).isEqualTo(dictionary2)
                assertThat(dictionary1.hashCode()).isEqualTo(dictionary2.hashCode())
            }

            it("does not equal a user dictionary with a different filename") {
                val dictionary1 = UserDictionary("dictionary1.dic")
                val dictionary2 = UserDictionary("dictionary2.dic")

                assertThat(dictionary1).isNotEqualTo(dictionary2)
            }

            it("does not equal a bundled dictionary with the same filename") {
                val dictionary1 = UserDictionary("dictionary.dic")
                val dictionary2 = BundledDictionary("dictionary.dic")

                assertThat(dictionary1).isNotEqualTo(dictionary2)
            }

            it("does not equal a different object") {
                val dictionary = UserDictionary("dictionary.dic")
                val other = Any()

                assertThat(dictionary).isNotEqualTo(other)
            }
        }
    }
})
