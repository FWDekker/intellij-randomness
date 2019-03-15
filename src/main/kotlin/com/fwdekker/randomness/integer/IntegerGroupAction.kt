package com.fwdekker.randomness.integer

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting integers.
 */
class IntegerGroupAction : DataGroupAction() {
    override val insertAction = IntegerInsertAction()
    override val insertArrayAction = IntegerInsertAction().ArrayAction()
    override val settingsAction = IntegerSettingsAction()
}
