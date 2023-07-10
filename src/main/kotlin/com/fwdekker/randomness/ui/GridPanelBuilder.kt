package com.fwdekker.randomness.ui

import com.intellij.ui.SeparatorFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import com.intellij.util.ui.DialogUtil
import java.awt.Dimension
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent


class BuilderState {
    private val myComponents = mutableMapOf<String, JComponent>()

    private val myGroups = mutableMapOf<String, ButtonGroup>()


    fun addComponent(component: JComponent) {
        if (component.name != null)
            myComponents[component.name] = component
    }

    fun getLabelForOrNull(name: String) =
        myComponents.filterKeys { it == "${name}Label" }.values.singleOrNull() as? JLabel

    fun getLabelForOrNull(group: ButtonGroup) =
        getLabelForOrNull(myGroups.filterValues { it == group }.keys.single())


    fun addButtonGroup(name: String, group: ButtonGroup) {
        myGroups[name] = group
    }

    fun getButtonGroupForOrNull(button: JComponent) =
        myGroups.filterKeys { button.name.startsWith(it) }.values.singleOrNull()
}


/**
 * Designates a class as belonging to the grid panel builder DSL.
 *
 * This marker prevents scope leakage. For example, consider a DSL such as `html { head { meta(...) } }`. Without DSL
 * markers, the code `html { head { head { meta(...) } } }` is valid, even if `head` is not defined in the scope of
 * `head`, because functions from the scope of `html` leak into the scope of `head`. Adding the same DSL marker to both
 * `html` and `head` tells Kotlin that these should not leak into each other.
 */
@DslMarker
annotation class GridPanelBuilderMarker

/**
 * The grid panel DSL uses JetBrains' [GridLayoutManager] to lay out elements in a panel.
 *
 * The entry point of the DSL is [GridPanelBuilder.Companion.panel].
 *
 * The main commands within the DSL are [panel], [row], and [cell]. A panel contains rows, a row contains cells, and a
 * cell contains any [JComponent], which may include a panel created with `panel`. For example, a single panel with a
 * label in it is written as `panel { row { cell { JLabel("horse") } } }` (though a simpler syntax is described later).
 *
 * The order is always panel, then row, then cell. However, to reduce verbosity, it is possible to invoke the function
 * `cell` while inside a panel, though this implicitly adds a row in between. Therefore, a simple panel with a label
 * can also be created using `panel { cell { JPanel("horse") } }`. The function `row` is implicitly invoked in between
 * `panel` and `cell`. Similarly, `panel { panel { ...` actually expands to `panel { row { cell { panel { ...`.
 *
 * The DSL uses a grid layout in the background. The number of rows for the layout of any panel is calculated by
 * counting the number of invocations of `row` inside the panel. The number of columns is calculated by finding the row
 * with the largest number of columns; some cells may span multiple columns. Note that cells cannot span multiple rows.
 *
 * An obvious alternative to this DSL is to use JetBrains' own Kotlin UI DSL (cf. [com.intellij.ui.dsl.builder]).
 * However, this DSL quickly becomes overly complex, does not properly align elements, has inconvenient defaults, does
 * not work well with button groups, is non-agnostic to the way the underlying data is stored, and has several other
 * issues. Therefore, this grid panel DSL is preferred.
 *
 * @param T the type of output returned to the user
 */
@GridPanelBuilderMarker
sealed class GridPanelBuilder<T : Any?>(protected val state: BuilderState) {
    /**
     * Adds a panel to the current context.
     *
     * @param spec a grid builder DSL description of a panel
     * @return depends on implementation
     */
    abstract fun panel(spec: PanelBuilder.() -> Unit): T

    /**
     * Adds a row to the current context.
     *
     * @param spec a grid builder DSL description of a row
     * @return depends on implementation
     */
    abstract fun row(spec: RowBuilder.() -> Unit): T

    /**
     * Adds a cell to the current context.
     *
     * @param constraints the constraints applied to the added cell
     * @param spec a grid builder DSL description of a cell, returning the component that the cell should contain
     * @return depends on implementation
     */
    abstract fun cell(constraints: GridConstraints = constraints(), spec: CellBuilder.() -> JComponent): T


    /**
     * Creates a [cell] containing a visible separator with the given [text].
     *
     * @param text the text to display in the separator
     * @return see [cell]
     */
    fun textSeparatorCell(text: String) =
        cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) { SeparatorFactory.createSeparator(text, null) }

    /**
     * Adds a [cell] containing a small vertical spacer to be placed to separate distinct panels.
     *
     * @return see [cell]
     */
    fun vSeparatorCell() = vSpacerCell(height = UIConstants.SIZE_TINY)

    /**
     * Adds a [cell] containing a vertical spacer with the given [height].
     *
     * @param height the height of the vertical spacer, or `-1` if the spacer should use all remaining vertical space
     * @return see [cell]
     */
    fun vSpacerCell(height: Int = -1) =
        if (height == -1)
            cell(
                constraints(
                    fill = GridConstraints.FILL_VERTICAL,
                    vSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW,
                )
            ) { Spacer() }
        else
            cell(constraints(fixedHeight = height)) { Spacer() }

    /**
     * Adds a [cell] containing a horizontal spacer with the given [width].
     *
     * @param width the width of the horizontal spacer, or `-1` if the spacer should use all remaining horizontal space
     * @return see [cell]
     */
    fun hSpacerCell(width: Int = -1) =
        if (width == -1)
            cell(
                constraints(
                    fill = GridConstraints.FILL_HORIZONTAL,
                    hSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
                )
            ) { Spacer() }
        else
            cell(constraints(fixedWidth = width)) { Spacer() }


    fun label(name: String, text: String) = JBLabel(text).withName(name)

    fun buttonGroup(name: String): ButtonGroup {
        val group = ButtonGroup()
        state.addButtonGroup(name, group)
        return group
    }

    fun radioButton(name: String, text: String, actionCommand: String = text): JRadioButton =
        JBRadioButton(text)
            .withActionCommand(actionCommand)
            .withName(name)


    /**
     * Sets the name of this [JComponent].
     *
     * If a [JLabel] was previously added with the name `"${name}Label"`, then this label is also registered as the
     * label for `this` component.
     *
     * @param T the type of `this`
     * @param name the name to set
     * @return `this`
     */
    fun <T : JComponent> T.withName(name: String): T {
        this.name = name

        state.getLabelForOrNull(name)?.labelFor = this

        return this
    }

    /**
     * Sets the action command of `this` to [actionCommand].
     *
     * @param T the type of `this`
     * @param actionCommand the new action command of `this`
     * @return `this`
     */
    fun <T : AbstractButton> T.withActionCommand(actionCommand: String) =
        also { this.actionCommand = actionCommand }.let { this }

    /**
     * Ensures that `this` is enabled if and only if [button] is checked and [condition] is `true`.
     *
     * @param T the type of `this`
     * @param button the button of which the checked state determines whether `this` is enabled
     * @param condition an optional additional condition to check when determining whether `this` should be enabled
     * @return `this`
     */
    fun <T : JComponent> T.toggledBy(button: AbstractButton, condition: () -> Boolean = { true }): T {
        button.addChangeListener(
            { _: ChangeEvent? ->
                this.isEnabled = button.isSelected && condition()
            }.also { it(null) }
        )

        return this
    }

    /**
     * Adds `this` to [group].
     *
     * @param T the type of `this`
     * @param group the group to add `this` to
     * @return `this`
     */
    fun <T : AbstractButton> T.inGroup(group: ButtonGroup) = group.add(this).let { this }

    /**
     * Applies the mnemonic to `this` based on its text, albeit without targeting any specific component.
     *
     * @param T the type of `this`
     * @return `this`
     */
    fun <T : JLabel> T.loadMnemonic() = DialogUtil.registerMnemonic(this, null, '&').let { this }


    /**
     * Creates [GridConstraints] to be applied to a [cell].
     *
     * @param colSpan the number of columns spanned by the cell
     * @param anchor the location of the component in the cell
     * @param indent additional offset to the left
     * @param fill whether the component fills the cell
     * @param vSizePolicy determines the vertical size if the component's size changes
     * @param hSizePolicy determines the horizontal size if the component's size changes
     * @param fixedWidth the fixed, unchanging width of the cell
     * @param fixedHeight the fixed, unchanging height of the cell
     * @return the created constraints
     */
    @Suppress("detekt:LongParameterList") // Acceptable as alternative to overkill builder pattern
    fun constraints(
        colSpan: Int = 1,
        anchor: Int = GridConstraints.ANCHOR_WEST,
        indent: Int = 0,
        fill: Int = GridConstraints.FILL_NONE,
        vSizePolicy: Int = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_CAN_SHRINK,
        hSizePolicy: Int = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_CAN_SHRINK,
        fixedWidth: Int? = null,
        fixedHeight: Int? = null,
    ) = GridConstraints(
        -1,
        -1,
        1,
        colSpan,
        anchor,
        fill,
        hSizePolicy,
        vSizePolicy,
        Dimension(fixedWidth ?: -1, fixedHeight ?: -1),
        Dimension(fixedWidth ?: -1, fixedHeight ?: -1),
        Dimension(fixedWidth ?: -1, fixedHeight ?: -1),
        indent
    )


    /**
     * Holds the entry point for the grid panel DSL.
     */
    companion object {
        /**
         * Creates a panel using the grid panel DSL.
         *
         * @param spec a description written in grid panel DSL
         * @return a panel using the grid panel DSL
         */
        fun panel(spec: PanelBuilder.() -> Unit) = PanelBuilder(BuilderState()).apply(spec).build()
    }
}


/**
 * Grid builder DSL for a panel, which contains rows.
 */
class PanelBuilder(state: BuilderState) : GridPanelBuilder<Unit>(state) {
    /**
     * The list of rows that have been registered to this panel.
     */
    private val rowBuilders = mutableListOf<RowBuilder>()


    /**
     * Adds a row to this panel.
     *
     * The [spec] is invoked immediately.
     *
     * @param spec a grid builder DSL description of a row
     * @return nothing
     */
    override fun row(spec: RowBuilder.() -> Unit) {
        val builder = RowBuilder(state)
        spec(builder)
        rowBuilders.add(builder)
    }

    /**
     * Adds a cell inside a row to this panel.
     *
     * The [spec] is invoked once [build] is invoked.
     *
     * @param constraints the constraints applied to the added cell
     * @param spec a grid builder DSL description of a cell, returning the component that the cell should contain
     * @return nothing
     */
    override fun cell(constraints: GridConstraints, spec: CellBuilder.() -> JComponent) =
        row { cell(constraints, spec) }

    /**
     * Adds a panel inside a cell inside a row to this panel.
     *
     * @param spec a grid builder DSL description of a panel
     * @return nothing
     */
    override fun panel(spec: PanelBuilder.() -> Unit) =
        row { cell { panel(spec) } }


    /**
     * Determines the number of rows and columns of this panel and lays out the cells within the panel.
     *
     * @return the built panel
     */
    fun build(): JPanel {
        val panel = JPanel()

        val cols = rowBuilders.maxOfOrNull { it.cols } ?: return panel
        panel.layout = GridLayoutManager(rowBuilders.size, cols)

        rowBuilders.forEachIndexed { i, rowBuilder -> rowBuilder.build(panel, i) }

        return panel
    }
}

/**
 * Grid builder DSL for a row, which contains cells.
 */
class RowBuilder(state: BuilderState) : GridPanelBuilder<Unit>(state) {
    /**
     * The list of cells that will be added to this row when [build] is invoked.
     */
    private val cells = mutableListOf<Cell>()

    /**
     * The number of columns spanned by the added cells.
     */
    val cols get() = cells.sumOf { it.constraints?.colSpan ?: 0 }


    /**
     * Adds a cell to this row, containing the component returned by [spec].
     *
     * The [spec] is invoked once [build] is invoked.
     *
     * @param constraints the constraints applied to the added cell
     * @param spec a grid builder DSL description of a cell, returning the component that the cell should contain
     * @return nothing
     */
    override fun cell(constraints: GridConstraints, spec: CellBuilder.() -> JComponent) {
        val component = spec(CellBuilder(state))

        // Register mnemonics
        if (component is AbstractButton) DialogUtil.setTextWithMnemonic(component, component.text, '&')
        if (component is JLabel) DialogUtil.registerMnemonic(component, null, '&')

        // Add group-based buttons
        if (component is JRadioButton || component is VariableLabelRadioButton) {
            // TODO: Clean up
            val button =
                if (component is JRadioButton) component
                else if (component is VariableLabelRadioButton) component.button
                else throw Exception("")

            val group = state.getButtonGroupForOrNull(component)
            if (group != null) {
                group.add(button)

                val label = state.getLabelForOrNull(group)
                if (label != null)
                    button.addItemListener { if (button.isSelected) label.labelFor = button }
            }
        }

        // Store component
        cells.add(Cell(constraints, component))
        state.addComponent(component)
    }

    /**
     * Adds a panel inside a cell to this row.
     *
     * The [spec] is invoked once [build] is invoked.
     *
     * @param spec a grid builder DSL description of a panel
     * @return nothing
     */
    override fun panel(spec: PanelBuilder.() -> Unit) =
        cell { panel(spec) }

    /**
     * Adds a row inside a panel inside a cell to this row.
     *
     * The [spec] is invoked once [build] is invoked.
     *
     * @param spec a grid builder DSL description of a row
     * @return nothing
     */
    override fun row(spec: RowBuilder.() -> Unit) =
        cell { panel { row(spec) } }

    /**
     * Adds an empty space spanning [colSpan] columns.
     *
     * @param colSpan the number of columns to span
     */
    fun skip(colSpan: Int = 1) {
        cells.add(Cell(constraints(colSpan = colSpan), null))
    }


    /**
     * Lays out the [cells] in the [row]th row of [panel].
     *
     * @param panel the panel to add the [cell]s to
     * @param row the index of the row in [panel] to add the [cell]s to
     */
    fun build(panel: JPanel, row: Int) {
        cells.fold(0) { currentCol, (constraints, component) ->
            if (component != null && constraints != null) {
                constraints.row = row
                constraints.column = currentCol
                panel.add(component, constraints)
            }

            currentCol + (constraints?.colSpan ?: 0)
        }
    }


    /**
     * A cell to be added to a row.
     *
     * @property constraints the constraints to apply to the cell, or `null` if no cell should be added
     * @property component the component to be added, or `null` if no cell should be added
     */
    private data class Cell(val constraints: GridConstraints?, val component: JComponent?)
}

/**
 * Grid builder DSL for a cell, which contains a [JComponent].
 */
class CellBuilder(state: BuilderState) : GridPanelBuilder<JComponent>(state) {
    /**
     * Adds a panel as the component of this cell.
     *
     * @param spec a grid builder DSL description of a panel
     * @return the created panel
     */
    override fun panel(spec: PanelBuilder.() -> Unit): JPanel {
        val builder = PanelBuilder(state)
        spec(builder)
        return builder.build()
    }

    /**
     * Adds a row inside a panel as the component of this cell.
     *
     * @param spec a grid builder DSL description of a row
     * @return the created panel that surrounds the row
     */
    override fun row(spec: RowBuilder.() -> Unit) =
        panel { row(spec) }

    /**
     * Adds a cell inside a row inside a panel as the component of this cell.
     *
     * @param constraints the constraints applied to the added cell
     * @param spec a grid builder DSL description of a cell
     * @return the created panel that surrounds the row that surrounds the cell
     */
    override fun cell(constraints: GridConstraints, spec: CellBuilder.() -> JComponent) =
        panel { row { cell(constraints, spec) } }
}
