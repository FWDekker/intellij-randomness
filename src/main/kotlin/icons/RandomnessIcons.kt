package icons

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil
import com.intellij.util.IconUtil
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.image.RGBImageFilter
import javax.swing.Icon


/**
 * Basic Randomness icons.
 */
object RandomnessIcons {
    /**
     * The main icon of Randomness.
     */
    val randomness = IconLoader.findIcon("/icons/randomness.svg")!!

    /**
     * The template icon for template icons.
     */
    // TODO: Rename files
    // TODO: Make base colour WHITE
    val basicTemplate = IconLoader.findIcon("/icons/unknown-template.svg")!!

    /**
     * The template icon for scheme icons.
     */
    val basicScheme = IconLoader.findIcon("/icons/unknown-scheme.svg")!!

    /**
     * The icon for templates of mixed types.
     */
    val mixedTemplate = IconLoader.findIcon("/icons/mixed-template.svg")!!

    /**
     * The icon for schemes of mixed types.
     */
    val mixedScheme = IconLoader.findIcon("/icons/mixed-scheme.svg")!!

    /**
     * An icon for settings in general.
     */
    val settings = IconLoader.findIcon("/icons/settings.svg")!!

    /**
     * An icon for arrays.
     */
    val array = IconLoader.findIcon("/icons/array.svg")!!

    /**
     * An icon for references.
     */
    val reference = IconLoader.findIcon("/icons/reference.svg")!!

    /**
     * An icon for repeated insertions.
     */
    val repeat = IconLoader.findIcon("/icons/repeat.svg")!!
}


/**
 * A colored icon with some text in it.
 *
 * @property base The underlying icon which should be given color; must be square.
 * @property text The text to display inside the [base].
 * @property color The color to give to the [base].
 */
data class TypeIcon(val base: Icon, val text: String, val color: Color) : Icon {
    init {
        require(iconWidth == iconHeight) { "Base image must be square." }
    }


    /**
     * Returns an icon that describes both this icon's type and [other]'s type.
     *
     * @param other the icon to combine this icon with
     * @return an icon that describes both this icon's type and [other]'s type
     */
    fun combineWith(other: TypeIcon) =
        if (this == other)
            copy(base = RandomnessIcons.basicTemplate)
        else if (this.text == other.text && this.color == other.color)
            TypeIcon(RandomnessIcons.basicTemplate, this.text, this.color)
        else
            MIXED_TEMPLATE


    /**
     * Paints the colored text icon.
     *
     * @param c a [Component] to get properties useful for painting
     * @param g the graphics context
     * @param x the X coordinate of the icon's top-left corner
     * @param y the Y coordinate of the icon's top-left corner
     */
    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        if (c == null || g == null) return

        IconUtil.filterIcon(base, { ColorReplacementFilter(color) }, c).paintIcon(c, g, x, y)

        val textIcon = IconUtil.textToIcon(text, c, FONT_SIZE * iconWidth)
        textIcon.paintIcon(c, g, x + (iconWidth - textIcon.iconWidth) / 2, y + (iconHeight - textIcon.iconHeight) / 2)
    }

    /**
     * The width of the base icon.
     */
    override fun getIconWidth() = base.iconWidth

    /**
     * The height of the base icon.
     */
    override fun getIconHeight() = base.iconHeight


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The icon displayed for a template of mixed type.
         */
        val MIXED_TEMPLATE = TypeIcon(RandomnessIcons.basicTemplate, "", Color.BLACK)

        /**
         * The scale of the text inside the icon relative to the icon's size.
         */
        const val FONT_SIZE = 12f / 32f
    }
}

/**
 * An overlay icon, which can be displayed on top of other icons.
 *
 * This icon is drawn as the [base] surrounded by a small margin of background color, which creates visual distance
 * between the overlay and the rest of the icon this overlay is shown in top of. The background color is determined when
 * the icon is drawn.
 *
 * @property base The base of the icon; must be square.
 */
data class OverlayIcon(val base: Icon) : Icon {
    init {
        require(iconWidth == iconHeight) { "Base image must be square." }
    }


    /**
     * Paints the overlay icon.
     *
     * @param c a [Component] to get properties useful for painting
     * @param g the graphics context
     * @param x the X coordinate of the icon's top-left corner
     * @param y the Y coordinate of the icon's top-left corner
     */
    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        if (c == null || g == null) return

        IconUtil.filterIcon(base, { ColorReplacementFilter(c.background) }, null)
            .paintIcon(c, g, x, y)
        IconUtil.scale(base, null, (iconWidth - 2 * MARGIN) / iconWidth)
            .paintIcon(c, g, (x + MARGIN).toInt(), (y + MARGIN).toInt())
    }

    /**
     * The width of the base icon.
     */
    override fun getIconWidth() = base.iconWidth

    /**
     * The height of the base icon.
     */
    override fun getIconHeight() = base.iconHeight


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The margin around the base image that is filled with background color.
         *
         * This number is a fraction relative to the base image's size.
         */
        const val MARGIN = 2f / 32
    }
}

/**
 * An icon with various icons displayed on top of it as overlays.
 *
 * @property base
 * @property overlays
 */
data class OverlayedIcon(val base: Icon, val overlays: List<Icon>) : Icon {
    init {
        require(iconWidth == iconHeight) { "Base icon must be square." }
        require(overlays.all { it.iconWidth == it.iconHeight }) { "Overlays must be square." }
        require(overlays.map { it.iconWidth }.toSet().size <= 1) { "All overlays must have same size." }
    }


    /**
     * Paints the scheme icon.
     *
     * @param c a [Component] to get properties useful for painting
     * @param g the graphics context
     * @param x the X coordinate of the icon's top-left corner
     * @param y the Y coordinate of the icon's top-left corner
     */
    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        if (c == null || g == null) return

        base.paintIcon(c, g, x, y)
        overlays.forEachIndexed { i, overlay ->
            val overlaySize = iconWidth.toFloat() / OVERLAYS_PER_ROW
            val overlayX = (i % 2 * overlaySize).toInt()
            val overlayY = (i / 2 * overlaySize).toInt()

            IconUtil.scale(overlay, null, overlaySize / overlay.iconWidth).paintIcon(c, g, overlayX, overlayY)
        }
    }

    /**
     * The width of the base icon.
     */
    override fun getIconWidth() = base.iconWidth

    /**
     * The height of the base icon.
     */
    override fun getIconHeight() = base.iconHeight


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Number of overlays displayed per row.
         */
        const val OVERLAYS_PER_ROW = 2
    }
}


/**
 * Replaces all non-empty pixels with [color].
 *
 * @property color The color to replace all non-empty pixels with.
 */
class ColorReplacementFilter(private val color: Color) : RGBImageFilter() {
    /**
     * Returns 0 if [rgb] is 0, or returns [color] with its alpha scaled relative to that of [rgb] otherwise.
     *
     * @param x ignored
     * @param y ignored
     * @param rgb 0 if and only if this pixel has a color
     * @return 0 if [rgb] is 0, or returns [color] with its alpha scaled relative to that of [rgb] otherwise
     */
    override fun filterRGB(x: Int, y: Int, rgb: Int): Int {
        return if (rgb == 0) 0
        else ColorUtil.withAlpha(color, asFraction(Color(rgb, true).alpha) * asFraction(color.alpha)).rgb
    }


    /**
     * Represents an integer in the range [0, 256) to a fraction of that range.
     *
     * @param number the number to represent as a fraction
     * @return number as a fraction
     */
    @Suppress("MagicNumber") // 255 is not magic
    private fun asFraction(number: Int) = number / 255.0
}
