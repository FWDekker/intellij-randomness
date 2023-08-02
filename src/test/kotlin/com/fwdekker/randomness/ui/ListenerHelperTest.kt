package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DummySchemeEditor
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.ui.dsl.builder.panel
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.text.JTextComponent
import javax.swing.text.PlainDocument
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import com.intellij.openapi.editor.Document as JBDocument


/**
 * Unit tests for extension functions in `ListenerHelperKt`.
 */
object ListenerHelperTest : FunSpec({
    tags(NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture

    var listenerInvoked = false
    val listener = { listenerInvoked = true }


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        listenerInvoked = false
    }

    afterEach {
        ideaFixture.tearDown()
    }


    test("addChangeListenerTo") {
        forAll(
            @Suppress("UNCHECKED_CAST") // Unavoidable
            table(
                headers("description", "createComponent", "changeComponent"),
                row(
                    "AbstractButton: JCheckBox: Change selection",
                    { JCheckBox() },
                    { (it as JCheckBox).isSelected = true },
                ),
                row(
                    "AbstractButton: JRadioButton: Change selection",
                    { JRadioButton() },
                    { (it as JCheckBox).isSelected = true },
                ),
                row(
                    "Document: Insert text",
                    { PlainDocument() },
                    { (it as PlainDocument).insertString(0, "text", null) },
                ),
                row(
                    "Document: Remove text",
                    { PlainDocument().also { it.insertString(0, "text", null) } },
                    { (it as PlainDocument).remove(1, 2) },
                ),
                row(
                    "Document: Replace text",
                    { PlainDocument().also { it.insertString(0, "text", null) } },
                    { (it as PlainDocument).replace(2, 1, "y", null) },
                ),
                row(
                    "JBDocument: Set text",
                    { EditorFactory.getInstance().createDocument("") },
                    { (it as JBDocument).setText("text") },
                ),
                row(
                    "JBDocument: Insert text",
                    { EditorFactory.getInstance().createDocument("text") },
                    { (it as JBDocument).insertString(1, "text") },
                ),
                row(
                    "JBDocument: Remove text",
                    { EditorFactory.getInstance().createDocument("text") },
                    { (it as JBDocument).deleteString(2, 3) },
                ),
                row(
                    "JBDocument: Replace text",
                    { EditorFactory.getInstance().createDocument("text") },
                    { (it as JBDocument).replaceString(0, 2, "text") },
                ),
                row(
                    "JComboBox: Select different item",
                    { ComboBox(arrayOf("item1", "item2")) },
                    { (it as ComboBox<String>).item = "item1" },
                ),
                row(
                    "JComboBox: While typing",
                    { ComboBox(arrayOf("item1", "item2")).also { it.isEditable = true } },
                    { ((it as ComboBox<String>).editor.editorComponent as JTextComponent).text = "ite" },
                ),
                row(
                    "JSpinner: Change value",
                    { JSpinner() },
                    { (it as JSpinner).value = 5 },
                ),
                row(
                    "JTextComponent: JTextArea",
                    { JTextArea() },
                    { (it as JTextArea).text = "text" },
                ),
                row(
                    "JTextComponent: JDateTimeField",
                    { JDateTimeField() },
                    { (it as JDateTimeField).longValue = 22_424_977L },
                ),
                row(
                    "JTextComponent: JTextField",
                    { JTextField() },
                    { (it as JTextField).text = "text" },
                ),
                row(
                    "JTextComponent: Insert text",
                    { JTextField() },
                    { (it as JTextField).document.insertString(1, "text", null) },
                ),
                row(
                    "JTextComponent: Remove text",
                    { JTextField() },
                    { (it as JTextField).document.remove(2, 2) },
                ),
                row(
                    "JTree: Change selection",
                    { JTree() },
                    { (it as JTree).setSelectionRow(2) },
                ),
                row(
                    "JTree: Add node",
                    { JTree() },
                    { (it as JTree).model().insertNodeInto(DefaultMutableTreeNode(), it.model().root(), 0) },
                ),
                row(
                    "JTree: Remove node",
                    { JTree() },
                    { (it as JTree).model().removeNodeFromParent(it.model().root().firstChild as MutableTreeNode) },
                ),
                row(
                    "SchemeEditor: Recursion",
                    { DummySchemeEditor { panel { row { textField() } } } },
                    { ((it as DummySchemeEditor).components.first() as JTextField).text = "text" },
                ),
            )
        ) { _, createComponent: () -> Any, changeComponent: (Any) -> Unit ->
            val component = guiGet { createComponent() }
            addChangeListenerTo(component, listener = listener)

            listenerInvoked shouldBe false
            guiRun { changeComponent(component) }

            listenerInvoked shouldBe true
        }
    }
})


/**
 * Returns the [model] of this [JTree] cast to a [DefaultTreeModel].
 */
private fun JTree.model() = this.model as DefaultTreeModel

/**
 * Returns the [root] of this [DefaultTreeModel] cast to a [DefaultMutableTreeNode].
 */
private fun DefaultTreeModel.root() = this.root as DefaultMutableTreeNode
