package com.fwdekker.randomness.ui

import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter
import kotlin.math.min


/**
 * A document filter that ensures the document always contains at most [maxLength] characters.
 *
 * @property maxLength The maximum length of the text in the document, must be at least `1`.
 */
open class MaxLengthDocumentFilter(private val maxLength: Int) : DocumentFilter() {
    init {
        require(maxLength >= 1) { "Maximum length must be a positive number, but was '$maxLength'." }
    }


    /**
     * Inserts [text] into the document with attributes [attr], using [fb] to bypass recursively invoking the filter.
     *
     * While the document's length is not at its maximum, the characters of [text] are inserted at [offset]. Once the
     * document hits its maximum, the subsequent characters are overwritten by the remainder of [text]. If [text] is
     * still too long, the last character in the document is replaced by the last character of [text].
     */
    override fun insertString(fb: FilterBypass, offset: Int, text: String?, attr: AttributeSet?) {
        if (text == null) return

        val rawLength = fb.document.length + text.length
        if (rawLength <= maxLength) {
            fb.insertString(offset, text, attr)
            return
        }

        val selectableDesired = rawLength - maxLength
        val selectablePossible = fb.document.length - offset
        val insertable = maxLength - offset
        fb.replace(offset, min(selectableDesired, selectablePossible), text.take(insertable), attr)

        if (offset + text.length >= maxLength)
            fb.replace(maxLength - 1, 1, text.takeLast(1), attr)
    }

    /**
     * Replaces [length] characters starting at [offset] with [text], applying attributes [attrs], using [fb] to bypass
     * recursively invoking the filter.
     */
    override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String?, attrs: AttributeSet?) {
        remove(fb, offset, length)
        insertString(fb, offset, text, attrs)
    }
}

/**
 * Similar to [MaxLengthDocumentFilter], but the length cannot go below [minLength] either.
 *
 * @property minLength The minimum allowable length of the document. If the length is initially below this value, any
 * progress made towards this value is accepted but cannot be undone.
 * @param maxLength the maximum allowable length of the document
 */
class MinMaxLengthDocumentFilter(private val minLength: Int, maxLength: Int) : MaxLengthDocumentFilter(maxLength) {
    init {
        require(minLength <= maxLength) { "Minimum length must be less than or equal to maximum length." }
    }


    /**
     * Removes [length] characters starting at [offset] from the document, using [fb] to bypass recursively invoking the
     * filter.
     */
    override fun remove(fb: FilterBypass, offset: Int, length: Int) {
        val removable = fb.document.length - minLength
        if (removable < 0) return

        val willRemove = min(removable, length)
        super.remove(fb, offset + (length - willRemove), willRemove)
    }
}
