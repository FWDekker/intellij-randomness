package com.fwdekker.randomness.ui

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Timely.generateTimely
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.undo.UndoUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.util.Disposer
import com.intellij.ui.InplaceButton
import com.intellij.ui.SeparatorFactory
import java.awt.BorderLayout
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
 * @property getScheme Returns a scheme that generates previews. Its random source will be changed.
 */
@Suppress("detekt:LateinitUsage") // Initialized in panel DSL
class PreviewPanel(private val getScheme: () -> Scheme) : Disposable {
    /**
     * The root panel containing the preview elements.
     */
    val rootComponent: JPanel
    private val refreshButton: JComponent
    private val previewDocument: Document

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


    init {
        // Components
        refreshButton =
            InplaceButton(Bundle("shared.action.refresh"), AllIcons.Actions.Refresh) {
                seed = Random.nextInt()
                updatePreview()
            }

        val factory = EditorFactory.getInstance()
        previewDocument = factory.createDocument(Bundle("preview.placeholder")).also { UndoUtil.disableUndoFor(it) }
        val previewEditor = factory.createViewer(previewDocument)
        previewEditor.component.setFixedHeight(UIConstants.SIZE_MEDIUM)
        Disposer.register(this) { EditorFactory.getInstance().releaseEditor(previewEditor) }

        // Layout
        rootComponent = JPanel(BorderLayout())
        val header = JPanel(BorderLayout())
        header.add(SeparatorFactory.createSeparator(Bundle("preview.title"), null), BorderLayout.CENTER)
        header.add(refreshButton, BorderLayout.EAST)

        rootComponent.add(header, BorderLayout.NORTH)
        rootComponent.add(previewEditor.component, BorderLayout.CENTER)
    }

    /**
     * Disposes this panel's resources, to be used when this panel is no longer used.
     */
    override fun dispose() = Unit


    /**
     * Updates the preview with the current settings.
     */
    @Suppress("SwallowedException") // Alternative is to add coupling to `SettingsComponent`
    fun updatePreview() {
        try {
            previewText = generateTimely { getScheme().also { it.random = Random(seed) }.generateStrings() }.first()
        } catch (exception: DataGenerationException) {
            previewText = Bundle("preview.invalid", exception.message)
        } catch (exception: IllegalArgumentException) {
            // Ignore exception; invalid settings are handled by form validation
        }
    }
}
