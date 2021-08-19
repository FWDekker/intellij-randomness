package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.fwdekker.randomness.array.ArraySchemeDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import icons.RandomnessIcons
import java.awt.BorderLayout
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.random.Random


/**
 * Dummy implementation of [Scheme].
 *
 * @property literals The outputs to cyclically produce.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class DummyScheme(
    var literals: List<String> = listOf(DEFAULT_OUTPUT),
    override var decorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme() {
    override var icons: RandomnessIcons? = RandomnessIcons.Data
    override var name = "Dummy"


    override fun generateUndecoratedStrings(count: Int) = List(count) { literals[it % literals.size] }


    override fun doValidate() =
        if (literals[0] == INVALID_OUTPUT) "Invalid input!"
        else null

    override fun deepCopy() = copy(decorator = decorator.deepCopy())


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default singular value contained in [literals].
         */
        const val DEFAULT_OUTPUT = "literal"

        /**
         * The magic string contained in [literals] to make the scheme invalid according to [doValidate].
         */
        const val INVALID_OUTPUT = "invalid"


        /**
         * Convenience method for creating a [DummyScheme] using vararg notation.
         *
         * @param literals the literals for the dummy scheme
         * @return a [DummyScheme] with the given literals
         */
        fun from(vararg literals: String) = DummyScheme(literals = literals.toList())
    }
}

/**
 * Dummy implementation of [StateEditor] for a [DummyScheme].
 *
 * Dummy schemes have a producer, but this editor only supports returning a producer of a single string.
 *
 * @param scheme the scheme to edit in the component
 */
class DummySchemeEditor(scheme: DummyScheme = DummyScheme()) : StateEditor<DummyScheme>(scheme) {
    override val rootComponent = JPanel(BorderLayout())

    private val literalsInput = JTextField()
        .also { it.name = "literals" }
        .also { rootComponent.add(it, BorderLayout.NORTH) }
    private val arrayDecoratorEditor = ArraySchemeDecoratorEditor(originalState.decorator)
        .also { rootComponent.add(it.rootComponent, BorderLayout.SOUTH) }


    init {
        loadState()
    }


    override fun loadState(state: DummyScheme) {
        super.loadState(state)

        literalsInput.text = state.literals.joinToString(separator = ",")
        arrayDecoratorEditor.loadState(state.decorator)
    }

    override fun readState() =
        DummyScheme(
            literals = literalsInput.text.split(','),
            decorator = arrayDecoratorEditor.readState()
        )


    override fun addChangeListener(listener: () -> Unit) {
        addChangeListenerTo(literalsInput, listener = listener)
        arrayDecoratorEditor.addChangeListener(listener)
    }
}


/**
 * Dummy implementation of [SettingsConfigurable].
 */
class DummySettingsConfigurable : SettingsConfigurable() {
    override fun getDisplayName() = "Dummy"

    override fun createEditor() = DummySchemeEditor()
}

/**
 * Inserts a dummy value.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @property dummySupplier Generates dummy values to insert.
 * @param repeat true if and only if the same value should be inserted at each caret
 */
class DummyInsertAction(repeat: Boolean = false, private val dummySupplier: (Random) -> String) : InsertAction(repeat) {
    override val icon: Icon? = null

    override val name = "Random Dummy"


    override fun generateStrings(count: Int) = List(count) { dummySupplier(Random.Default) }
}
