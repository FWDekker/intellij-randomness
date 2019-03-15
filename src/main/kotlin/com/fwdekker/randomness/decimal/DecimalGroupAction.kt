package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting decimals.
 */
class DecimalGroupAction : DataGroupAction() {
    override val insertAction = DecimalInsertAction()
    override val insertArrayAction = DecimalInsertAction().ArrayAction()
    override val settingsAction = DecimalSettingsAction()
}
