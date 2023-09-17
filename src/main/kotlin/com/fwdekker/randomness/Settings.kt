package com.fwdekker.randomness

import com.fwdekker.randomness.template.Template
import com.fwdekker.randomness.template.TemplateList
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.openapi.components.State as JBState


/**
 * Contains references to various [State] objects.
 *
 * @property templateList The template list.
 */
data class Settings(var templateList: TemplateList = TemplateList()) : State() {
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
        val DEFAULT: Settings by lazy { PersistentSettings.default.state }
    }
}

/**
 * The actual user's actual stored actually-serialized settings (actually).
 */
@JBState(
    name = "com.fwdekker.randomness.PersistentSettings",
    storages = [Storage("\$APP_CONFIG\$/randomness-beta.xml")],
)
class PersistentSettings : PersistentStateComponent<Settings> {
    private val settings = Settings()


    /**
     * Returns the template list.
     */
    override fun getState() = settings

    /**
     * Copies [settings] into `this`.
     *
     * @see TemplateList.copyFrom
     */
    override fun loadState(settings: Settings) = this.settings.copyFrom(settings)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The persistent instance.
         */
        val default: PersistentSettings get() = service()
    }
}
