package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [SymbolSetDialog].
 */
object SymbolSetDialogTest : Spek({
    lateinit var symbolSetDialog: SymbolSetDialog
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    fun init(reservedNames: List<String> = emptyList(), symbolSet: SymbolSet? = null) {
        symbolSetDialog =
            if (symbolSet == null)
                GuiActionRunner.execute<SymbolSetDialog> { SymbolSetDialog(reservedNames) }
            else
                GuiActionRunner.execute<SymbolSetDialog> { SymbolSetDialog(reservedNames, symbolSet) }

        frame = showInFrame(symbolSetDialog.createCenterPanel())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("text field values") {
        it("returns the value of the name field") {
            init()

            GuiActionRunner.execute { frame.textBox("name").target().text = "boast" }

            assertThat(symbolSetDialog.name).isEqualTo("boast")
        }

        it("returns the value of the symbol field") {
            init()

            GuiActionRunner.execute { frame.textBox("symbols").target().text = "just" }

            assertThat(symbolSetDialog.symbols).isEqualTo("just")
        }

        it("loads the given symbol set's values") {
            init(symbolSet = SymbolSet("liar", "preserve"))

            assertThat(symbolSetDialog.name).isEqualTo("liar")
            assertThat(symbolSetDialog.symbols).isEqualTo("preserve")
        }
    }

    describe("validation") {
        it("passes for valid input") {
            init()

            GuiActionRunner.execute {
                frame.textBox("name").target().text = "green"
                frame.textBox("symbols").target().text = "earth"
            }

            assertThat(symbolSetDialog.doValidate()).isNull()
        }

        it("passes for an unedited symbol set") {
            init(symbolSet = SymbolSet("reduce", "rice"))

            assertThat(symbolSetDialog.doValidate()).isNull()
        }

        it("fails if the name is empty") {
            init(symbolSet = SymbolSet("wash", "notice"))

            GuiActionRunner.execute { frame.textBox("name").target().text = "" }

            val validationInfo = symbolSetDialog.doValidate()
            assertThat(validationInfo).isNotNull()
            assertThat(validationInfo?.component).isEqualTo(frame.textBox("name").target())
            assertThat(validationInfo?.message).isEqualTo("Enter a name.")
        }

        it("fails if the name is reserved") {
            init(reservedNames = listOf("city", "mixture"))

            GuiActionRunner.execute { frame.textBox("name").target().text = "mixture" }

            val validationInfo = symbolSetDialog.doValidate()
            assertThat(validationInfo).isNotNull()
            assertThat(validationInfo?.component).isEqualTo(frame.textBox("name").target())
            assertThat(validationInfo?.message)
                .isEqualTo("That name is used for another symbol set. Use a different name.")
        }

        it("fails if the symbols are empty") {
            init(symbolSet = SymbolSet("content", "destroy"))

            GuiActionRunner.execute { frame.textBox("symbols").target().text = "" }

            val validationInfo = symbolSetDialog.doValidate()
            assertThat(validationInfo).isNotNull()
            assertThat(validationInfo?.component).isEqualTo(frame.textBox("symbols").target())
            assertThat(validationInfo?.message).isEqualTo("Enter at least one symbol.")
        }
    }
})
