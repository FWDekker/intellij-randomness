package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeEditor
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnActionButton
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel
import javax.swing.ListSelectionModel


/**
 * A component that can be used to edit a scheme of type [S] which consists of a list of schemes of type [E].
 *
 * The interface consists of a list on the left, containing the instances of [E] inside [originalScheme], and displays
 * an editor on the right whenever an entry in the list is pressed.
 *
 * @param S the type of scheme that can be edited in this component
 * @param E the type of scheme that [S] consists of
 * @property stateToEntryList Converts an instance of [S] into a list of type [E].
 * @property entryListToState Converts a list of [E]s into an instance of [S].
 * @property addActionGroupGenerator Returns the actions that can be taken when the "Add" button is pressed.
 * @param originalScheme the scheme to be edited in this component
 * @param emptyText the text to display when the table is empty
 */
abstract class ListSchemeEditor<S : Scheme, E : Scheme>(
    originalScheme: S,
    private val stateToEntryList: (S) -> List<E>,
    private val entryListToState: (List<E>) -> S,
    private val addActionGroupGenerator: (JBList<E>, DefaultListModel<E>) -> ActionGroup,
    emptyText: String
) : SchemeEditor<S>(originalScheme) {
    @Suppress("UNCHECKED_CAST") // `deepCopy` always returns instance of its own class
    private val stateListModel = DefaultListModel<E>()
    private val changeListeners = mutableListOf<() -> Unit>()
    private var entryEditor: SchemeEditor<out E>? = null

    final override val rootComponent = JPanel(BorderLayout())

    /**
     * The list of entries of type [E].
     *
     * This list includes possibly unsaved entries.
     */
    val stateList = JBList(stateListModel)


    init {
        val splitter = JBSplitter(false, DEFAULT_SPLITTER_PROPORTION)
        rootComponent.add(splitter, BorderLayout.CENTER)

        stateList.setEmptyText(emptyText)
        stateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        splitter.firstComponent = JBScrollPane(decorateTemplateList(stateList))

        stateList.addListSelectionListener { event ->
            changeListeners.forEach { it() }

            if (event.valueIsAdjusting)
                return@addListSelectionListener

            val entry = stateList.selectedValue
            if (entry == null) {
                splitter.secondComponent = null
                return@addListSelectionListener
            }

            entryEditor = createEditor(entry)
                .also { editor ->
                    editor.addChangeListener {
                        editor.applyScheme()
                        changeListeners.forEach { it() }
                        stateList.repaint() // Force updates to be visible
                    }

                    splitter.secondComponent = JBScrollPane(editor.rootComponent)
                }
        }
    }

    /**
     * Decorates the given list with buttons for adding, removing, copying, etc.
     *
     * @param templateList the list to decorate
     * @return a panel containing both the decorator and the given list
     */
    private fun decorateTemplateList(templateList: JBList<E>) =
        ToolbarDecorator.createDecorator(templateList)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .disableAddAction()
            .addExtraAction(AnActionButton.GroupPopupWrapper(addActionGroupGenerator(stateList, stateListModel)))
            .addExtraAction(object : AnActionButton("Copy", PlatformIcons.COPY_ICON) {
                override fun actionPerformed(e: AnActionEvent) {
                    templateList.selectedValue?.let { entry ->
                        @Suppress("UNCHECKED_CAST") // `deepCopy` always returns instance of its own class
                        stateListModel.addElement(entry.deepCopy() as E)
                        templateList.selectedIndex = stateListModel.size - 1
                    }
                }

                override fun isEnabled() = templateList.selectedValue != null
            })
            .setButtonComparator("Add", "Remove", "Copy", "Up", "Down")
            .createPanel()


    /**
     * Creates an editor for the given entry.
     *
     * @param entry the entry to edit
     * @return an editor for the given entry
     */
    abstract fun createEditor(entry: E): SchemeEditor<out E>


    override fun loadScheme(scheme: S) {
        super.loadScheme(scheme)

        stateListModel.removeAllElements()
        @Suppress("UNCHECKED_CAST") // `deepCopy` always returns instance of its own class
        stateListModel.addAll(stateToEntryList(scheme.deepCopy() as S))
        stateList.selectedIndex = -1

        if (!stateListModel.isEmpty)
            stateList.selectedIndex = 0
        changeListeners.forEach { it() }
    }

    override fun readScheme(): S = entryListToState(stateListModel.elements().toList())


    override fun isModified(): Boolean {
        val originalEntries = stateToEntryList(originalScheme)
        val unsavedEntries = stateToEntryList(readScheme())

        return super.isModified() ||
            originalEntries.size != unsavedEntries.size ||
            originalEntries.zip(unsavedEntries).any { it.first != it.second }
    }

    override fun doValidate() = entryEditor?.doValidate() ?: readScheme().doValidate()


    override fun addChangeListener(listener: () -> Unit) {
        changeListeners += listener
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default proportion of the splitter component.
         */
        const val DEFAULT_SPLITTER_PROPORTION = .2f
    }
}
