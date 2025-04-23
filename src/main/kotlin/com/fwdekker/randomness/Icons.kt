package com.fwdekker.randomness

import com.fwdekker.randomness.Icons.ARRAY
import com.fwdekker.randomness.Icons.REFERENCE
import com.fwdekker.randomness.Icons.REPEAT
import com.fwdekker.randomness.Icons.SETTINGS
import com.fwdekker.randomness.TypeIcon.Companion.combine
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil
import com.intellij.ui.LayeredIcon
import com.intellij.ui.RowIcon
import com.intellij.ui.components.JBLabel
import com.intellij.ui.icons.FilteredIcon
import com.intellij.ui.icons.RgbImageFilterSupplier
import com.intellij.ui.icons.RowIcon.Alignment
import com.intellij.ui.icons.TextIcon
import com.intellij.util.IconUtil
import java.awt.Color
import java.awt.image.RGBImageFilter
import javax.swing.Icon
import javax.swing.SwingConstants
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
 * An overlay icon, which can be displayed on top of other icons.
 *
 * This icon is drawn as a base icon surrounded by a small margin of background color, which creates visual distance
 * between the overlay and the rest of the icon this overlay is shown in top of. The background color is determined when
 * the icon is drawn.
 *
 * @see createOverlayedIcon
 */
object OverlayIcons {
    /**
     * Overlay icon for arrays.
     */
    @JvmField
    val ARRAY = create(Icons.ARRAY, Icons.ARRAY_FILLED)

    /**
     * Overlay icon for template references.
     */
    @JvmField
    val REFERENCE = create(Icons.REFERENCE, Icons.REFERENCE_FILLED)

    /**
     * Overlay icon for repeated insertion.
     */
    @JvmField
    val REPEAT = create(Icons.REPEAT, Icons.REPEAT_FILLED)

    /**
     * Overlay icon for settings.
     */
    @JvmField
    val SETTINGS = create(Icons.SETTINGS, Icons.SETTINGS_FILLED)


    /**
     * The margin around the base image that is filled with background color.
     *
     * This number is a fraction relative to the base image's size.
     */
    private const val MARGIN = 4f / 32

    /**
     * Creates a new overlay icon.
     *
     * @param base the base of the icon; must be square
     * @param background the background shape to ensure that the small margin of background color is also applied inside
     * the [base], or `null` if [base] is already a solid shape
     */
    private fun create(base: Icon, background: Icon? = null): Icon =
        LayeredIcon(2).apply {
            val c = JBLabel()
            val filter = RadialColorReplacementFilter(listOf(c.background))  // TODO: Do I need this filter?

            setIcon(filterIcon(background ?: base, filter), 0)
            setIcon(IconUtil.scale(base, c, 1 - 2 * MARGIN), 1, SwingConstants.CENTER)
        }
}



/**
 * Describes a colored icon with some text in it.
 *
 * Does not return an [Icon] because then the fields [text] and [colors] would be lost, making a method such as
 * [combine] a big unstable mess. Does not implement [Icon] because that class should not be extended by plugins. (See
 * https://youtrack.jetbrains.com/issue/IJPL-163887/.)
 *
 * @property base the underlying icon which should be given color; must be square
 * @property text the text to display inside the [base]
 * @property colors the colors to give to the [base]
 */
data class TypeIcon(private val base: Icon, private val text: String, private val colors: List<Color>) {
    /**
     * Creates the corresponding [Icon] object.
     */
    fun init(): Icon {
        require(base.iconWidth == base.iconHeight) { "Base must be square." }
        require(colors.isNotEmpty()) { "At least one color must be defined." }

        val component = JBLabel()
        val baseSize = base.iconWidth
        val filter = RadialColorReplacementFilter(colors, Pair(baseSize / 2, baseSize / 2))

        return LayeredIcon(2).apply {
            setIcon(filterIcon(base, filter), 0)
            setIcon(TextIcon(text, component, FONT_SIZE * baseSize), 1, SwingConstants.CENTER)
        }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The scale of the text inside the icon relative to the icon's size.
         */
        private const val FONT_SIZE = 12f / 32f


        /**
         * Returns a single icon that describes all given [TypeIcon]s, or `null` if [icons] is empty.
         */
        fun combine(icons: Collection<TypeIcon>): TypeIcon? =
            if (icons.isEmpty()) null
            else TypeIcon(
                Icons.TEMPLATE,
                icons.map { it.text }.toSet().singleOrNull() ?: "",
                icons.flatMap { it.colors }
            )
    }
}


/**
 * An overlayed icon, which is a base icon with various overlays placed on top.
 *
 * @property base the underlying base icon
 * @property overlays the various icons that are overlayed on top of [base]
 * @see OverlayIcons
 */
data class OverlayedIcon(private val base: Icon, private val overlays: List<Icon> = emptyList()) {
    fun init(): Icon {
        require(base.iconWidth == base.iconHeight) { "Base must be square." }
        require(overlays.all { it.iconWidth == it.iconHeight }) { "All overlays must be square." }

        val component = JBLabel()
        val targetWidth = base.iconWidth / 2

        val rowIcon = RowIcon(overlays.size, alignment = Alignment.BOTTOM)
        overlays.forEachIndexed { idx, overlay ->
            rowIcon.setIcon(
                if (targetWidth == overlay.iconWidth) overlay
                else IconUtil.scale(overlay, component, targetWidth.toFloat() / overlay.iconWidth),
                idx
            )
        }

        return LayeredIcon(2).apply {
            setIcon(base, 0)
            setIcon(rowIcon, 1, SwingConstants.NORTH_WEST)
        }
    }


    /**
     * Returns a new overlayed icon that has one extra overlay.
     */
    fun plusOverlay(icon: Icon): OverlayedIcon = copy(overlays = overlays + icon)
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
     * Returns [base] after multiplying its alpha by the alpha of [shiftBy].
     */
    private fun shiftAlpha(base: Color, shiftBy: Color): Color =
        ColorUtil.withAlpha(base, asFraction(base.alpha) * asFraction(shiftBy.alpha))

    /**
     * Represents a [number] in the range `[0, 256)` as a fraction of that range.
     */
    private fun asFraction(number: Int): Double = number / COMPONENT_MAX

    /**
     * Returns the appropriate color from [colors] for an [offset] relative to the [center].
     */
    private fun positionToColor(offset: Pair<Int, Int>): Color {
        val angle = 2 * Math.PI + STARTING_ANGLE + atan2(offset.second.toDouble(), offset.first.toDouble())
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
        const val COMPONENT_MAX: Double = 255.0

        /**
         * The angle in radians at which the first color should start being displayed.
         */
        const val STARTING_ANGLE: Double = .25 * (2 * Math.PI)
    }
}

/**
 * Applies the given [filter] to the given [base].
 *
 * This method is not nice and should eventually be deprecated. However, IntelliJ's API is not ready for that yet. See
 * also https://github.com/FWDekker/intellij-randomness/issues/552.
 *
 * @see IconUtil.filterIcon
 * @see IconLoader.filterIcon
 * @see FilteredIcon
 */
private fun filterIcon(base: Icon, filter: RGBImageFilter): FilteredIcon {
    val supplier = object : RgbImageFilterSupplier {
        override fun getFilter(): RGBImageFilter = filter
    }

    return FilteredIcon(base, supplier)
}
