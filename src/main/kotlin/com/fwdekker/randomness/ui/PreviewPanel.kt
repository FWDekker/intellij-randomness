package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataInsertAction
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner
import kotlin.random.Random


/**
 * A panel that shows a preview of the values generated by a [DataInsertAction].
 *
 * Generates a random value that the given [DataInsertAction] could generate as a preview to the user. By reusing the
 * same seed, the generated value remains (approximately) the same and the user can easily see the effect of their
 * changes. After registering some components with this panel, the preview will be updated whenever those components are
 * updated.
 *
 * @param T the type of [DataInsertAction]
 * @property getGenerator returns a [DataInsertAction] that uses the given source of randomness
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class PreviewPanel<T : DataInsertAction>(private val getGenerator: () -> T) {
    /**
     * The root panel containing the preview elements.
     */
    lateinit var rootPane: JPanel
    private lateinit var refreshButton: JButton
    private lateinit var previewLabel: JLabel

    private var seed = Random.nextInt()


    init {
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
     * Updates the preview with the current settings.
     */
    @Suppress("SwallowedException") // Alternative is to add coupling to SettingsComponent
    fun updatePreview() {
        try {
            previewLabel.text = "" +
                "<html>" +
                getGenerator().also { it.random = Random(seed) }
                    .generateString()
                    .replace("<", "&lt;")
                    .replace("\n", "<br>") +
                "</html>"
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
                is JSpinner -> component.addChangeListener { updatePreview() }
                is JRadioButton -> component.addItemListener { updatePreview() }
                is JCheckBox -> component.addItemListener { updatePreview() }
                is ActivityTableModelEditor<*> -> component.addChangeListener { updatePreview() }
                is ButtonGroup -> updatePreviewOnUpdateOf(*component.buttons())
                else -> throw IllegalArgumentException("Unknown component type ${component.javaClass.canonicalName}.")
            }
        }
    }
}
