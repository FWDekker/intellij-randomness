package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsComponent
import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.decimal.DecimalSchemeEditor
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.integer.IntegerSchemeEditor
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.literal.LiteralSchemeEditor
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.StringSchemeEditor
import com.fwdekker.randomness.template.TemplateSettings.Companion.default
import com.fwdekker.randomness.ui.PreviewPanel
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.uuid.UuidSchemeEditor
import com.fwdekker.randomness.word.WordScheme
import com.fwdekker.randomness.word.WordSchemeEditor
import com.intellij.ui.components.JBList
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel


/**
 * Component for settings of random UDS-based string generation.
 *
 * @param settings the settings to edit in the component
 *
 * @see TemplateSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class TemplateSettingsComponent(settings: TemplateSettings = default) : SettingsComponent<TemplateSettings>(settings) {
    override lateinit var rootPane: JPanel private set
    private lateinit var previewPanelHolder: PreviewPanel
    private lateinit var previewPanel: JPanel
    private lateinit var schemeListModel: DefaultListModel<Scheme<*>>
    private lateinit var schemeList: JList<Scheme<*>>
    private lateinit var schemeEditor: JPanel


    init {
        loadSettings()

        schemeList.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) displaySchemeEditor(schemeList.selectedIndex)
        }

        addChangeListener { previewPanelHolder.updatePreview() }
        previewPanelHolder.updatePreview()
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        previewPanelHolder = PreviewPanel { TemplateInsertAction(TemplateSettings().also { saveSettings(it) }) }
        previewPanel = previewPanelHolder.rootPane

        schemeListModel = DefaultListModel()
        schemeList = JBList(schemeListModel)
        schemeEditor = JPanel()
    }

    override fun loadSettings(settings: TemplateSettings) {
        schemeListModel.removeAllElements()
        schemeListModel.addAll(settings.templates.first().schemes)
    }

    override fun saveSettings(settings: TemplateSettings) {
        settings.templates.first().schemes = schemeListModel.elements().toList()
    }

    override fun doValidate(): ValidationInfo? = null


    private fun displaySchemeEditor(listIndex: Int) {
        val scheme = schemeListModel.get(listIndex)
        val editor = when (scheme) {
            is IntegerScheme -> IntegerSchemeEditor(scheme)
            is DecimalScheme -> DecimalSchemeEditor(scheme)
            is StringScheme -> StringSchemeEditor(scheme)
            is UuidScheme -> UuidSchemeEditor(scheme)
            is WordScheme -> WordSchemeEditor(scheme)
            is LiteralScheme -> LiteralSchemeEditor(scheme)
            else -> error("Unknown scheme type '${scheme.javaClass.canonicalName}'.")
        }
        editor.addChangeListener { schemeListModel.set(listIndex, editor.saveScheme()) }

        schemeEditor.removeAll()
        schemeEditor.add(editor.rootPane)
    }


    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(schemeList, listener = listener)
}
