package com.fwdekker.randomness.datetime

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.StateEditor
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.GridPanelBuilder
import com.fwdekker.randomness.ui.JDateTimeField
import com.fwdekker.randomness.ui.UIConstants
import com.fwdekker.randomness.ui.addChangeListener
import com.fwdekker.randomness.ui.addChangeListenerTo
import com.intellij.ui.components.BrowserLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.util.ui.JBUI
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants


/**
 * Component for editing random date-time settings.
 *
 * @param scheme the scheme to edit in the component
 */
class DateTimeSchemeEditor(scheme: DateTimeScheme = DateTimeScheme()) : StateEditor<DateTimeScheme>(scheme) {
    override val rootComponent: JPanel
    override val preferredFocusedComponent
        get() = rootComponent

    private lateinit var minDateTimeField: JDateTimeField
    private lateinit var maxDateTimeField: JDateTimeField
    private lateinit var patternField: JTextField
    private lateinit var arrayDecoratorEditor: ArrayDecoratorEditor


    init {
        rootComponent = GridPanelBuilder.panel {
            textSeparator(Bundle("datetime.ui.value_separator"))

            panel {
                row {
                    lateinit var minCountLabel: JLabel

                    cell {
                        JBLabel(Bundle("datetime.ui.min_datetime_option"))
                            .also { minCountLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_LARGE)) {
                        JDateTimeField(DateTimeScheme.DEFAULT_MIN_DATE_TIME.toLocalDateTime())
                            .withName("minDateTime")
                            .setLabel(minCountLabel)
                            .also { minDateTimeField = it }
                    }
                }

                row {
                    lateinit var maxCountLabel: JLabel

                    cell {
                        JBLabel(Bundle("datetime.ui.max_datetime_option"))
                            .also { maxCountLabel = it }
                    }

                    cell(constraints(fixedWidth = UIConstants.SIZE_LARGE)) {
                        JDateTimeField(DateTimeScheme.DEFAULT_MAX_DATE_TIME.toLocalDateTime())
                            .withName("maxDateTime")
                            .setLabel(maxCountLabel)
                            .also { maxDateTimeField = it }
                    }

                    run { bindDateTimes(minDateTimeField, maxDateTimeField) }
                }

                row {
                    lateinit var patternLabel: JLabel

                    cell {
                        JBLabel(Bundle("datetime.ui.pattern_option"))
                            .also { patternLabel = it }
                    }

                    row {
                        cell(constraints(fixedWidth = UIConstants.SIZE_VERY_LARGE)) {
                            JBTextField()
                                .withName("pattern")
                                .setLabel(patternLabel)
                                .also { patternField = it }
                        }

                        cell {
                            BrowserLink(
                                Bundle("datetime.ui.pattern_help"),
                                "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html"
                            )
                        }
                    }
                }

                row {
                    skip()

                    cell {
                        JBLabel(Bundle("datetime.ui.pattern_comment"))
                            .also {
                                it.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                                it.isFocusable = false
                                it.setCopyable(false)
                                it.verticalTextPosition = SwingConstants.TOP
                            }
                    }
                }
            }

            vSeparator()

            cell(constraints(fill = GridConstraints.FILL_HORIZONTAL)) {
                ArrayDecoratorEditor(originalState.arrayDecorator)
                    .also { arrayDecoratorEditor = it }
                    .rootComponent
            }

            vSpacer()
        }

        loadState()
    }

    /**
     * Binds two `DateTimePicker`s together, analogous to how [com.fwdekker.randomness.ui.bindSpinners] works.
     */
    private fun bindDateTimes(minField: JDateTimeField, maxField: JDateTimeField) {
        minField.addChangeListener {
            val minEpoch = minField.value.toEpochMilli()
            val maxEpoch = maxField.value.toEpochMilli()

            if (minEpoch > maxEpoch) maxField.value = minField.value
        }
        maxField.addChangeListener {
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
            arrayDecorator = arrayDecoratorEditor.readState()
        ).also { it.uuid = originalState.uuid }


    override fun addChangeListener(listener: () -> Unit) =
        addChangeListenerTo(
            minDateTimeField, maxDateTimeField, patternField, arrayDecoratorEditor,
            listener = listener
        )
}
