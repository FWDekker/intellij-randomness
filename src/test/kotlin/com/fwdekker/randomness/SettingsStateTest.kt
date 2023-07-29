package com.fwdekker.randomness

import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.template.TemplateList
import com.fwdekker.randomness.template.TemplateReference
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy


/**
 * Unit tests for [Settings].
 */
object SettingsStateTest : DescribeSpec({
    lateinit var state: Settings


    beforeEach {
        state = Settings()
    }


    describe("doValidate") {
        it("passes for the default values") {
            assertThat(state.doValidate()).isNull()
        }

        it("fails if the template list is invalid") {
            state.templateList.templates = listOf(Template(name = "Among"), Template(name = "Among"))

            assertThat(state.doValidate()).isNotNull()
        }
    }

    describe("copyFrom") {
        it("cannot copy from another type") {
            assertThatThrownBy { state.copyFrom(DummyScheme()) }.isNotNull()
        }

        it("copies the other's UUIDs") {
            val other = Settings()

            state.copyFrom(other)

            assertThat(state.uuid).isEqualTo(other.uuid)
            assertThat(state.templateList.uuid).isEqualTo(other.templateList.uuid)
        }

        it("writes deep copies into the target's settings fields") {
            val otherState = Settings()

            state.copyFrom(otherState)

            assertThat(state.templateList)
                .isEqualTo(otherState.templateList)
                .isNotSameAs(otherState.templateList)
        }

        it("writes itself into the template list's templates") {
            val otherState = Settings(TemplateList.from(TemplateReference()))

            state.copyFrom(otherState)

            assertThat(+(state.templateList.templates[0].schemes[0] as TemplateReference).templateList)
                .isSameAs(state.templateList)
        }
    }

    describe("deepCopy") {
        it("retains its own UUID if retainUuid is true") {
            val oldUuid = state.uuid

            assertThat(state.deepCopy(retainUuid = true).uuid).isEqualTo(oldUuid)
        }

        it("does not retain its own UUID if retainUuid is false") {
            val oldUuid = state.uuid

            assertThat(state.deepCopy(retainUuid = false).uuid).isNotEqualTo(oldUuid)
        }

        it("writes itself into the template list's templates") {
            val reference = TemplateReference()
            state.templateList = TemplateList.from(reference)

            val copy = state.deepCopy()

            assertThat(+(copy.templateList.templates[0].schemes[0] as TemplateReference).templateList)
                .isSameAs(copy.templateList)
        }
    }
})
