package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettingsAction
import com.intellij.codeInsight.hint.HintManager
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.QuickSwitchSchemeAction
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import icons.RandomnessIcons
import java.awt.event.ActionEvent
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.swing.Icon
import kotlin.random.Random


/**
 * Thrown if a random datum could not be generated.
 *
 * @param message the detail message
 * @param cause the cause
 */
class DataGenerationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)


/**
 * A group of actions for a particular type of random data that can be generated.
 *
 * @property icon the icon to display with the action
 */
abstract class DataGroupAction(private val icon: Icon = RandomnessIcons.Data.Base) : ActionGroup() {
    /**
     * The action used to insert single data.
     */
    abstract val insertAction: DataInsertAction

    /**
     * The action used to insert arrays of data.
     */
    abstract val insertArrayAction: DataInsertArrayAction

    /**
     * The action used to insert repeated single data.
     */
    abstract val insertRepeatAction: DataInsertRepeatAction

    /**
     * The action used to insert repeated arrays of data.
     */
    abstract val insertRepeatArrayAction: DataInsertRepeatArrayAction

    /**
     * The action used to edit the generator settings for this data type.
     */
    abstract val settingsAction: DataSettingsAction

    /**
     * The action used to quickly switch between schemes of this data type.
     */
    abstract val quickSwitchSchemeAction: DataQuickSwitchSchemeAction<*>

    /**
     * The action used to quickly switch between array schemes.
     */
    abstract val quickSwitchArraySchemeAction: DataQuickSwitchSchemeAction<*>


    /**
     * Returns the insert action, array insert action, and settings action.
     *
     * @param event carries information on the invocation place
     * @return the insert action, array insert action, and settings action
     */
    override fun getChildren(event: AnActionEvent?) =
        arrayOf(insertArrayAction, insertRepeatAction, insertRepeatArrayAction, settingsAction, quickSwitchSchemeAction)

    /**
     * Returns `true`.
     *
     * @param context carries information about the context of the invocation
     * @return `true`
     */
    override fun canBePerformed(context: DataContext) = true

    /**
     * Chooses one of the three actions to execute based on the key modifiers in [event].
     *
     * @param event carries information on the invocation place
     */
    override fun actionPerformed(event: AnActionEvent) {
        val altPressed = event.modifiers and ActionEvent.ALT_MASK != 0
        val ctrlPressed = event.modifiers and ActionEvent.CTRL_MASK != 0
        val shiftPressed = event.modifiers and ActionEvent.SHIFT_MASK != 0

        // alt behavior is handled by implementation of `actionPerformed`
        when {
            altPressed && ctrlPressed && shiftPressed -> quickSwitchArraySchemeAction.actionPerformed(event)
            altPressed && ctrlPressed -> quickSwitchSchemeAction.actionPerformed(event)
            altPressed && shiftPressed -> insertRepeatArrayAction.actionPerformed(event)
            ctrlPressed && shiftPressed -> ArraySettingsAction().actionPerformed(event)
            altPressed -> insertRepeatAction.actionPerformed(event)
            ctrlPressed -> settingsAction.actionPerformed(event)
            shiftPressed -> insertArrayAction.actionPerformed(event)
            else -> insertAction.actionPerformed(event)
        }
    }

    /**
     * Sets the title of this action.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.text = insertAction.name
        event.presentation.icon = icon
    }

    /**
     * Returns `true`.
     *
     * @return `true`
     */
    override fun isPopup() = true
}


/**
 * Inserts randomly generated strings at the event's editor's carets.
 *
 * @property icon the icon to display with the action
 */
abstract class DataInsertAction(private val icon: Icon) : AnAction() {
    /**
     * The name of the action to display.
     */
    abstract val name: String

    /**
     * The random generator used to generate random values.
     */
    var random: Random = Random.Default


    /**
     * Sets the title of this action and disables this action if no editor is currently opened.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        val editor = event.getData(CommonDataKeys.EDITOR)

        presentation.text = name
        presentation.icon = icon
        presentation.isEnabled = editor != null
    }

    /**
     * Inserts the data generated by [generateStrings] at the caret(s) in the editor; one datum for each caret.
     *
     * @param event carries information on the invocation place
     */
    @Suppress("ReturnCount") // Result of null checks at start
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
            ?: return
        val project = event.getData(CommonDataKeys.PROJECT)
            ?: return

        val data =
            try {
                generateStringsTimely(editor.caretModel.caretCount)
            } catch (e: DataGenerationException) {
                HintManager.getInstance().showErrorHint(
                    editor,
                    """
                        Randomness was unable to generate random data.
                        ${if (!e.message.isNullOrBlank()) "The following error was encountered: ${e.message}\n" else ""}
                        Check your Randomness settings and try again.
                    """.trimIndent()
                )
                return
            }

        WriteCommandAction.runWriteCommandAction(project) {
            editor.caretModel.allCarets.forEachIndexed { i, caret ->
                val start = caret.selectionStart
                val end = caret.selectionEnd
                val newEnd = start + data[i].length

                editor.document.replaceString(start, end, data[i])
                caret.setSelection(start, newEnd)
            }
        }
    }

    /**
     * Generates a random datum.
     *
     * @return a random datum
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    fun generateString() = generateStrings(1).first()

    /**
     * Generates a random datum, or throws an exception if it takes longer than [GENERATOR_TIMEOUT] milliseconds.
     *
     * @return a random datum
     * @throws DataGenerationException if data could not be generated in time
     */
    @Throws(DataGenerationException::class)
    fun generateStringTimely() = generateTimely { generateString() }

    /**
     * Generates random data.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    abstract fun generateStrings(count: Int = 1): List<String>

    /**
     * Generates random data, or throws an exception if it takes longer than [GENERATOR_TIMEOUT] milliseconds.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated in time
     */
    @Throws(DataGenerationException::class)
    fun generateStringsTimely(count: Int = 1) = generateTimely { generateStrings(count) }

    /**
     * Runs the given function and returns its return value, or throws an exception if it takes longer than
     * [GENERATOR_TIMEOUT] milliseconds.
     *
     * @param T the return type of [generator]
     * @param generator the function to call
     * @return the return value of [generator]
     * @throws DataGenerationException if data could not be generated in time
     */
    @Throws(DataGenerationException::class)
    private fun <T> generateTimely(generator: () -> T): T {
        val executor = Executors.newSingleThreadExecutor()
        try {
            return executor.submit<T> { generator() }.get(GENERATOR_TIMEOUT, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            throw DataGenerationException("Timed out while generating data.", e)
        } catch (e: ExecutionException) {
            throw DataGenerationException(e.cause?.message ?: e.message, e)
        } finally {
            executor.shutdown()
        }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The timeout in milliseconds before the generator should be interrupted when generating a value.
         */
        const val GENERATOR_TIMEOUT = 5000L
    }
}


/**
 * Inserts randomly generated arrays of strings at the event's editor's carets.
 *
 * @property arrayScheme the scheme to use for generating arrays
 * @property dataInsertAction the action to generate data with
 * @param icon the icon to display with the action
 */
abstract class DataInsertArrayAction(
    private val arrayScheme: () -> ArrayScheme,
    private val dataInsertAction: DataInsertAction,
    icon: Icon = RandomnessIcons.Data.Array
) : DataInsertAction(icon) {
    /**
     * Generates array-like strings of random data.
     *
     * @param count the number of array-like strings to generate
     * @return array-like strings of random data
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    override fun generateStrings(count: Int): List<String> {
        val arrayScheme = arrayScheme()
        if (arrayScheme.count <= 0)
            throw DataGenerationException("Array cannot have fewer than 1 element.")

        dataInsertAction.random = random
        return dataInsertAction.generateStrings(count * arrayScheme.count)
            .chunked(arrayScheme.count)
            .map { arrayScheme.arrayify(it) }
    }
}


/**
 * Inserts the same randomly generated string at the event's editor's carets.
 *
 * @property dataInsertAction the action to generate data with
 * @param icon the icon to display with the action
 */
abstract class DataInsertRepeatAction(
    private val dataInsertAction: DataInsertAction,
    icon: Icon = RandomnessIcons.Data.Array
) : DataInsertAction(icon) {
    /**
     * Generates a random datum and repeats it [count] times.
     *
     * @param count the number of times to repeat the data
     * @return a random datum, repeated [count] times
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    override fun generateStrings(count: Int): List<String> {
        dataInsertAction.random = random
        return dataInsertAction.generateString().let { string -> List(count) { string } }
    }
}


/**
 * Inserts the same randomly generated array of strings at the event's editor's carets.
 *
 * @property dataInsertArrayAction the action to generate data with
 * @param icon the icon to display with the action
 */
abstract class DataInsertRepeatArrayAction(
    private val dataInsertArrayAction: DataInsertArrayAction,
    icon: Icon = RandomnessIcons.Data.Array
) : DataInsertAction(icon) {
    /**
     * Generates a random array-like string of random data and repeats it [count] times.
     *
     * @param count the number of times to repeat the data
     * @return a random array-like string of random data and repeats it [count] times
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    override fun generateStrings(count: Int) =
        dataInsertArrayAction.generateString().let { string -> List(count) { string } }
}


/**
 * Opens the settings window for changing settings.
 *
 * @param icon the icon to display with the action
 */
abstract class DataSettingsAction(private val icon: Icon = RandomnessIcons.Data.Settings) : AnAction() {
    /**
     * The name of the action.
     */
    abstract val name: String

    /**
     * The class of the configurable maintaining the settings.
     */
    protected abstract val configurableClass: Class<out SettingsConfigurable<*, *>>


    /**
     * Sets the title of this action.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.text = name
        event.presentation.icon = icon
    }

    /**
     * Opens the IntelliJ settings menu at the right location to adjust the configurable of type [configurableClass].
     *
     * @param event carries information on the invocation place
     */
    override fun actionPerformed(event: AnActionEvent) =
        ShowSettingsUtil.getInstance().showSettingsDialog(event.project, configurableClass)
}


/**
 * Opens a popup to allow the user to quickly switch to the selected scheme.
 *
 * @param T the type of scheme that can be switched between
 * @property settings the settings containing the schemes that can be switched between
 * @property icon the icon to present with this action
 */
abstract class DataQuickSwitchSchemeAction<T : Scheme<T>>(
    private val settings: Settings<*, T>,
    private val icon: Icon = RandomnessIcons.Data.Settings
) : QuickSwitchSchemeAction(true) {
    /**
     * The name of the action.
     */
    abstract val name: String


    /**
     * Sets the title and icon of this action.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.text = name
        event.presentation.icon = icon
    }

    /**
     * Adds actions for all schemes in `settings` to the given group.
     *
     * @param project ignored
     * @param group the group to add actions to
     * @param dataContext ignored
     */
    override fun fillActions(project: Project?, group: DefaultActionGroup, dataContext: DataContext) {
        val current = settings.currentScheme

        settings.schemes.forEach { scheme ->
            val icon = if (scheme === current) AllIcons.Actions.Forward else ourNotCurrentAction

            group.add(object : DumbAwareAction(scheme.myName, "", icon) {
                override fun actionPerformed(event: AnActionEvent) {
                    settings.currentSchemeName = scheme.name
                }
            })
        }
    }
}
