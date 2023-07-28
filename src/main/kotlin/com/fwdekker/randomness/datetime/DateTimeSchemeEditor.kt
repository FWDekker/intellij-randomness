package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.JDateTimeField
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.fwdekker.randomness.ui.withFixedWidth
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JPanel
import javax.swing.JTextField


/**
 * Component for editing random date-time settings.
 *
 * @param scheme the scheme to edit in the component
 */
class DateTimeSchemeEditor(scheme: DateTimeScheme = DateTimeScheme()) : StateEditor<DateTimeScheme>(scheme) {
    override val rootComponent: JPanel
    override val stateComponents get() = super.stateComponents + arrayDecoratorEditor
    override val preferredFocusedComponent get() = minDateTimeField

    private lateinit var minDateTimeField: JDateTimeField
    private lateinit var maxDateTimeField: JDateTimeField
    private lateinit var patternField: JTextField
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = panel {
            group(Bundle("datetime.ui.value.header")) {
                row(Bundle("datetime.ui.value.min_datetime_option")) {
                    cell(JDateTimeField(DateTimeScheme.DEFAULT_MIN_DATE_TIME.toLocalDateTime()))
                        .withFixedWidth(UIConstants.SIZE_LARGE)
                        .also { it.component.name = "minDateTime" }
                        .also { minDateTimeField = it.component }
                }

                row(Bundle("datetime.ui.value.max_datetime_option")) {
                    cell(JDateTimeField(DateTimeScheme.DEFAULT_MAX_DATE_TIME.toLocalDateTime()))
                        .withFixedWidth(UIConstants.SIZE_LARGE)
                        .also { it.component.name = "maxDateTime" }
                        .also { maxDateTimeField = it.component }
                }.bottomGap(BottomGap.SMALL)

                bindDateTimes(minDateTimeField, maxDateTimeField)

                row(Bundle("datetime.ui.value.pattern_option")) {
                    textField()
                        .withFixedWidth(UIConstants.SIZE_VERY_LARGE)
                        .comment(Bundle("datetime.ui.pattern_comment"))
                        .also { it.component.name = "pattern" }
                        .also { patternField = it.component }

                    browserLink(Bundle("datetime.ui.value.pattern_help"), Bundle("datetime.ui.value.pattern_help_url"))
                }
            }

            row {
                arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
                cell(arrayDecoratorEditor.rootComponent).horizontalAlign(HorizontalAlign.FILL)
            }
        }

        loadState()
    }

    /**
     * Binds two `DateTimePicker`s together, analogous to how [com.fwdekker.randomness.ui.bindSpinners] works.
     */
    private fun bindDateTimes(minField: JDateTimeField, maxField: JDateTimeField) {
        addChangeListenerTo(minField) {
            val minEpoch = minField.value.toEpochMilli()
            val maxEpoch = maxField.value.toEpochMilli()

            if (minEpoch > maxEpoch) maxField.value = minField.value
        }
        addChangeListenerTo(maxField) {
            val minEpoch = minField.value.toEpochMilli()
            val maxEpoch = maxField.value.toEpochMilli()

            if (maxEpoch < minEpoch) minField.value = maxField.value
        }
    }


    override fun loadState(state: DateTimeScheme) {
        super.loadState(state)

        minDateTimeField.value = state.minDateTime.toLocalDateTime()
        maxDateTimeField.value = state.maxDateTime.toLocalDateTime()
        patternField.text = state.pattern
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        DateTimeScheme(
            minDateTime = minDateTimeField.value.toEpochMilli(),
            maxDateTime = maxDateTimeField.value.toEpochMilli(),
            pattern = patternField.text,
            arrayDecorator = arrayDecoratorEditor.readState(),
        ).also { it.uuid = originalState.uuid }
}
