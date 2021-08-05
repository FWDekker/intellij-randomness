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
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.AnActionButton
import com.intellij.ui.JBSplitter
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel


/**
 * Component for settings of random UDS-based string generation.
 *
 * @param settings the settings to edit in the component
 *
 * @see TemplateSettingsAction
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class TemplateSettingsComponent(val settings: TemplateSettings = default) :
    SettingsComponent<TemplateSettings>(settings) {
    override lateinit var rootPane: JPanel private set
    private lateinit var previewPanelHolder: PreviewPanel
    private lateinit var previewPanel: JPanel
    private lateinit var templatePanel: JBSplitter
    private lateinit var schemeList: JList<Scheme<*>>
    private lateinit var schemeListModel: DefaultListModel<Scheme<*>>
    private lateinit var schemeEditor: JPanel


    init {
        loadSettings()

        schemeList.addListSelectionListener { event ->
            if (!event.valueIsAdjusting && schemeList.selectedIndex in 0 until schemeListModel.size)
                displaySchemeEditor(schemeList.selectedIndex)
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

        templatePanel = JBSplitter(false, .2f)

        schemeListModel = DefaultListModel()
        schemeList = JBList(schemeListModel)
        schemeList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        schemeList.setCellRenderer { _, value, _, _, _ -> JBLabel(value::class.simpleName ?: "Scheme") }
        val scrollPane = JBScrollPane()
        scrollPane.border = JBUI.Borders.empty()
        scrollPane.setViewportView(schemeList)
        val toolbar = ToolbarDecorator.createDecorator(schemeList)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .disableAddAction()
            .addExtraAction(AnActionButton.GroupPopupWrapper(
                DefaultActionGroup(
                    object : DumbAwareAction("Literal") {
                        override fun actionPerformed(e: AnActionEvent) {
                            schemeListModel.addElement(LiteralScheme())
                            schemeList.selectedIndex = schemeListModel.size - 1
                        }
                    },
                    object : DumbAwareAction("Integer") {
                        override fun actionPerformed(e: AnActionEvent) {
                            schemeListModel.addElement(IntegerScheme())
                            schemeList.selectedIndex = schemeListModel.size - 1
                        }
                    },
                    object : DumbAwareAction("Decimal") {
                        override fun actionPerformed(e: AnActionEvent) {
                            schemeListModel.addElement(DecimalScheme())
                            schemeList.selectedIndex = schemeListModel.size - 1
                        }
                    },
                    object : DumbAwareAction("String") {
                        override fun actionPerformed(e: AnActionEvent) {
                            schemeListModel.addElement(StringScheme())
                            schemeList.selectedIndex = schemeListModel.size - 1
                        }
                    },
                    object : DumbAwareAction("Word") {
                        override fun actionPerformed(e: AnActionEvent) {
                            schemeListModel.addElement(WordScheme())
                            schemeList.selectedIndex = schemeListModel.size - 1
                        }
                    },
                    object : DumbAwareAction("UUID") {
                        override fun actionPerformed(e: AnActionEvent) {
                            schemeListModel.addElement(UuidScheme())
                            schemeList.selectedIndex = schemeListModel.size - 1
                        }
                    }
                )
                    .apply {
                        templatePresentation.icon = LayeredIcon.ADD_WITH_DROPDOWN
                        templatePresentation.text = "Add"
                        registerCustomShortcutSet(CommonShortcuts.getNewForDialogs(), null)
                    }
            ))
            .addExtraAction(object : AnActionButton("Copy", AllIcons.General.InlineCopy) {
                override fun actionPerformed(e: AnActionEvent) {
                    schemeList.selectedValue?.let { scheme ->
                        schemeListModel.addElement(scheme.deepCopy() as Scheme<*>)
                        schemeList.selectedIndex = schemeListModel.size - 1
                    }
                }

                override fun isEnabled() = schemeList.selectedValue != null
            })
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
        templatePanel.firstComponent = JBScrollPane(toolbar.createPanel())

        schemeEditor = JPanel(BorderLayout())
        templatePanel.secondComponent = schemeEditor
    }

    override fun loadSettings(settings: TemplateSettings) {
        schemeListModel.removeAllElements()
        schemeListModel.addAll(settings.templates.first().schemes)
        schemeList.setSelectionInterval(0, 0)
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
        editor.addChangeListener {
            schemeListModel.set(listIndex, editor.saveScheme())
            previewPanelHolder.updatePreview()
        }

        schemeEditor.removeAll()
        schemeEditor.add(editor.rootPane)
    }


    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(schemeList, listener = listener)
}
