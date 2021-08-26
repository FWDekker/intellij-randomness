package com.fwdekker.randomness.word

import com.fwdekker.randomness.TempFileHelper
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [DictionarySettings].
 */
object DictionarySettingsTest : Spek({
    describe("doValidate") {
        val tempFileHelper = TempFileHelper()


        afterGroup {
            tempFileHelper.cleanUp()
        }


        it("passes for the default settings") {
            assertThat(DictionarySettings().doValidate()).isNull()
        }

        it("fails if a dictionary of a now-deleted file is given") {
            val dictionaryFile = tempFileHelper.createFile("noon\nreason\n", ".dic").also { it.delete() }

            val settings = DictionarySettings(userDictionaryFiles = setOf(dictionaryFile.absolutePath))

            assertThat(settings.doValidate()).matches("Dictionary '.*\\.dic' is invalid: File not found\\.")
        }

        it("fails if one of the dictionaries is invalid") {
            val settings = DictionarySettings(userDictionaryFiles = setOf("does_not_exist.dic"))

            assertThat(settings.doValidate()).isEqualTo("Dictionary 'does_not_exist.dic' is invalid: File not found.")
        }

        it("fails if one the dictionaries is empty") {
            val dictionaryFile = tempFileHelper.createFile("", ".dic")

            val settings = DictionarySettings(userDictionaryFiles = setOf(dictionaryFile.absolutePath))

            assertThat(settings.doValidate()).matches("Dictionary '.*\\.dic' is empty\\.")
        }
    }
})
