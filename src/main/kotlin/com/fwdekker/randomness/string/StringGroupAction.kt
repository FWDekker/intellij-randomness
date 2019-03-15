package com.fwdekker.randomness.string

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting strings.
 */
class StringGroupAction : DataGroupAction() {
    override val insertAction = StringInsertAction()
    override val insertArrayAction = StringInsertAction().ArrayAction()
    override val settingsAction = StringSettingsAction()
}
