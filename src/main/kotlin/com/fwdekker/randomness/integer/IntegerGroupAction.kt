package com.fwdekker.randomness.integer

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.SettingsAction


/**
 * All actions related to inserting integers.
 */
class IntegerGroupAction : DataGroupAction() {
    override fun getInsertAction(): DataInsertAction = IntegerInsertAction()

    override fun getInsertArrayAction(): DataInsertAction.ArrayAction = IntegerInsertAction().ArrayAction()

    override fun getSettingsAction(): SettingsAction = IntegerSettingsAction()
}
