package com.fwdekker.randomness

import com.fwdekker.randomness.Scheme.Companion.DEFAULT_NAME
import com.fwdekker.randomness.ui.JIntSpinner
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import javax.swing.JPanel


/**
 * Dummy implementation of [Settings].
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 */
data class DummySettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<DummyScheme> = DEFAULT_SCHEMES.toMutableList(),
    override var currentSchemeName: String = DEFAULT_NAME
) : Settings<DummySettings, DummyScheme> {
    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES
            get() = listOf(DummyScheme())
    }


    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: DummySettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * Dummy implementation of [Scheme].
 *
 * @property myName the name of the scheme
 * @property count a configurable value
 */
data class DummyScheme(
    override var myName: String = DEFAULT_NAME,
    var count: Int = DEFAULT_COUNT
) : Scheme<DummyScheme> {
    companion object {
        /**
         * The default value of the [count][count] field.
         */
        const val DEFAULT_COUNT = 3
    }


    override fun copyFrom(other: DummyScheme) {
        this.myName = other.myName
        this.count = other.count
    }

    override fun copyAs(name: String) = this.copy(myName = name)
}


/**
 * Dummy implementation of [SettingsComponent].
 *
 * @param settings the settings to edit in the component
 */
class DummySettingsComponent(settings: DummySettings = DummySettings()) :
    SettingsComponent<DummySettings, DummyScheme>(settings) {
    override val rootPane = JPanel()
    private val countSpinner = JIntSpinner(minValue = 0)
        .also { it.name = "count" }

    override val unsavedSettings = DummySettings()
    override val schemesPanel = DummySchemesPanel(unsavedSettings)
        .also { panel ->
            panel.addListener(object : SchemesPanel.Listener<DummyScheme> {
                override fun onCurrentSchemeWillChange(scheme: DummyScheme) = saveScheme(scheme)

                override fun onCurrentSchemeHasChanged(scheme: DummyScheme) = loadScheme(scheme)
            })
        }


    init {
        loadSettings()

        rootPane.add(countSpinner)
        rootPane.add(schemesPanel)
    }


    override fun loadScheme(scheme: DummyScheme) {
        countSpinner.value = scheme.count
    }

    override fun saveScheme(scheme: DummyScheme) {
        scheme.count = countSpinner.value
    }

    override fun doValidate() = countSpinner.validateValue()


    /**
     * Dummy implementation of [SchemesPanel].
     *
     * @param settings the settings to edit in the panel
     */
    class DummySchemesPanel(settings: DummySettings) : SchemesPanel<DummyScheme>(settings) {
        override val type: Class<DummyScheme>
            get() = DummyScheme::class.java

        override fun createDefaultInstance() = DummyScheme()
    }
}


/**
 * Dummy implementation of [SettingsConfigurable].
 */
class DummySettingsConfigurable(override val component: DummySettingsComponent = DummySettingsComponent()) :
    SettingsConfigurable<DummySettings, DummyScheme>() {
    override fun getDisplayName() = "Dummy"
}
