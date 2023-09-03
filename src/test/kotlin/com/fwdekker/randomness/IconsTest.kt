package com.fwdekker.randomness

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.JLabel


/**
 * Unit tests for [TypeIcon].
 */
object TypeIconTest : FunSpec({
    context("constructor") {
        test("fails if no colors are given") {
            shouldThrow<IllegalArgumentException> { TypeIcon(PlainIcon(), "text", emptyList()) }
                .message shouldBe "At least one color must be defined."
        }
    }


    context("combineWith") {
        test("returns a template TypeIcon") {
            val icon1 = TypeIcon(PlainIcon(), "text1", listOf(Color.GREEN))
            val icon2 = TypeIcon(PlainIcon(), "text2", listOf(Color.RED))

            icon1.combineWith(icon2).base shouldBe Icons.TEMPLATE
        }

        test("retains the text if it is the same for both icons") {
            val icon1 = TypeIcon(PlainIcon(), "text", listOf(Color.PINK))
            val icon2 = TypeIcon(PlainIcon(), "text", listOf(Color.CYAN))

            icon1.combineWith(icon2).text shouldBe "text"
        }

        test("removes the text if it is different for the icons") {
            val icon1 = TypeIcon(PlainIcon(), "text1", listOf(Color.GRAY))
            val icon2 = TypeIcon(PlainIcon(), "text2", listOf(Color.ORANGE))

            icon1.combineWith(icon2).text shouldBe ""
        }

        test("appends the colors of the combined icons") {
            val icon1 = TypeIcon(PlainIcon(), "text1", listOf(Color.BLUE, Color.WHITE))
            val icon2 = TypeIcon(PlainIcon(), "text2", listOf(Color.RED, Color.PINK))

            icon1.combineWith(icon2).colors shouldContainExactly listOf(Color.BLUE, Color.WHITE, Color.RED, Color.PINK)
        }
    }


    context("paintIcon") {
        tags(NamedTag("Swing"))


        lateinit var image: BufferedImage


        beforeContainer {
            FailOnThreadViolationRepaintManager.install()
        }

        beforeEach {
            image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        }


        test("paints nothing if the component is null") {
            with(image.createGraphics()) {
                TypeIcon(PlainIcon(), "text", listOf(Color.PINK)).paintIcon(null, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0, 32, 32, null, 0, 32).forEach { it shouldBe 0 }
        }

        test("paints something otherwise") {
            with(image.createGraphics()) {
                val label = guiGet { JLabel() }

                TypeIcon(PlainIcon(), "text", listOf(Color.GRAY)).paintIcon(label, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0, 32, 32, null, 0, 32).forEach { it shouldNotBe 0 }
        }
    }
})

/**
 * Unit tests for [OverlayIcon].
 */
object OverlayIconTest : FunSpec({
    context("paintIcon") {
        tags(NamedTag("Swing"))


        lateinit var image: BufferedImage


        beforeContainer {
            FailOnThreadViolationRepaintManager.install()
        }

        beforeEach {
            image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        }


        test("paints nothing if the component is null") {
            with(image.createGraphics()) {
                OverlayIcon(PlainIcon(), PlainIcon()).paintIcon(null, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0, 32, 32, null, 0, 32).forEach { it shouldBe 0 }
        }

        test("fills the margin with background color") {
            with(image.createGraphics()) {
                val icon1 = PlainIcon(color = Color.MAGENTA.rgb)
                val icon2 = PlainIcon(color = Color.BLUE.rgb)
                val label = JLabel().also { it.background = Color.RED }

                OverlayIcon(icon1, icon2).paintIcon(label, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0) shouldBe Color.RED.rgb
            image.getRGB(0, 31) shouldBe Color.RED.rgb
            image.getRGB(31, 0) shouldBe Color.RED.rgb
            image.getRGB(31, 31) shouldBe Color.RED.rgb
        }

        test("fills the margin with background color if no background icon is set") {
            with(image.createGraphics()) {
                val label = JLabel().also { it.background = Color.BLUE }

                OverlayIcon(PlainIcon(color = Color.RED.rgb)).paintIcon(label, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0) shouldBe Color.BLUE.rgb
            image.getRGB(0, 31) shouldBe Color.BLUE.rgb
            image.getRGB(31, 0) shouldBe Color.BLUE.rgb
            image.getRGB(31, 31) shouldBe Color.BLUE.rgb
        }

        test("fills the center with the base icon") {
            with(image.createGraphics()) {
                val icon1 = PlainIcon(color = Color.YELLOW.rgb)
                val icon2 = PlainIcon(color = Color.CYAN.rgb)
                val label = JLabel().also { it.background = Color.WHITE }

                OverlayIcon(icon1, icon2).paintIcon(label, this, 0, 0)
                this.dispose()
            }

            image.getRGB(8, 8) shouldBe Color.YELLOW.rgb
        }
    }
})

/**
 * Unit tests for [OverlayedIcon].
 */
object OverlayedIconTest : FunSpec({
    context("constructor") {
        test("fails if the base image is not square") {
            shouldThrow<IllegalArgumentException> {
                OverlayedIcon(PlainIcon(186, 132), emptyList())
            }.message shouldBe "Base must be square."
        }

        test("fails if an overlay is not square") {
            shouldThrow<IllegalArgumentException> { OverlayedIcon(PlainIcon(), listOf(PlainIcon(), PlainIcon(38, 40))) }
                .message shouldBe "Overlays must be square."
        }

        test("fails if overlays have different sizes") {
            shouldThrow<IllegalArgumentException> { OverlayedIcon(PlainIcon(), listOf(PlainIcon(32, 32), PlainIcon(34, 34))) }
                .message shouldBe "All overlays must have same size."
        }
    }


    context("plusOverlay") {
        test("returns a copy with the given overlay added") {
            val newOverlay = PlainIcon()
            val icon = OverlayedIcon(PlainIcon(), listOf(PlainIcon(), PlainIcon()))

            icon.plusOverlay(newOverlay).overlays.last() shouldBeSameInstanceAs newOverlay
        }
    }


    context("paintIcon") {
        tags(NamedTag("Swing"))


        lateinit var image: BufferedImage


        beforeContainer {
            FailOnThreadViolationRepaintManager.install()
        }

        beforeEach {
            image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        }


        test("paints nothing if the component is null") {
            with(image.createGraphics()) {
                OverlayedIcon(PlainIcon(), listOf(PlainIcon())).paintIcon(null, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0, 32, 32, null, 0, 32).forEach { it shouldBe 0 }
        }

        test("paints the base icon only if no overlays are specified") {
            with(image.createGraphics()) {
                val label = JLabel()

                OverlayedIcon(PlainIcon(color = Color.GREEN.rgb)).paintIcon(label, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0, 32, 32, null, 0, 32).forEach { it shouldBe Color.GREEN.rgb }
        }

        test("paints the overlays starting in the top-left corner") {
            with(image.createGraphics()) {
                val label = JLabel().also { it.background = Color.BLUE }

                OverlayedIcon(PlainIcon(), listOf(PlainIcon(color = Color.BLUE.rgb))).paintIcon(label, this, 0, 0)
                this.dispose()
            }

            image.getRGB(0, 0, 16, 16, null, 0, 16).forEach { it shouldBe Color.BLUE.rgb }
            image.getRGB(16, 16, 16, 16, null, 0, 16).forEach { it shouldNotBe Color.BLUE.rgb }
        }
    }
})


/**
 * Unit tests for [RadialColorReplacementFilter].
 */
object RadialColorReplacementFilterTest : FunSpec({
    context("constructor") {
        test("fails if no colors are given") {
            shouldThrow<IllegalArgumentException> { RadialColorReplacementFilter(emptyList(), Pair(0, 0)) }
                .message shouldBe "At least one color must be defined."
        }

        test("fails if more than one color but no center is given") {
            shouldThrow<IllegalArgumentException> { RadialColorReplacementFilter(listOf(Color.BLUE, Color.RED)) }
                .message shouldBe "Center must be defined if more than one color is given."
        }
    }


    context("filterRGB") {
        test("returns 0 if the given pixel is 0") {
            val filter = RadialColorReplacementFilter(listOf(Color.RED), Pair(0, 0))

            filter.filterRGB(0, 0, 0) shouldBe 0
        }

        test("returns the single color if no center is set") {
            val filter = RadialColorReplacementFilter(listOf(Color.GREEN))

            filter.filterRGB(826, 308, Color.MAGENTA.rgb) shouldBe Color.GREEN.rgb
        }

        test("returns the single color if one color is set") {
            val filter = RadialColorReplacementFilter(listOf(Color.CYAN))

            filter.filterRGB(781, 114, Color.ORANGE.rgb) shouldBe Color.CYAN.rgb
        }

        test("shifts the single color if no center is set") {
            val filter = RadialColorReplacementFilter(listOf(Color.PINK))

            val filtered = filter.filterRGB(13, 73, Color(108, 68, 6, 12).rgb)

            Color(filtered, true).alpha shouldBe 12
        }

        test("returns the first of four colors if a position above the center is given") {
            val filter = RadialColorReplacementFilter(listOf(Color.RED, Color.BLUE, Color.PINK, Color.GRAY), Pair(0, 0))

            filter.filterRGB(0, 12, Color.MAGENTA.rgb) shouldBe Color.RED.rgb
        }

        test("returns the second color of four colors if a position to the right of the center is given") {
            val filter = RadialColorReplacementFilter(listOf(Color.RED, Color.BLUE, Color.PINK, Color.GRAY), Pair(0, 0))

            filter.filterRGB(104, 0, Color.PINK.rgb) shouldBe Color.BLUE.rgb
        }

        test("shifts the color") {
            val filter = RadialColorReplacementFilter(listOf(Color(105, 19, 53, 106), Color.DARK_GRAY), Pair(228, 12))

            val filtered = filter.filterRGB(229, 14, Color(148, 145, 135, 163).rgb)

            Color(filtered, true).alpha shouldBe 106 * 163 / 255
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
