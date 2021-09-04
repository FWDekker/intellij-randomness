package icons

import com.intellij.openapi.util.IconLoader
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon


/**
 * All Randomness icons.
 */
object RandomnessIcons {
    /**
     * The main icon of Randomness.
     */
    val randomness = IconLoader.findIcon("/icons/randomness.svg")!!

    /**
     * The [TypeIcon] for mixed data types.
     */
    val mixed = TypeIcon(
        IconLoader.findIcon("/icons/mixed-template.svg")!!,
        IconLoader.findIcon("/icons/mixed-scheme.svg")!!
    )

    /**
     * The [TypeIcon] for unknown data types.
     */
    val unknown = TypeIcon(
        IconLoader.findIcon("/icons/unknown-template.svg")!!,
        IconLoader.findIcon("/icons/unknown-scheme.svg")!!
    )

    /**
     * The [TypeIcon] for integers.
     */
    val integer = TypeIcon(
        IconLoader.findIcon("/icons/integer-template.svg")!!,
        IconLoader.findIcon("/icons/integer-scheme.svg")!!
    )

    /**
     * The [TypeIcon] for decimals.
     */
    val decimal = TypeIcon(
        IconLoader.findIcon("/icons/decimal-template.svg")!!,
        IconLoader.findIcon("/icons/decimal-scheme.svg")!!
    )

    /**
     * The [TypeIcon] for strings.
     */
    val string = TypeIcon(
        IconLoader.findIcon("/icons/string-template.svg")!!,
        IconLoader.findIcon("/icons/string-scheme.svg")!!
    )

    /**
     * The [TypeIcon] for words.
     */
    val word = TypeIcon(
        IconLoader.findIcon("/icons/word-template.svg")!!,
        IconLoader.findIcon("/icons/word-scheme.svg")!!
    )

    /**
     * The [TypeIcon] for UUIDs.
     */
    val uuid = TypeIcon(
        IconLoader.findIcon("/icons/uuid-template.svg")!!,
        IconLoader.findIcon("/icons/uuid-scheme.svg")!!
    )

    /**
     * The [TypeIcon] for literals.
     */
    val literal = TypeIcon(
        IconLoader.findIcon("/icons/literal-template.svg")!!,
        IconLoader.findIcon("/icons/literal-scheme.svg")!!
    )

    /**
     * An icon to indicate Randomness settings in general.
     */
    val settings = IconLoader.findIcon("/icons/settings.svg")!!

    /**
     * An icon overlay for arrays.
     */
    val arrayOverlay = IconLoader.findIcon("/icons/array-overlay.svg")!!

    /**
     * An icon overlay for references.
     */
    val referenceOverlay = IconLoader.findIcon("/icons/reference-overlay.svg")!!

    /**
     * An icon overlay for repeated insertions.
     */
    val repeatOverlay = IconLoader.findIcon("/icons/repeat-overlay.svg")!!

    /**
     * An icon overlay for settings.
     */
    val settingsOverlay = IconLoader.findIcon("/icons/settings-overlay.svg")!!
}

/**
 * An icon to signify a type.
 *
 * Types can be displayed either as a template or as a (non-template) scheme. A type icon allows easily changing between
 * the two even if the currently active state is not known.
 *
 * @property templateIcon The icon to use when the type is expressed by a template.
 * @property schemeIcon The icon to use when the type is expressed as a non-template scheme.
 * @property active The icon currently used and painted; equals either [templateIcon] or [schemeIcon].
 */
data class TypeIcon(
    private val templateIcon: Icon,
    private val schemeIcon: Icon,
    private val active: Icon = templateIcon
) : Icon {
    /**
     * Returns the template variant of this [TypeIcon].
     */
    val template: TypeIcon by lazy { copy(active = templateIcon) }

    /**
     * Returns the non-template scheme variant of this [TypeIcon].
     */
    val scheme: TypeIcon by lazy { copy(active = schemeIcon) }


    /**
     * Paints the [active] icon.
     *
     * @param c the [Component] to get useful painting properties from
     * @param g the graphics context
     * @param x the X coordinate of the icon's top-left corner
     * @param y the Y coordinate of the icon's top-left corner
     */
    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) = active.paintIcon(c, g, x, y)

    /**
     * Returns the width of [active].
     *
     * @return the width of [active]
     */
    override fun getIconWidth() = active.iconWidth

    /**
     * Returns the height of [active].
     *
     * @return the height of [active]
     */
    override fun getIconHeight() = active.iconHeight
}
