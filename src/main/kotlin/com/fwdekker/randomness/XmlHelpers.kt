package com.fwdekker.randomness

import org.jdom.Element


/**
 * Adds a property with given [name] and [value].
 */
fun Element.addProperty(name: String, value: String? = null) {
    Element("option")
        .also {
            it.setAttribute("name", name)
            if (value != null) it.setAttribute("value", value)
        }
        .also(::addContent)
}

/**
 * Returns the single child with attribute `name="[name]"`, or `null` if there is not exactly one such child.
 */
fun Element.getProperty(name: String): Element? =
    getMultiProperty(name).singleOrNull()

/**
 * Returns all children with attribute `name="[name]"`.
 */
fun Element.getMultiProperty(name: String): List<Element> =
    children.filter { it.getAttribute("name")?.value == name }

/**
 * Traverses a path of [Element]s based on their [names] by monadically either calling [getProperty] (if the name is not
 * `null`) or taking the single child (if the name is `null`).
 */
fun Element.getPropertyByPath(vararg names: String?): Element? =
    names.fold(this as? Element) { acc, name ->
        if (name == null) acc?.children?.singleOrNull()
        else acc?.getProperty(name)
    }

/**
 * Returns the value of the `value` attribute of property [name], or `null` if the property does not exist or if the
 * property has no value.
 */
fun Element.getPropertyValue(name: String): String? =
    getProperty(name)?.getAttribute("value")?.value

/**
 * Sets the value of the `value` attribute of property [name], or adds the property if it does not exist.
 */
fun Element.setPropertyValue(name: String, value: String) {
    getMultiProperty(name)
        .also {
            if (it.isEmpty()) addProperty(name, value)
            else if (it.size == 1) it[0].setAttribute("value", value)
        }
}

/**
 * Renames the property from [oldName] to [newName].
 *
 * Requires that there is not already a property with [newName]. If there is no property with [oldName], nothing
 * happens. If there are multiple properties with [oldName], they are all renamed to [newName].
 */
fun Element.renameProperty(oldName: String, newName: String) {
    require(getMultiProperty(newName).isEmpty()) { "Attribute '$newName' is already in use." }

    getMultiProperty(oldName).forEach { it.setAttribute("name", newName) }
}


/**
 * Assuming this is the [Element] representation of a [Settings] instance, returns the [Element] of the contained
 * [com.fwdekker.randomness.template.TemplateList], or `null` if no such [Element] could be found.
 */
fun Element.getTemplateList(): Element? =
    getPropertyByPath("templateList", null)

/**
 * Assuming this is the [Element] representation of a [Settings] instance, returns the list of [Element]s of the
 * contained [com.fwdekker.randomness.template.Template]s.
 */
fun Element.getTemplates(): List<Element> =
    getPropertyByPath("templateList", null, "templates", null)?.children ?: emptyList()

/**
 * Assuming this is the [Element] representation of a [Settings] instance, returns the list of [Element]s of the
 * contained [com.fwdekker.randomness.Scheme]s.
 */
fun Element.getSchemes(): List<Element> =
    getTemplates().mapNotNull { it.getProperty("schemes") }.flatMap { it.children }
