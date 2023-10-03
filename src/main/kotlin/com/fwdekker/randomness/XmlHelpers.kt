package com.fwdekker.randomness

import org.jdom.Element


/**
 * Returns a list of all [Element]s contained in this `Element`.
 */
fun Element.getElements(): List<Element> =
    content().toList().filterIsInstance<Element>()

/**
 * Returns the [Element] contained in this `Element` that has attribute `name="[name]"`, or `null` if no single such
 * `Element` exists.
 */
fun Element.getContentByName(name: String): Element? =
    getContent<Element> { (it as Element).getAttribute("name")?.value == name }.singleOrNull()

/**
 * Returns the value of the `value` attribute of the single [Element] contained in this `Element` that has attribute
 * `name="[name]"`, or `null` if no single such `Element` exists.
 */
fun Element.getAttributeValueByName(name: String): String? =
    getContentByName(name)?.getAttribute("value")?.value

/**
 * Sets the value of the `value` attribute of the single [Element] contained in this `Element` that has attribute
 * `name="[name]"` to [value], or does nothing if no single such `Element` exists.
 */
fun Element.setAttributeValueByName(name: String, value: String) {
    getContentByName(name)?.setAttribute("value", value)
}

/**
 * Returns the single [Element] that is contained in this `Element`, or `null` if this `Element` does not contain
 * exactly one `Element`.
 */
fun Element.getSingleContent(): Element? =
    content.singleOrNull() as? Element

/**
 * Traverses a path of [Element]s based on their [names] by monadically calling either [getContentByName] (if the name
 * is not `null`) or [getSingleContent] (if the name is `null`).
 */
fun Element.getContentByPath(vararg names: String?): Element? =
    names.fold(this as? Element) { acc, name ->
        if (name == null) acc?.getSingleContent()
        else acc?.getContentByName(name)
    }
