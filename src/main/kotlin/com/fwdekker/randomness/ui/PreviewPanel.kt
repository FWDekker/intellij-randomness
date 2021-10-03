package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Timely.generateTimely
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.ui.InplaceButton
import com.intellij.ui.SeparatorFactory
import com.intellij.ui.TitledSeparator
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.random.Random


/**
 * A panel that shows a preview of the values generated by a scheme.
 *
 * Generates a random value that the scheme from [getScheme] could generate as a preview to the user. By reusing the
 * same seed, the generated value remains (approximately) the same and the user can easily see the effect of their
 * changes. After registering some components with this panel, the preview will be updated whenever those components are
 * updated.
 *
 * Note that [dispose] should be invoked when this panel is no longer used.
 *
 * @property getScheme Returns a scheme that generates previews. Its random source will be changed.
 */
@Suppress("LateinitUsage") // Initialized by scene builder
class PreviewPanel(private val getScheme: () -> Scheme) : Disposable {
    /**
     * The root panel containing the preview elements.
     */
    lateinit var rootComponent: JPanel private set
    private lateinit var separator: TitledSeparator
    private lateinit var refreshButton: JComponent
    private lateinit var previewDocument: Document
    private lateinit var previewEditor: Editor
    private lateinit var previewComponent: JComponent

    /**
     * The current seed to generate data with.
     */
    private var seed = Random.nextInt()

    /**
     * The text currently displayed as the preview.
     */
    var previewText: String
        get() = previewDocument.text
        set(value) = runWriteAction { previewDocument.setText(value) }


    /**
     * Initializes custom UI components.
     *
     * This method is called by the scene builder at the start of the constructor.
     */
    @Suppress("UnusedPrivateMember") // Used by scene builder
    private fun createUIComponents() {
        separator = SeparatorFactory.createSeparator(Bundle("preview.title"), null)
        refreshButton = InplaceButton(Bundle("shared.action.refresh"), AllIcons.Actions.Refresh) {
            seed = Random.nextInt()
            updatePreview()
        }

        val factory = EditorFactory.getInstance()
        previewDocument = factory.createDocument(Bundle("preview.placeholder"))
        previewEditor = factory.createViewer(previewDocument)
        previewComponent = previewEditor.component
    }

    /**
     * Disposes of this panel's resources, to be used when this panel is no longer used.
     */
    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(previewEditor)
    }


    /**
     * Updates the preview with the current settings.
     */
    @Suppress("SwallowedException") // Alternative is to add coupling to SettingsComponent
    fun updatePreview() {
        try {
            previewText = generateTimely { getScheme().also { it.random = Random(seed) }.generateStrings() }.first()
        } catch (e: DataGenerationException) {
            previewText = Bundle("preview.invalid", e.message)
        } catch (e: IllegalArgumentException) {
            // Ignore exception; invalid settings are handled by form validation
        }
    }
}
