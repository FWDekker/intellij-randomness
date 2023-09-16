package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.datetime.DateTimeSchemeEditor
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.decimal.DecimalSchemeEditor
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.integer.IntegerSchemeEditor
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.StringSchemeEditor
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities


/**
 * Component for editing a [TemplateList]s.
 *
 * The editor essentially has a master-detail layout. On the left, a [TemplateJTree] shows all templates and the schemes
 * contained within them, and on the right, a [SchemeEditor] for the currently-selected template or scheme is shown.
 *
 * Though this editor only changes [TemplateList]s, it still maintains a reference to
 *
 * @property originalSettings The settings containing the templates to edit.
 * @see TemplateListConfigurable
 */
class TemplateListEditor(private val originalSettings: Settings = Settings.DEFAULT) : Disposable {
    /**
     * The root component of the editor.
     */
    val rootComponent = JPanel(BorderLayout())

    private val currentSettings = Settings()
    private val templateTree = TemplateJTree(originalSettings, currentSettings)
    private val schemeEditorPanel = JPanel(BorderLayout())
    private var schemeEditor: SchemeEditor<*>? = null
    private val previewPanel: PreviewPanel

    /**
     * The UUID of the scheme to select after the next invocation of [reset].
     *
     * @see TemplateListConfigurable
     */
    var queueSelection: String? = null


    init {
        val splitter = createSplitter(false, SPLITTER_PROPORTION_KEY, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        // Left half
        templateTree.addTreeSelectionListener { onTreeSelection() }
        splitter.firstComponent = templateTree.asDecoratedPanel()

        // Right half
        previewPanel = PreviewPanel {
            val selectedNode = templateTree.selectedNodeNotRoot ?: return@PreviewPanel StringScheme("")
            val parentNode = templateTree.myModel.getParentOf(selectedNode)!!

            if (selectedNode.state is Template) selectedNode.state
            else parentNode.state as Template
        }
        addChangeListenerTo(templateTree) { previewPanel.updatePreview() }
        schemeEditorPanel.border = JBEmptyBorder(EDITOR_PANEL_MARGIN)
        schemeEditorPanel.add(previewPanel.rootComponent, BorderLayout.SOUTH)

        splitter.secondComponent = schemeEditorPanel

        // Load current state
        reset()
    }

    /**
     * Invoked when an entry is (de)selected in the tree.
     */
    private fun onTreeSelection() {
        schemeEditor?.also { editor ->
            schemeEditorPanel.remove((schemeEditorPanel.layout as BorderLayout).getLayoutComponent(BorderLayout.CENTER))
            editor.dispose()
            schemeEditor = null
        }

        val selectedNode = templateTree.selectedNodeNotRoot
        val selectedState = selectedNode?.state
        if (selectedState !is Scheme) {
            schemeEditorPanel.revalidate() // Hide editor immediately
            return
        }

        schemeEditor = createEditor(selectedState)
            .also { editor ->
                editor.addChangeListener {
                    editor.apply()
                    templateTree.myModel.fireNodeStructureChanged(selectedNode)
                }

                schemeEditorPanel.add(
                    JBScrollPane(editor.rootComponent).also {
                        it.border = null
                        it.preferredSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
                    },
                    BorderLayout.CENTER
                )
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner") {
                    val focused = it.newValue as? JComponent
                    if (focused == null || !editor.rootComponent.isAncestorOf(focused))
                        return@addPropertyChangeListener

                    editor.rootComponent.scrollRectToVisible(
                        SwingUtilities.convertRectangle(
                            focused,
                            focused.bounds,
                            editor.rootComponent
                        )
                    )
                }

                editor.apply() // Apply validation fixes from UI
                templateTree.myModel.fireNodeStructureChanged(selectedNode)
                schemeEditorPanel.revalidate() // Show editor immediately
            }
    }

    /**
     * Creates an editor to edit [scheme].
     */
    private fun createEditor(scheme: Scheme) =
        when (scheme) {
            is IntegerScheme -> IntegerSchemeEditor(scheme)
            is DecimalScheme -> DecimalSchemeEditor(scheme)
            is StringScheme -> StringSchemeEditor(scheme)
            is UuidScheme -> UuidSchemeEditor(scheme)
            is WordScheme -> WordSchemeEditor(scheme)
            is DateTimeScheme -> DateTimeSchemeEditor(scheme)
            is TemplateReference -> TemplateReferenceEditor(scheme)
            is Template -> TemplateEditor(scheme)
            else -> error(Bundle("template_list.error.unknown_state_type", "scheme", scheme.javaClass.canonicalName))
        }


    /**
     * Validates the state of the editor and indicates whether and why it is invalid.
     *
     * @return `null` if the state is valid, or a string explaining why the state is invalid
     */
    fun doValidate(): String? = currentSettings.doValidate()

    /**
     * Returns `true` if and only if the editor contains modifications relative to the last saved state.
     */
    fun isModified() = originalSettings != currentSettings

    /**
     * Saves the editor's state into [originalSettings].
     *
     * Does nothing if and only if [isModified] returns `false`.
     */
    fun apply() {
        val oldList = originalSettings.templates
        originalSettings.copyFrom(currentSettings)
        val newList = originalSettings.templates

        TemplateActionLoader().updateActions(oldList, newList)
    }

    /**
     * Resets the editor's state to the last saved state.
     *
     * Does nothing if and only if [isModified] return `false`.
     */
    fun reset() {
        currentSettings.copyFrom(originalSettings)
        templateTree.reload()

        queueSelection?.also {
            templateTree.selectedScheme = currentSettings.templateList.getSchemeByUuid(it)
            SwingUtilities.invokeLater { schemeEditor?.preferredFocusedComponent?.requestFocus() }

            queueSelection = null
        }
    }


    override fun dispose() {
        schemeEditor?.dispose()
        previewPanel.dispose()
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
        var createSplitter: (Boolean, String, Float) -> Splitter =
            { vertical, proportionKey, defaultProportion ->
                OnePixelSplitter(vertical, proportionKey, defaultProportion)
            }
    }
}
