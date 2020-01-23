package com.fwdekker.randomness

import javax.swing.JComponent


/**
 * Carries information on why a component has invalid input.
 *
 * Based on [com.intellij.openapi.ui.ValidationInfo] and [com.intellij.openapi.options.ConfigurationException].
 *
 * @property message the message explaining why the component is not valid
 * @property component the component that is not valid
 * @property quickFix a runnable that can be executed to make the component valid
 */
data class ValidationInfo(val message: String, val component: JComponent? = null, val quickFix: Runnable? = null)
