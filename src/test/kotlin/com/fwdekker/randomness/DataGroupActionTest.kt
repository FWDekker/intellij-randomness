package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat


/**
 * Unit tests for [DataGroupAction]s.
 *
 * @property group the group to test
 */
abstract class DataGroupActionTest(private val group: () -> DataGroupAction) : BasePlatformTestCase() {
    private fun <T : AnAction> testAction(action: T, getName: (T) -> String) {
        val event = TestActionEvent(action)
        action.update(event)
        assertThat(event.presentation.text).isEqualTo(getName(action))
    }


    fun testInsertAction() = testAction(group().insertAction) { it.name }

    fun testInsertArrayAction() = testAction(group().insertArrayAction) { it.name }

    fun testInsertRepeatAction() = testAction(group().insertRepeatAction) { it.name }

    fun testInsertRepeatArrayAction() = testAction(group().insertRepeatArrayAction) { it.name }

    fun testSettingsAction() = testAction(group().settingsAction) { it.name }

    fun testQuickSwitchSchemeAction() = testAction(group().quickSwitchSchemeAction) { it.name }

    fun testQuickSwitchArraySchemeAction() = testAction(group().quickSwitchArraySchemeAction) { it.name }
}
