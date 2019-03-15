package com.fwdekker.randomness.string

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting strings.
 */
class StringGroupAction : DataGroupAction() {
    override fun getInsertAction() = StringInsertAction()

    override fun getInsertArrayAction() = StringInsertAction().ArrayAction()

    override fun getSettingsAction() = StringSettingsAction()
}
