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
object Icons {
    /**
     * The main icon of Randomness.
     */
    @JvmField
    val RANDOMNESS = IconLoader.getIcon("/icons/randomness.svg", javaClass)

    /**
     * The template icon for template icons.
     */
    @JvmField
    val TEMPLATE = IconLoader.getIcon("/icons/template.svg", javaClass)

    /**
     * The template icon for scheme icons.
     */
    @JvmField
    val SCHEME = IconLoader.getIcon("/icons/scheme.svg", javaClass)

    /**
     * An icon for settings.
     */
    @JvmField
    val SETTINGS = IconLoader.getIcon("/icons/settings.svg", javaClass)

    /**
     * A filled-in version of [SETTINGS].
     */
    @JvmField
    val SETTINGS_FILLED = IconLoader.getIcon("/icons/settings-filled.svg", javaClass)

    /**
     * An icon for arrays.
     */
    @JvmField
    val ARRAY = IconLoader.getIcon("/icons/array.svg", javaClass)

    /**
     * A filled-in version of [ARRAY].
     */
    @JvmField
    val ARRAY_FILLED = IconLoader.getIcon("/icons/array-filled.svg", javaClass)

    /**
     * An icon for references.
     */
    @JvmField
    val REFERENCE = IconLoader.getIcon("/icons/reference.svg", javaClass)

    /**
     * A filled-in version of [REFERENCE].
     */
    @JvmField
    val REFERENCE_FILLED = IconLoader.getIcon("/icons/reference-filled.svg", javaClass)

    /**
     * An icon for repeated insertions.
     */
    @JvmField
    val REPEAT = IconLoader.getIcon("/icons/repeat.svg", javaClass)

    /**
     * A filled-in version of [REPEAT].
     */
    @JvmField
    val REPEAT_FILLED = IconLoader.getIcon("/icons/repeat-filled.svg", javaClass)
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


        /**
         * Returns a single icon that describes all [icons], or `null` if [icons] is empty.
         */
        fun combine(icons: Collection<TypeIcon>): TypeIcon? =
            if (icons.isEmpty()) null
            else TypeIcon(
                Icons.TEMPLATE,
                if (icons.map { it.text }.toSet().size == 1) icons.first().text else "",
                icons.flatMap { it.colors }
            )
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
        val ARRAY get() = OverlayIcon(Icons.ARRAY, Icons.ARRAY_FILLED)

        /**
         * Overlay icon for template references.
         */
        val REFERENCE get() = OverlayIcon(Icons.REFERENCE, Icons.REFERENCE_FILLED)

        /**
         * Overlay icon for repeated insertion.
         */
        val REPEAT get() = OverlayIcon(Icons.REPEAT, Icons.REPEAT_FILLED)

        /**
         * Overlay icon for settings.
         */
        val SETTINGS get() = OverlayIcon(Icons.SETTINGS, Icons.SETTINGS_FILLED)
    }
}

/**
 * An icon with various icons displayed on top of it as overlays.
 *
 * @property base The underlying base icon.
 * @property overlays The various icons that are overlayed on top of [base].
 */
data class OverlayedIcon(val base: Icon, val overlays: List<Icon> = emptyList()) : Icon {
    /**
     * @see validate
     */
    private var validated: Boolean = false


    /**
     * Returns a copy of this icon that has [icon] as an additional overlay icon.
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

        validate()

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
     * Lazily validates the relative sizes of the [base] and the [overlays].
     *
     * This code must *not* be called in the constructor; it should be deferred to some later point. Calling this in the
     * constructor *will* cause exceptions, because the constructor is called in the constructor of various actions, and
     * actions are constructor before UI scaling is initialized.
     *
     * See also https://youtrack.jetbrains.com/issue/IJPL-163887/
     */
    private fun validate() {
        if (validated) return

        require(base.iconWidth == base.iconHeight) { "Base must be square." }
        require(overlays.all { it.iconWidth == it.iconHeight }) { "All overlays must be square." }
        require(overlays.map { it.iconWidth }.toSet().size <= 1) { "All overlays must have same size." }

        validated = true
    }


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
 * @param colors the colors that should be used, in clockwise order starting north-west
 * @param center the center relative to which colors should be calculated; not required if only one color is given
 */
class RadialColorReplacementFilter(
    private val colors: List<Color>,
    private val center: Pair<Int, Int>? = null,
) : RGBImageFilter() {
    init {
        require(colors.isNotEmpty()) { "At least one color must be defined." }
        require(colors.size == 1 || center != null) { "Center must be defined if more than one color is given." }
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
    @Suppress("UseJBColor") // Filtering works the same in both themes
    override fun filterRGB(x: Int, y: Int, rgb: Int) =
        if (rgb == 0) 0
        else if (center == null || colors.size == 1) shiftAlpha(colors[0], Color(rgb, true)).rgb
        else shiftAlpha(positionToColor(Pair(x - center.first, y - center.second)), Color(rgb, true)).rgb


    /**
     * Returns [toShift] which has its alpha multiplied by that of [shiftBy].
     */
    private fun shiftAlpha(toShift: Color, shiftBy: Color) =
        ColorUtil.withAlpha(toShift, asFraction(toShift.alpha) * asFraction(shiftBy.alpha))

    /**
     * Represents a [number] in the range `[0, 256)` as a fraction of that range.
     */
    private fun asFraction(number: Int) = number / COMPONENT_MAX.toDouble()

    /**
     * Returns the appropriate color from [colors] for an [offset] relative to the [center].
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
