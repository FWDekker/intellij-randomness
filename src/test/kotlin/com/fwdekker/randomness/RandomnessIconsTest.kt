package com.fwdekker.randomness

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.JLabel


/**
 * Unit tests for [TypeIcon].
 */
object TypeIconTest : Spek({
    describe("constructor") {
        it("fails if no colors are given") {
            assertThatThrownBy { TypeIcon(PlainIcon(), "relief", emptyList()) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("At least one color must be defined.")
        }
    }


    describe("combineWith") {
        it("returns a template TypeIcon") {
            val icon1 = TypeIcon(PlainIcon(), "event", listOf(Color.GREEN))
            val icon2 = TypeIcon(PlainIcon(), "month", listOf(Color.RED))

            assertThat(icon1.combineWith(icon2).base).isEqualTo(RandomnessIcons.TEMPLATE)
        }

        it("retains the text if it is the same for both icons") {
            val icon1 = TypeIcon(PlainIcon(), "whoever", listOf(Color.PINK))
            val icon2 = TypeIcon(PlainIcon(), "whoever", listOf(Color.CYAN))

            assertThat(icon1.combineWith(icon2).text).isEqualTo("whoever")
        }

        it("removes the text if it is different for the icons") {
            val icon1 = TypeIcon(PlainIcon(), "brick", listOf(Color.GRAY))
            val icon2 = TypeIcon(PlainIcon(), "language", listOf(Color.ORANGE))

            assertThat(icon1.combineWith(icon2).text).isEqualTo("")
        }

        it("uses the colors of both icons") {
            val icon1 = TypeIcon(PlainIcon(), "castle", listOf(Color.MAGENTA, Color.WHITE))
            val icon2 = TypeIcon(PlainIcon(), "wicked", listOf(Color.RED, Color.PINK))

            assertThat(icon1.combineWith(icon2).colors)
                .containsExactly(Color.MAGENTA, Color.WHITE, Color.RED, Color.PINK)
        }
    }


    describe("paintIcon") {
        it("paints nothing if the component is null") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()

            TypeIcon(PlainIcon(), "somehow", listOf(Color.PINK)).paintIcon(null, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0, 32, 32, null, 0, 32).all { it == 0 }).isTrue()
        }

        it("paints something otherwise") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()
            val c = GuiActionRunner.execute<JLabel> { JLabel() }

            TypeIcon(PlainIcon(), "check", listOf(Color.GRAY)).paintIcon(c, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0, 32, 32, null, 0, 32).all { it == 0 }).isFalse()
        }
    }
})

/**
 * Unit tests for [OverlayIcon].
 */
object OverlayIconTest : Spek({
    describe("paintIcon") {
        it("paints nothing if the component is null") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()

            OverlayIcon(PlainIcon(), PlainIcon()).paintIcon(null, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0, 32, 32, null, 0, 32).all { it == 0 }).isTrue()
        }

        it("fills the margin with background color") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()
            val c = JLabel().also { it.background = Color.RED }

            OverlayIcon(PlainIcon(color = Color.MAGENTA.rgb), PlainIcon(color = Color.BLUE.rgb)).paintIcon(c, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0)).isEqualTo(Color.RED.rgb)
            assertThat(i.getRGB(0, 31)).isEqualTo(Color.RED.rgb)
            assertThat(i.getRGB(31, 0)).isEqualTo(Color.RED.rgb)
            assertThat(i.getRGB(31, 31)).isEqualTo(Color.RED.rgb)
        }

        it("fills the margin with background color if no background icon is set") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()
            val c = JLabel().also { it.background = Color.BLUE }

            OverlayIcon(PlainIcon(color = Color.RED.rgb)).paintIcon(c, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0)).isEqualTo(Color.BLUE.rgb)
            assertThat(i.getRGB(0, 31)).isEqualTo(Color.BLUE.rgb)
            assertThat(i.getRGB(31, 0)).isEqualTo(Color.BLUE.rgb)
            assertThat(i.getRGB(31, 31)).isEqualTo(Color.BLUE.rgb)
        }

        it("fills the center with the base icon") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()
            val c = JLabel().also { it.background = Color.WHITE }

            OverlayIcon(PlainIcon(color = Color.YELLOW.rgb), PlainIcon(color = Color.CYAN.rgb)).paintIcon(c, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(8, 8)).isEqualTo(Color.YELLOW.rgb)
        }
    }
})

/**
 * Unit tests for [OverlayedIcon].
 */
object OverlayedIconTest : Spek({
    describe("constructor") {
        it("fails if the base image is not square") {
            assertThatThrownBy { OverlayedIcon(PlainIcon(186, 132), emptyList()) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Base must be square.")
        }

        it("fails if an overlay is not square") {
            assertThatThrownBy { OverlayedIcon(PlainIcon(), listOf(PlainIcon(), PlainIcon(38, 205))) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Overlays must be square.")
        }

        it("fails if overlays have different sizes") {
            assertThatThrownBy { OverlayedIcon(PlainIcon(), listOf(PlainIcon(32, 32), PlainIcon(34, 34))) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("All overlays must have same size.")
        }
    }


    describe("plusOverlay") {
        it("returns a copy with the given overlay added") {
            val newOverlay = PlainIcon()
            val icon = OverlayedIcon(PlainIcon(), listOf(PlainIcon(), PlainIcon()))

            assertThat(icon.plusOverlay(newOverlay).overlays.last()).isSameAs(newOverlay)
        }
    }


    describe("paintIcon") {
        it("paints nothing if the component is null") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()

            OverlayedIcon(PlainIcon(), listOf(PlainIcon())).paintIcon(null, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0, 32, 32, null, 0, 32).all { it == 0 }).isTrue()
        }

        it("paints the base icon only if no overlays are specified") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()
            val c = JLabel()

            OverlayedIcon(PlainIcon(color = Color.GREEN.rgb)).paintIcon(c, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0, 32, 32, null, 0, 32).all { it == Color.GREEN.rgb }).isTrue()
        }

        it("paints the overlays starting in the top-left corner") {
            val i = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
            val g = i.createGraphics()
            val c = JLabel().also { it.background = Color.BLUE }

            OverlayedIcon(PlainIcon(), listOf(PlainIcon(color = Color.BLUE.rgb))).paintIcon(c, g, 0, 0)
            g.dispose()

            assertThat(i.getRGB(0, 0, 16, 16, null, 0, 16).all { it == Color.BLUE.rgb }).isTrue()
            assertThat(i.getRGB(16, 16, 16, 16, null, 0, 16).all { it != Color.BLUE.rgb }).isTrue()
        }
    }
})


/**
 * Unit tests for [RadialColorReplacementFilter].
 */
object RadialColorReplacementFilterTest : Spek({
    describe("constructor") {
        it("fails if no colors are given") {
            assertThatThrownBy { RadialColorReplacementFilter(emptyList(), Pair(0, 0)) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("At least one color must be defined.")
        }

        it("fails if more than one color but no center is given") {
            assertThatThrownBy { RadialColorReplacementFilter(listOf(Color.BLUE, Color.RED)) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Center must be defined if more than one color is given.")
        }
    }


    describe("filterRGB") {
        it("returns 0 if the given pixel is 0") {
            val filter = RadialColorReplacementFilter(listOf(Color.RED), Pair(0, 0))

            assertThat(filter.filterRGB(0, 0, 0)).isEqualTo(0)
        }

        it("returns the single color if no center is set") {
            val filter = RadialColorReplacementFilter(listOf(Color.GREEN))

            assertThat(filter.filterRGB(826, 308, Color.MAGENTA.rgb)).isEqualTo(Color.GREEN.rgb)
        }

        it("returns the single color if one color is set") {
            val filter = RadialColorReplacementFilter(listOf(Color.CYAN))

            assertThat(filter.filterRGB(781, 114, Color.ORANGE.rgb)).isEqualTo(Color.CYAN.rgb)
        }

        it("shifts the single color if no center is set") {
            val filter = RadialColorReplacementFilter(listOf(Color.PINK))

            val filtered = filter.filterRGB(13, 73, Color(108, 68, 6, 12).rgb)

            assertThat(Color(filtered, true).alpha).isEqualTo(12)
        }

        it("returns the first of four colors if a position above the center is given") {
            val filter =
                RadialColorReplacementFilter(listOf(Color.RED, Color.GREEN, Color.PINK, Color.YELLOW), Pair(0, 0))

            assertThat(filter.filterRGB(0, 12, Color.MAGENTA.rgb)).isEqualTo(Color.RED.rgb)
        }

        it("returns the second color of four colors if a position to the right of the center is given") {
            val filter =
                RadialColorReplacementFilter(listOf(Color.YELLOW, Color.WHITE, Color.GRAY, Color.GREEN), Pair(0, 0))

            assertThat(filter.filterRGB(104, 0, Color.PINK.rgb)).isEqualTo(Color.WHITE.rgb)
        }

        it("shifts the color") {
            val filter = RadialColorReplacementFilter(listOf(Color(105, 19, 53, 106), Color.DARK_GRAY), Pair(228, 12))

            val filtered = filter.filterRGB(229, 14, Color(148, 145, 135, 163).rgb)

            assertThat(Color(filtered, true).alpha).isEqualTo(106 * 163 / 255)
        }
    }
})


/**
 * A plain, single-color icon of given width and height.
 *
 * @property width The width of the icon.
 * @property height The height of the icon.
 * @property color The color to fill the icon with; default is transparent.
 */
private data class PlainIcon(
    private val width: Int = 32,
    private val height: Int = 32,
    private val color: Int = 0,
) : Icon {
    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        g?.color = Color(color, true)
        g?.fillRect(x, y, width, height)
    }

    override fun getIconWidth() = width

    override fun getIconHeight() = height
}
