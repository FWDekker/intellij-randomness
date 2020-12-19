package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataInsertAction
import com.jgoodies.forms.factories.DefaultComponentFactory
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.beans.PropertyChangeEvent
import java.util.ResourceBundle
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import kotlin.random.Random


/**
 * A panel that shows a preview of the values generated by a [DataInsertAction].
 *
 * Generates a random value that the given [DataInsertAction] could generate as a preview to the user. By reusing the
 * same seed, the generated value remains (approximately) the same and the user can easily see the effect of their
 * changes. After registering some components with this panel, the preview will be updated whenever those components are
 * updated.
 *
 * @param getGenerator returns a [DataInsertAction] that uses the given source of randomness
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class PreviewPanel(private val getGenerator: () -> DataInsertAction) {
    /**
     * The root panel containing the preview elements.
     */
    lateinit var rootPane: JPanel
    private lateinit var separator: JComponent
    private lateinit var refreshButton: JButton
    private lateinit var previewLabel: JTextArea

    private var seed = Random.nextInt()


    init {
        previewLabel.border = null
        previewLabel.isFocusable = false

        refreshButton.addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) = Unit

            override fun mouseEntered(e: MouseEvent?) = Unit

            override fun mouseClicked(e: MouseEvent?) {
                seed = Random.nextInt()
                updatePreview()
            }

            override fun mouseExited(e: MouseEvent?) = Unit

            override fun mousePressed(e: MouseEvent?) = Unit
        })
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        val bundle = ResourceBundle.getBundle("randomness")
        val factory = DefaultComponentFactory.getInstance()

        separator = factory.createSeparator(bundle.getString("settings.preview"))
    }


    /**
     * Updates the preview with the current settings.
     */
    @Suppress("SwallowedException") // Alternative is to add coupling to SettingsComponent
    fun updatePreview() {
        try {
            previewLabel.text = getGenerator().also { it.random = Random(seed) }.generateString()
        } catch (e: DataGenerationException) {
            // Ignore exception; invalid settings are handled by form validation
        } catch (e: IllegalArgumentException) {
            // Ignore exception; invalid settings are handled by form validation
        }
    }

    /**
     * Adds listeners to the given components so that this preview is updated whenever the given components are updated.
     *
     * @param components the components to add listeners to
     */
    @Suppress("SpreadOperator") // Acceptable because this method is called rarely
    fun updatePreviewOnUpdateOf(vararg components: Any) {
        components.forEach { component ->
            when (component) {
                is ActivityTableModelEditor<*> -> component.addChangeListener { updatePreview() }
                is ButtonGroup -> updatePreviewOnUpdateOf(*component.buttons())
                is JCheckBox -> component.addItemListener { updatePreview() }
                is JRadioButton -> component.addItemListener { updatePreview() }
                is JSpinner -> component.addChangeListener { updatePreview() }
                is JTextField -> component.addChangeListener { updatePreview() }
                else -> throw IllegalArgumentException("Unknown component type ${component.javaClass.canonicalName}.")
            }
        }
    }

    /**
     * Adds a `ChangeListener` to a text field.
     *
     * Code taken from [StackOverflow][https://stackoverflow.com/a/27190162/3307872].
     *
     * @param changeListener the change listener that responds to changes in the text field
     */
    private fun JTextField.addChangeListener(changeListener: (JTextField) -> Unit) {
        val dl = object : DocumentListener {
            private var lastChange = 0
            private var lastNotifiedChange = 0


            override fun changedUpdate(e: DocumentEvent?) {
                lastChange++
                SwingUtilities.invokeLater {
                    if (lastNotifiedChange == lastChange) return@invokeLater

                    lastNotifiedChange = lastChange
                    changeListener(this@addChangeListener)
                }
            }

            override fun insertUpdate(e: DocumentEvent?) = changedUpdate(e)

            override fun removeUpdate(e: DocumentEvent?) = changedUpdate(e)
        }

        this.addPropertyChangeListener("document") { e: PropertyChangeEvent ->
            (e.oldValue as? Document)?.removeDocumentListener(dl)
            (e.newValue as? Document)?.addDocumentListener(dl)
            dl.changedUpdate(null)
        }
        this.document.addDocumentListener(dl)
    }
}
