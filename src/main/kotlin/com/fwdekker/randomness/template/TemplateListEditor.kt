package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.decimal.DecimalSchemeEditor
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.integer.IntegerSchemeEditor
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.literal.LiteralSchemeEditor
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.StringSchemeEditor
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.SimpleTreeModelListener
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.SwingUtilities


/**
 * Component for editing [TemplateList]s while keeping in mind the overall [SettingsState].
 *
 * The editor consists of a left side and a right side. The left side contains a tree with the templates as roots and
 * their schemes as the leaves. When the user selects a scheme, the appropriate [StateEditor] for that scheme is loaded
 * on the right side.
 *
 * The tree itself contains copies of the entries in the given [TemplateList]. Changes in the editor are immediately
 * written to these copies so that when another scheme editor is displayed the changes are not lost. The changes in the
 * copies are written into the given template list only when [applyState] is invoked.
 *
 * @param settings the settings containing the templates to edit
 */
class TemplateListEditor(settings: SettingsState = SettingsState.default) : StateEditor<SettingsState>(settings) {
    override val rootComponent = JPanel(BorderLayout())

    private val currentState: SettingsState = SettingsState()
    private val templateTree = TemplateJTree(originalState, currentState)
    private val schemeEditorPanel = JPanel(BorderLayout())
    private var schemeEditor: StateEditor<*>? = null
    private val previewPanel: PreviewPanel

    /**
     * The UUID of the scheme to select after the next invocation of [reset].
     *
     * @see TemplateSettingsAction
     */
    var queueSelection: String? = null


    init {
        val splitter = CREATE_SPLITTER(false, SPLITTER_PROPORTION_KEY, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        // Left half
        templateTree.addTreeSelectionListener { onTreeSelection() }
        splitter.firstComponent = templateTree.asDecoratedPanel()

        // Right half
        previewPanel = PreviewPanel {
            val selectedNode = templateTree.selectedNodeNotRoot ?: return@PreviewPanel LiteralScheme("")
            val parentNode = templateTree.myModel.getParentOf(selectedNode)!!

            if (selectedNode.state is Template) selectedNode.state
            else parentNode.state as Template
        }
        Disposer.register(this, previewPanel)
        addChangeListener { previewPanel.updatePreview() }
        schemeEditorPanel.border = JBEmptyBorder(EDITOR_PANEL_MARGIN)
        schemeEditorPanel.add(previewPanel.rootComponent, BorderLayout.SOUTH)

        splitter.secondComponent = schemeEditorPanel

        loadState()
    }

    /**
     * Invoked when an entry is (de)selected in the tree.
     */
    private fun onTreeSelection() {
        schemeEditor?.also {
            schemeEditorPanel.remove((schemeEditorPanel.layout as BorderLayout).getLayoutComponent(BorderLayout.CENTER))
            schemeEditor = null
        }

        val selectedNode = templateTree.selectedNodeNotRoot
        val selectedState = selectedNode?.state
        if (selectedState !is Scheme) {
            templateTree.myModel.fireNodeChanged(selectedNode)
            return
        }

        schemeEditor = createEditor(selectedState)
            .also { editor ->
                editor.addChangeListener {
                    editor.applyState()
                    templateTree.myModel.fireNodeChanged(selectedNode)
                    templateTree.myModel.fireNodeStructureChanged(selectedNode)
                }

                schemeEditorPanel.add(
                    JBScrollPane(editor.rootComponent).also {
                        it.border = null
                        it.preferredSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
                    },
                    BorderLayout.CENTER
                )
                editor.applyState() // Apply validation fixes from UI
                rootComponent.revalidate() // Show editor immediately
            }
    }

    /**
     * Creates an editor to edit the given scheme.
     *
     * @param scheme the scheme to create an editor for
     */
    private fun createEditor(scheme: Scheme) =
        when (scheme) {
            is IntegerScheme -> IntegerSchemeEditor(scheme)
            is DecimalScheme -> DecimalSchemeEditor(scheme)
            is StringScheme -> StringSchemeEditor(scheme)
            is UuidScheme -> UuidSchemeEditor(scheme)
            is WordScheme -> WordSchemeEditor(scheme)
            is LiteralScheme -> LiteralSchemeEditor(scheme)
            is TemplateReference -> TemplateReferenceEditor(scheme)
            is Template -> TemplateEditor(scheme)
            else -> error(Bundle("template_list.error.unknown_scheme_type", scheme.javaClass.canonicalName))
        }


    override fun loadState(state: SettingsState) {
        super.loadState(state)

        currentState.copyFrom(state)
        templateTree.reload()
    }

    override fun readState() = currentState.deepCopy(retainUuid = true)


    override fun reset() {
        super.reset()

        queueSelection?.also {
            templateTree.selectedScheme = currentState.templateList.getSchemeByUuid(it)
            SwingUtilities.invokeLater { schemeEditor?.preferredFocusedComponent?.requestFocus() }

            queueSelection = null
        }
    }

    override fun addChangeListener(listener: () -> Unit) {
        templateTree.model.addTreeModelListener(SimpleTreeModelListener { listener() })
        templateTree.addTreeSelectionListener { listener() }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The key to store the user's last-used splitter proportion under.
         */
        const val SPLITTER_PROPORTION_KEY = "com.fwdekker.randomness.template.TemplateListEditor"

        /**
         * The default proportion of the splitter component.
         */
        const val DEFAULT_SPLITTER_PROPORTION = .25f

        /**
         * Pixels of margin outside the editor panel.
         */
        const val EDITOR_PANEL_MARGIN = 10

        /**
         * Creates a new splitter.
         *
         * For some reason, `OnePixelSplitter` does not work in tests. Therefore, tests overwrite this field to inject a
         * different constructor.
         */
        var CREATE_SPLITTER: (Boolean, String, Float) -> Splitter =
            { vertical, proportionKey, defaultProportion ->
                OnePixelSplitter(vertical, proportionKey, defaultProportion)
            }
    }
}
