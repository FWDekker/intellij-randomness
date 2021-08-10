package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.XCollection
import icons.RandomnessIcons


/**
 * Generates random data by concatenating the random outputs of a list of [Scheme]s.
 *
 * @property name The unique name of the template.
 * @property schemes The ordered list of underlying schemes.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class Template(
    override var name: String = DEFAULT_NAME,
    @get:XCollection(
        elementTypes = [
            IntegerScheme::class,
            DecimalScheme::class,
            StringScheme::class,
            WordScheme::class,
            UuidScheme::class,
            LiteralScheme::class
        ]
    )
    var schemes: List<Scheme> = DEFAULT_SCHEMES,
    override var decorator: Nothing? = null
) : Scheme() {
    override val icons: RandomnessIcons
        get() = schemes.singleOrNull()?.icons ?: RandomnessIcons.Data


    override fun generateUndecoratedStrings(count: Int) =
        schemes.onEach { it.random = random }.map { it.generateStrings(count) }
            .let { data -> (0 until count).map { i -> data.joinToString(separator = "") { it[i] } } }

    override fun doValidate() =
        schemes.firstNotNullOfOrNull { scheme -> scheme.doValidate()?.let { "${scheme::class.simpleName} > $it" } }


    override fun deepCopy() =
        Template(
            name = name,
            schemes = schemes.map { it.deepCopy() }
        )


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [name] field.
         */
        const val DEFAULT_NAME = "Unnamed template"

        /**
         * The default value of the [schemes] field.
         */
        val DEFAULT_SCHEMES: List<Scheme>
            get() = listOf(IntegerScheme())
    }
}


/**
 * A collection of different templates.
 *
 * @property templates the collection of templates, each with a unique [Template.name]
 * @see TemplateSettings
 */
data class TemplateList(
    @MapAnnotation(sortBeforeSave = false)
    var templates: List<Template> = DEFAULT_TEMPLATES
) : Scheme() {
    override val decorator: Nothing? = null
    override val name = "TemplateList"


    override fun generateUndecoratedStrings(count: Int) =
        templates.first().also { it.random = random }.generateStrings(count)


    override fun doValidate(): String? {
        val duplicate =
            templates.firstOrNull { templates.indexOf(it) != templates.lastIndexOf(it) }?.name
        val invalid =
            templates.firstNotNullOfOrNull { template -> template.doValidate()?.let { "${template.name} > $it" } }

        return when {
            duplicate != null -> "There are multiple templates with the name '$duplicate'."
            invalid != null -> invalid
            else -> null
        }
    }


    override fun deepCopy() = TemplateList(templates.map { it.deepCopy() })


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_TEMPLATES: List<Template>
            get() = listOf(
                Template("Integer", listOf(IntegerScheme())),
                Template("Decimal", listOf(DecimalScheme())),
                Template("String", listOf(StringScheme())),
                Template("Word", listOf(WordScheme())),
                Template("UUID", listOf(UuidScheme()))
            )
    }
}
