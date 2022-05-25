package com.fwdekker.randomness

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil
import com.intellij.util.IconUtil
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.image.RGBImageFilter
import javax.swing.Icon
import kotlin.math.atan2


/**
 * Basic Randomness icons.
 */
object RandomnessIcons {
    /**
     * The main icon of Randomness.
     */
    val RANDOMNESS = IconLoader.findIcon("/icons/randomness.svg", this.javaClass.classLoader)!!

    /**
     * The template icon for template icons.
     */
    val TEMPLATE = IconLoader.findIcon("/icons/template.svg", this.javaClass.classLoader)!!

    /**
     * The template icon for scheme icons.
     */
    val SCHEME = IconLoader.findIcon("/icons/scheme.svg", this.javaClass.classLoader)!!

    /**
     * An icon for settings.
     */
    val SETTINGS = IconLoader.findIcon("/icons/settings.svg", this.javaClass.classLoader)!!

    /**
     * A filled-in version of [SETTINGS].
     */
    val SETTINGS_FILLED = IconLoader.findIcon("/icons/settings-filled.svg", this.javaClass.classLoader)!!

    /**
     * An icon for arrays.
     */
    val ARRAY = IconLoader.findIcon("/icons/array.svg", this.javaClass.classLoader)!!

    /**
     * A filled-in version of [ARRAY].
     */
    val ARRAY_FILLED = IconLoader.findIcon("/icons/array-filled.svg", this.javaClass.classLoader)!!

    /**
     * An icon for references.
     */
    val REFERENCE = IconLoader.findIcon("/icons/reference.svg", this.javaClass.classLoader)!!

    /**
     * A filled-in version of [REFERENCE].
     */
    val REFERENCE_FILLED = IconLoader.findIcon("/icons/reference-filled.svg", this.javaClass.classLoader)!!

    /**
     * An icon for repeated insertions.
     */
    val REPEAT = IconLoader.findIcon("/icons/repeat.svg", this.javaClass.classLoader)!!

    /**
     * A filled-in version of [REPEAT].
     */
    val REPEAT_FILLED = IconLoader.findIcon("/icons/repeat-filled.svg", this.javaClass.classLoader)!!
}


/**
 * A colored icon with some text in it.
 *
 * @property base The underlying icon which should be given color; must be square.
 * @property text The text to display inside the [base].
 * @property colors The colors to give to the [base].
 */
data class TypeIcon(val base: Icon, val text: String, val colors: List<Color>) : Icon {
    init {
        require(colors.isNotEmpty()) { "At least one color must be defined." }
    }


    /**
     * Returns an icon that describes both this icon's type and [other]'s type.
     *
     * @param other the icon to combine this icon with
     * @return an icon that describes both this icon's type and [other]'s type
     */
    fun combineWith(other: TypeIcon) =
        TypeIcon(
            RandomnessIcons.TEMPLATE,
            if (this.text == other.text) this.text else "",
            this.colors + other.colors
        )


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

        val filter = RadialColorReplacementFilter(colors, Pair(iconWidth / 2, iconHeight / 2))
        IconUtil.filterIcon(base, { filter }, c).paintIcon(c, g, x, y)

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
 * @property background The background shape to ensure that the small margin of background color is also applied inside
 * the [base], or `null` if [base] is already a solid shape.
 */
data class OverlayIcon(val base: Icon, val background: Icon? = null) : Icon {
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

        IconUtil.filterIcon(background ?: base, { RadialColorReplacementFilter(listOf(c.background)) }, c)
            .paintIcon(c, g, x, y)
        IconUtil.scale(base, c, 1 - 2 * MARGIN)
            .paintIcon(c, g, x + (MARGIN * iconWidth).toInt(), y + (MARGIN * iconHeight).toInt())
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
        const val MARGIN = 4f / 32


        /**
         * Overlay icon for arrays.
         */
        val ARRAY by lazy { OverlayIcon(RandomnessIcons.ARRAY, RandomnessIcons.ARRAY_FILLED) }

        /**
         * Overlay icon for template references.
         */
        val REFERENCE by lazy { OverlayIcon(RandomnessIcons.REFERENCE, RandomnessIcons.REFERENCE_FILLED) }

        /**
         * Overlay icon for repeated insertion.
         */
        val REPEAT by lazy { OverlayIcon(RandomnessIcons.REPEAT, RandomnessIcons.REPEAT_FILLED) }

        /**
         * Overlay icon for settings.
         */
        val SETTINGS by lazy { OverlayIcon(RandomnessIcons.SETTINGS, RandomnessIcons.SETTINGS_FILLED) }
    }
}

/**
 * An icon with various icons displayed on top of it as overlays.
 *
 * @property base
 * @property overlays
 */
data class OverlayedIcon(val base: Icon, val overlays: List<Icon> = emptyList()) : Icon {
    init {
        require(base.iconWidth == base.iconHeight) { Bundle("icons.error.base_square") }
        require(overlays.all { it.iconWidth == it.iconHeight }) { Bundle("icons.error.overlay_square") }
        require(overlays.map { it.iconWidth }.toSet().size <= 1) { Bundle("icons.error.overlay_same_size") }
    }


    /**
     * Returns a copy of this icon that has [icon] as an additional overlay icon.
     *
     * @param icon the additional overlay icon
     * @return a copy of this icon that has [icon] as an additional overlay icon
     */
    fun plusOverlay(icon: Icon) = copy(overlays = overlays + icon)


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
            val overlayX = (i % OVERLAYS_PER_ROW * overlaySize).toInt()
            val overlayY = (i / OVERLAYS_PER_ROW * overlaySize).toInt()

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
 * Replaces all colors with one of [colors] depending on the angle relative to [center].
 *
 * @property colors The colors that should be used, in clockwise order starting north-west.
 * @property center The center relative to which colors should be calculated; not required if only one color is given.
 */
class RadialColorReplacementFilter(
    private val colors: List<Color>,
    private val center: Pair<Int, Int>? = null
) : RGBImageFilter() {
    init {
        require(colors.isNotEmpty()) { Bundle("icons.error.one_colour") }
        require(colors.size == 1 || center != null) { Bundle("icons.error.center_undefined") }
    }


    /**
     * Returns the color to be displayed at ([x], [y]), considering the coordinates relative to the [center] and the
     * relative alpha of the encountered color.
     *
     * @param x the X coordinate of the pixel
     * @param y the Y coordinate of the pixel
     * @param rgb `0` if and only if the pixel's color should be replaced
     * @return `0` if [rgb] is `0`, or one of [colors] with its alpha shifted by [rgb]'s alpha otherwise
     */
    override fun filterRGB(x: Int, y: Int, rgb: Int) =
        if (rgb == 0) 0
        else if (center == null || colors.size == 1) shiftAlpha(colors[0], Color(rgb, true)).rgb
        else shiftAlpha(positionToColor(Pair(x - center.first, y - center.second)), Color(rgb, true)).rgb


    /**
     * Returns [toShift] which has its alpha multiplied by that of [shiftBy].
     *
     * @param toShift the color of which to shift the alpha
     * @param shiftBy the color which has the alpha to shift by
     * @return [toShift] which has its alpha multiplied by that of [shiftBy]
     */
    private fun shiftAlpha(toShift: Color, shiftBy: Color) =
        ColorUtil.withAlpha(toShift, asFraction(toShift.alpha) * asFraction(shiftBy.alpha))

    /**
     * Represents an integer in the range `[0, 256)` to a fraction of that range.
     *
     * @param number the number to represent as a fraction
     * @return number as a fraction
     */
    private fun asFraction(number: Int) = number / COMPONENT_MAX.toDouble()

    /**
     * Converts an offset to the [center] to a color in [colors].
     *
     * @param offset the offset to get the color for
     * @return the color to be displayed at [offset]
     */
    private fun positionToColor(offset: Pair<Int, Int>): Color {
        val angle = 2 * Math.PI - (atan2(offset.second.toDouble(), offset.first.toDouble()) + STARTING_ANGLE)
        val index = angle / (2 * Math.PI / colors.size)
        return colors[Math.floorMod(index.toInt(), colors.size)]
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Maximum value for an RGB component.
         */
        const val COMPONENT_MAX = 255

        /**
         * The angle in radians at which the first color should start being displayed.
         */
        const val STARTING_ANGLE = -(3 * Math.PI / 4)
    }
}
