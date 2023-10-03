package com.fwdekker.randomness

import com.fwdekker.randomness.PersistentSettings.Companion.CURRENT_VERSION
import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.template.TemplateList
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.util.xmlb.annotations.Transient
import org.jdom.Element
import java.lang.module.ModuleDescriptor
import com.intellij.openapi.components.State as JBState


/**
 * Contains references to various [State] objects.
 *
 * @property version The version of Randomness with which these settings were created.
 * @property templateList The template list.
 */
data class Settings(
    var version: String = CURRENT_VERSION,
    var templateList: TemplateList = TemplateList(),
) : State() {
    /**
     * @see TemplateList.templates
     */
    @get:Transient
    val templates: MutableList<Template> get() = templateList.templates


    override fun doValidate() = templateList.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(templateList = templateList.deepCopy(retainUuid = retainUuid)).deepCopyTransient(retainUuid)

    override fun copyFrom(other: State) {
        require(other is Settings) { "Cannot copy from different type." }

        this.templateList.copyFrom(other.templateList)
        copyFromTransient(other)
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The persistent [Settings] instance.
         */
        val DEFAULT: Settings by lazy { service<PersistentSettings>().settings }
    }
}

/**
 * The persistent [Settings] instance, stored as an [Element] to allow custom conversion for backwards compatibility.
 *
 * @see Settings.DEFAULT Preferred method of accessing the persistent settings instance.
 */
@JBState(
    name = "Randomness",
    storages = [
        Storage("randomness.xml", deprecated = true),
        Storage("randomness-beta.xml", exportable = true),
    ],
    category = SettingsCategory.PLUGINS,
)
class PersistentSettings : PersistentStateComponent<Element> {
    /**
     * The persistent settings instance.
     *
     * @see Settings.DEFAULT Preferred method of accessing the persistent settings instance.
     */
    val settings = Settings()


    /**
     * Returns the [settings] as an [Element].
     */
    override fun getState(): Element = XmlSerializer.serialize(settings)

    /**
     * Deserializes [element] into a [Settings] instance, which is then copied into the [settings] instance.
     *
     * @see TemplateList.copyFrom
     */
    override fun loadState(element: Element) =
        settings.copyFrom(XmlSerializer.deserialize(upgrade(element), Settings::class.java))


    /**
     * Silently upgrades the format of the settings contained in [element] to the format of the latest version.
     */
    private fun upgrade(element: Element): Element {
        val elementVersion = element.getAttributeValueByName("version")?.let { ModuleDescriptor.Version.parse(it) }

        when {
            elementVersion == null -> Unit

            // Placeholder to show how an upgrade might work. Remove this once an actual upgrade has been added.
            elementVersion < ModuleDescriptor.Version.parse("0.0.0-placeholder") ->
                element.getContentByPath("templateList", null, "templates", null)?.getElements()
                    ?.forEachIndexed { idx, template -> template.setAttributeValueByName("name", "Template$idx") }
        }

        element.setAttributeValueByName("version", CURRENT_VERSION)
        return element
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The currently-running version of Randomness.
         */
        const val CURRENT_VERSION: String = "3.0.0-beta.3"
    }
}
