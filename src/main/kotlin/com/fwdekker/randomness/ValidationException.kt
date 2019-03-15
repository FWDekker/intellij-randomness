package com.fwdekker.randomness

import javax.swing.JComponent


/**
 * An exception indicating that the validation of some [JComponent] has failed.
 *
 * @param message   the detail message
 * @param component the component
 * @see Exception
 */
class ValidationException(message: String, val component: JComponent) : Exception(message)
