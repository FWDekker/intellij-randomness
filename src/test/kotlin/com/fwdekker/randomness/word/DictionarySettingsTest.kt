package com.fwdekker.randomness.word

import com.fwdekker.randomness.TempFileHelper
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [DictionarySettings].
 */
object DictionarySettingsTest : Spek({
    val tempFileHelper = TempFileHelper()


    afterGroup {
        tempFileHelper.cleanUp()
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(DictionarySettings().doValidate()).isNull()
        }

        it("fails if multiple dictionaries have the same name") {
            val file = tempFileHelper.createFile("express\ntube", ".dic")

            val settings = DictionarySettings(
                listOf(UserDictionary(file.absolutePath), UserDictionary(file.absolutePath))
            )

            assertThat(settings.doValidate()).matches("Remove duplicate dictionary '.*\\.dic'\\.")
        }

        it("fails if a dictionary of a now-deleted file is given") {
            val file = tempFileHelper.createFile("noon\nreason\n", ".dic").also { it.delete() }

            val settings = DictionarySettings(listOf(UserDictionary(file.absolutePath)))

            assertThat(settings.doValidate()).matches("Dictionary '.*\\.dic' is invalid: File not found\\.")
        }

        it("fails if one of the dictionaries is invalid") {
            val settings = DictionarySettings(listOf(UserDictionary("does_not_exist.dic")))

            assertThat(settings.doValidate()).isEqualTo("Dictionary 'does_not_exist.dic' is invalid: File not found.")
        }

        it("fails if one the dictionaries is empty") {
            val file = tempFileHelper.createFile("", ".dic")

            val settings = DictionarySettings(listOf(UserDictionary(file.absolutePath)))

            assertThat(settings.doValidate()).matches("Dictionary '.*\\.dic' is empty\\.")
        }

        it("fails if one of the dictionaries becomes invalid") {
            val file = tempFileHelper.createFile("rapid\ncloth", ".dic")
            val dictionary = UserDictionary(file.absolutePath)

            val settings = DictionarySettings(listOf(dictionary))
            dictionary.words // Force cache load
            file.writeText("")

            assertThat(settings.doValidate()).matches("Dictionary '.*\\.dic' is empty\\.")
        }
    }

    describe("deepCopy") {
        it("retains the UUID if desired") {
            val settings = DictionarySettings()

            assertThat(settings.deepCopy(retainUuid = true).uuid).isEqualTo(settings.uuid)
        }

        it("generates a new UUID if desired") {
            val settings = DictionarySettings()

            assertThat(settings.deepCopy(retainUuid = false).uuid).isNotEqualTo(settings.uuid)
        }

        it("contains an independent list of dictionaries") {
            val settings = DictionarySettings(listOf(BundledDictionary("wait.dic")))

            val copy = settings.deepCopy()
            copy.dictionaries = listOf(BundledDictionary("upright.dic"))

            assertThat(settings.dictionaries).containsExactly(BundledDictionary("wait.dic"))
        }

        it("contains a list of independent dictionaries") {
            val settings = DictionarySettings(listOf(BundledDictionary("wait.dic")))

            val copy = settings.deepCopy()
            (copy.dictionaries.first() as BundledDictionary).filename = "defense.dic"

            assertThat((settings.dictionaries.first() as BundledDictionary).filename).isEqualTo("wait.dic")
        }
    }
})
