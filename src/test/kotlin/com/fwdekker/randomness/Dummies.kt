package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.addChangeListenerTo
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.random.Random


/**
 * Dummy implementation of [Scheme].
 *
 * @property literals The outputs to cyclically produce.
 * @property decorators Settings that determine whether the output should be decorated.
 */
data class DummyScheme(
    var literals: List<String> = listOf(DEFAULT_OUTPUT),
    override var decorators: List<DecoratorScheme> = listOf(ArrayDecorator()),
) : Scheme() {
    override var typeIcon = TypeIcon(Icons.SCHEME, "dum", listOf(Color.GRAY))
    override val name get() = literals.joinToString()

    /**
     * Returns the single [ArrayDecorator] in [decorators].
     *
     * Use this field only if the test assumes that this scheme has a single decorator the entire time.
     */
    var arrayDecorator: ArrayDecorator
        get() = decorators.single() as ArrayDecorator
        set(value) {
            decorators = listOf(value)
        }


    override fun generateUndecoratedStrings(count: Int) = List(count) { literals[it % literals.size] }


    override fun doValidate() =
        if (literals[0] == INVALID_OUTPUT) "Invalid input!"
        else decorators.firstNotNullOfOrNull { it.doValidate() }

    override fun deepCopy(retainUuid: Boolean) =
        copy(decorators = decorators.map { it.deepCopy(retainUuid) })
            .also { if (retainUuid) it.uuid = this.uuid }


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
         * @return a [DummyScheme] with [literals]
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
    // TODO: Modernize this dummy implementation
    override val rootComponent = JPanel(BorderLayout())

    private val literalsInput = JTextField()
        .also { it.name = "literals" }
        .also { rootComponent.add(it, BorderLayout.NORTH) }
    private val arrayDecoratorEditor = ArrayDecoratorEditor(originalState.arrayDecorator)
        .also { rootComponent.add(it.rootComponent, BorderLayout.SOUTH) }


    init {
        loadState()
    }


    override fun loadState(state: DummyScheme) {
        super.loadState(state)

        literalsInput.text = state.literals.joinToString(",")
        arrayDecoratorEditor.loadState(state.arrayDecorator)
    }

    override fun readState() =
        DummyScheme(
            literals = literalsInput.text.split(','),
            decorators = listOf(arrayDecoratorEditor.readState())
        )


    override fun addChangeListener(listener: () -> Unit) {
        addChangeListenerTo(literalsInput, listener = listener)
        arrayDecoratorEditor.addChangeListener(listener)
    }
}


/**
 * Dummy implementation of [StateConfigurable].
 */
class DummyStateConfigurable : StateConfigurable() {
    override fun getDisplayName() = "Dummy"

    override fun createEditor() = DummySchemeEditor()
}

/**
 * Inserts a dummy value.
 *
 * Mostly for testing and demonstration purposes.
 *
 * @param repeat `true` if and only if the same value should be inserted at each caret
 * @property dummySupplier Generates dummy values to insert.
 */
class DummyInsertAction(repeat: Boolean = false, private val dummySupplier: (Random) -> String) :
    InsertAction(repeat, "Random Dummy", null, null) {
    override fun generateStrings(count: Int) = List(count) { dummySupplier(Random.Default) }
}
