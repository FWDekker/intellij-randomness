package com.fwdekker.randomness.ui

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.generateTimely
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import java.util.ResourceBundle
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea
import kotlin.random.Random


/**
 * A panel that shows a preview of the values generated by a scheme.
 *
 * Generates a random value that the given scheme could generate as a preview to the user. By reusing the same seed, the
 * generated value remains (approximately) the same and the user can easily see the effect of their changes. After
 * registering some components with this panel, the preview will be updated whenever those components are updated.
 *
 * @property getGenerator Returns a scheme that uses the given source of randomness.
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class PreviewPanel(private val getGenerator: () -> Scheme) {
    /**
     * The root panel containing the preview elements.
     */
    lateinit var rootComponent: JPanel private set
    private lateinit var separator: TitledSeparator
    private lateinit var refreshButton: JButton
    private lateinit var previewLabel: JTextArea

    private var seed = Random.nextInt()


    init {
        previewLabel.border = null
        previewLabel.isFocusable = false

        refreshButton.addActionListener {
            seed = Random.nextInt()
            updatePreview()
        }
    }


    /**
     * Initialises custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        val bundle = ResourceBundle.getBundle("randomness")

        separator = SeparatorFactory.createSeparator(bundle.getString("settings.preview"), null)
    }


    /**
     * Updates the preview with the current settings.
     */
    @Suppress("SwallowedException") // Alternative is to add coupling to SettingsComponent
    fun updatePreview() {
        try {
            previewLabel.text =
                generateTimely { getGenerator().also { it.random = Random(seed) }.generateStrings() }.first()
        } catch (e: DataGenerationException) {
            previewLabel.text = "Settings are invalid: ${e.message}"
        } catch (e: IllegalArgumentException) {
            // Ignore exception; invalid settings are handled by form validation
        }
    }
}
