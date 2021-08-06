package com.fwdekker.randomness.literal

import com.fwdekker.randomness.SchemeEditor
import com.fwdekker.randomness.ValidationInfo
import com.fwdekker.randomness.ui.addChangeListenerTo
import javax.swing.JPanel
import javax.swing.JTextField


class LiteralSchemeEditor(scheme: LiteralScheme = LiteralScheme()) : SchemeEditor<LiteralScheme>() {
    override lateinit var rootPane: JPanel private set
    private lateinit var literalInput: JTextField


    init {
        loadScheme(scheme)
    }


    override fun loadScheme(scheme: LiteralScheme) = scheme.also { literalInput.text = scheme.literal }.let { }

    override fun saveScheme() = LiteralScheme(literal = literalInput.text)

    override fun doValidate(): ValidationInfo? = null

    override fun addChangeListener(listener: () -> Unit) = addChangeListenerTo(literalInput, listener = listener)
}
