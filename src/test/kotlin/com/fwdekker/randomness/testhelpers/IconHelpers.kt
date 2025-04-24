package com.fwdekker.randomness.testhelpers

import com.fwdekker.randomness.TypeIcon
import com.intellij.util.ui.ColorIcon
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.Icon


/**
 * A very simple [ColorIcon].
 */
fun colorIcon(): ColorIcon = ColorIcon(32, Color.BLACK)

/**
 * A very simple [TypeIcon] with only one color.
 */
fun typeIcon(
    size: Int = 32,
    color: Color = Color.BLACK,
    text: String = "",
): TypeIcon = TypeIcon(ColorIcon(size, color), text, listOf(color))

/**
 * Returns an invisible color.
 */
val transparency = Color(0, true)


/**
 * Renders this [Icon] onto a [BufferedImage].
 */
fun Icon.render(): BufferedImage =
    BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB).apply {
        with(createGraphics()) {
            paintIcon(null, this, 0, 0)
            dispose()
        }
    }

/**
 * Returns the [Color] of the center pixel.
 */
fun BufferedImage.getCenterColor(): Color = Color(getRGB(width / 2 - 1, height / 2 - 1), true)

/**
 * Returns the [Color] of the pixel that is [offset] pixels south of the northernmost pixel.
 */
fun BufferedImage.getNorthColor(offset: Int = 0): Color = Color(getRGB(width / 2 - 1, offset), true)

/**
 * Returns the [Color] of the pixel that is [offset] pixels east of the easternmost pixel.
 */
fun BufferedImage.getEastColor(offset: Int = 0): Color = Color(getRGB(width - 1 - offset, height / 2 - 1), true)

/**
 * Returns the [Color] of the pixel that is [offset] pixels north of the southernmost pixel.
 */
fun BufferedImage.getSouthColor(offset: Int = 0): Color = Color(getRGB(width / 2 - 1, height - 1 - offset), true)

/**
 * Returns the [Color] of the pixel that is [offset] pixels east of the westernmost pixel.
 */
fun BufferedImage.getWestColor(offset: Int = 0): Color = Color(getRGB(offset, height / 2 - 1), true)
