package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting decimals.
 */
class DecimalGroupAction : DataGroupAction() {
    override fun getInsertAction() = DecimalInsertAction()

    override fun getInsertArrayAction() = DecimalInsertAction().ArrayAction()

    override fun getSettingsAction() = DecimalSettingsAction()
}
