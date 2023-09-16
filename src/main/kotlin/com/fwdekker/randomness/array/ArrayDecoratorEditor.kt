package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.affix.AffixDecoratorEditor
import com.fwdekker.randomness.array.ArrayDecorator.Companion.MIN_MIN_COUNT
import com.fwdekker.randomness.array.ArrayDecorator.Companion.PRESET_AFFIX_DECORATOR_DESCRIPTORS
import com.fwdekker.randomness.array.ArrayDecorator.Companion.PRESET_SEPARATORS
import com.fwdekker.randomness.ui.JIntSpinner
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.bindCurrentText
import com.fwdekker.randomness.ui.bindIntValue
import com.fwdekker.randomness.ui.bindSpinners
import com.fwdekker.randomness.ui.indentedRowRange
import com.fwdekker.randomness.ui.isEditable
import com.fwdekker.randomness.ui.loadMnemonic
import com.fwdekker.randomness.ui.withFixedWidth
import com.fwdekker.randomness.ui.withName
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.and
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox


/**
 * Component for editing an [ArrayDecorator].
 *
 * @param scheme the scheme to edit
 * @property embedded `true` if the editor is embedded, which means that no titled separator is shown at the top, and
 * the user cannot disable the array decorator; [apply] always enables the array decorator.
 */
class ArrayDecoratorEditor(
    scheme: ArrayDecorator,
    private val embedded: Boolean = false,
) : SchemeEditor<ArrayDecorator>(scheme) {
    override val rootComponent = panel {
        separator(Bundle("array.title"))
            .topGap(TopGap.MEDIUM)
            .visible(!embedded)

        indentedRowRange(indented = !embedded) {
            lateinit var enabledCheckBox: Cell<JCheckBox>

            row {
                checkBox(Bundle("array.ui.enabled"))
                    .loadMnemonic()
                    .withName("arrayEnabled")
                    .bindSelected(scheme::enabled)
                    .also { enabledCheckBox = it }
            }.visible(!embedded)

            indentedRowRange(indented = !embedded) {
                lateinit var minCountSpinner: JIntSpinner
                lateinit var maxCountSpinner: JIntSpinner

                row(Bundle("array.ui.min_count_option")) {
                    cell(JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT))
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .withName("arrayMinCount")
                        .bindIntValue(scheme::minCount)
                        .also { minCountSpinner = it.component }
                }

                row(Bundle("array.ui.max_count_option")) {
                    cell(JIntSpinner(value = MIN_MIN_COUNT, minValue = MIN_MIN_COUNT))
                        .withFixedWidth(UIConstants.SIZE_SMALL)
                        .withName("arrayMaxCount")
                        .bindIntValue(scheme::maxCount)
                        .also { maxCountSpinner = it.component }
                }.bottomGap(BottomGap.SMALL)

                bindSpinners(minCountSpinner, maxCountSpinner)

                row {
                    lateinit var separatorEnabledCheckBox: JCheckBox

                    checkBox(Bundle("array.ui.separator.option"))
                        .withName("arraySeparatorEnabled")
                        .bindSelected(scheme::separatorEnabled)
                        .also { separatorEnabledCheckBox = it.component }

                    cell(ComboBox(PRESET_SEPARATORS))
                        .enabledIf(enabledCheckBox.selected.and(separatorEnabledCheckBox.selected))
                        .isEditable(true)
                        .withName("arraySeparator")
                        .bindCurrentText(scheme::separator)
                }

                row {
                    AffixDecoratorEditor(
                        scheme.affixDecorator,
                        PRESET_AFFIX_DECORATOR_DESCRIPTORS,
                        enabledIf = enabledCheckBox.selected,
                        enableMnemonic = false,
                        namePrefix = "array",
                    )
                        .also { decoratorEditors += it }
                        .let { cell(it.rootComponent) }
                }
            }.enabledIf(enabledCheckBox.selected)
        }
    }


    init {
        reset()
    }
}
