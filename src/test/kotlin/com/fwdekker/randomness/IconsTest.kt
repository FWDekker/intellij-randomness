@file:OptIn(ExperimentalKotest::class)

package com.fwdekker.randomness

import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beSameIconAs
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.colorIcon
import com.fwdekker.randomness.testhelpers.getEastColor
import com.fwdekker.randomness.testhelpers.getWestColor
import com.fwdekker.randomness.testhelpers.render
import com.fwdekker.randomness.testhelpers.typeIcon
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.EmptyIcon
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.contain
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import java.awt.Color


/**
 * Unit tests for [TypeIcon].
 */
object TypeIconTest : FunSpec({
    context("constructor") {
        test("fails if the base is not square") {
            val base = ColorIcon(32, 30, 32, 30, Color.BLACK, false)

            shouldThrow<IllegalArgumentException> { TypeIcon(base, "text", emptyList()) }
                .message shouldBe "Base must be square."
        }

        test("fails if no colors are given") {
            shouldThrow<IllegalArgumentException> { TypeIcon(colorIcon(), "text", emptyList()) }
                .message shouldBe "At least one color must be defined."
        }
    }


    context("get").config(tags = setOf(Tags.IDEA_FIXTURE, Tags.SWING)) {
        lateinit var ideaFixture: IdeaTestFixture


        beforeSpec {
            FailOnThreadViolationRepaintManager.install()
        }

        afterSpec {
            FailOnThreadViolationRepaintManager.uninstall()
        }

        beforeNonContainer {
            ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
            ideaFixture.setUp()
        }

        afterNonContainer {
            ideaFixture.tearDown()
        }


        test("returns a non-empty icon") {
            val icon = TypeIcon(colorIcon(), "text", listOf(Color.GRAY)).get()

            val image = icon.render()

            image.getRGB(0, 0, 32, 32, null, 0, 32).filter { it != 0 } shouldNot beEmpty()
        }

        test("returns an icon that paints the specified colors") {
            val icon = TypeIcon(colorIcon(), "", listOf(Color.RED, Color.BLUE)).get()

            val image = icon.render()

            image.getEastColor(offset = 1) shouldBe Color.RED
            image.getWestColor(offset = 1) shouldBe Color.BLUE
        }
    }


    context("combine") {
        test("returns `null` if no icons are given to combine") {
            TypeIcon.combine(emptyList()) should beNull()
        }

        test("returns a template icon for a single icon") {
            val icons = listOf(TypeIcon(colorIcon(), "text", listOf(Color.LIGHT_GRAY)))

            TypeIcon.combine(icons)!!.base shouldBe Icons.TEMPLATE
        }

        test("returns a template icon for multiple icons") {
            val icons =
                listOf(
                    TypeIcon(colorIcon(), "text1", listOf(Color.GREEN)),
                    TypeIcon(colorIcon(), "text2", listOf(Color.RED)),
                    TypeIcon(colorIcon(), "text3", listOf(Color.MAGENTA)),
                )

            TypeIcon.combine(icons)!!.base shouldBe Icons.TEMPLATE
        }

        test("retains the text if it is the same for all icons") {
            val icons =
                listOf(
                    TypeIcon(colorIcon(), "text", listOf(Color.PINK)),
                    TypeIcon(colorIcon(), "text", listOf(Color.CYAN)),
                    TypeIcon(colorIcon(), "text", listOf(Color.GREEN)),
                )

            TypeIcon.combine(icons)!!.text shouldBe "text"
        }

        test("removes the text if it is not the same for all icons") {
            val icons =
                listOf(
                    TypeIcon(colorIcon(), "text1", listOf(Color.GRAY)),
                    TypeIcon(colorIcon(), "text2", listOf(Color.ORANGE)),
                    TypeIcon(colorIcon(), "text2", listOf(Color.BLUE)),
                )

            TypeIcon.combine(icons)!!.text shouldBe ""
        }

        test("appends the colors of the combined icons") {
            val icons =
                listOf(
                    TypeIcon(colorIcon(), "text1", listOf(Color.BLUE, Color.WHITE)),
                    TypeIcon(colorIcon(), "text2", listOf(Color.RED)),
                    TypeIcon(colorIcon(), "text3", listOf(Color.PINK, Color.BLACK, Color.BLUE)),
                )

            TypeIcon.combine(icons)!!.colors shouldContainExactly
                listOf(Color.BLUE, Color.WHITE, Color.RED, Color.PINK, Color.BLACK, Color.BLUE)
        }
    }
})

/**
 * Unit tests for [OverlayIcon].
 */
object OverlayIconTest : FunSpec({
    context("get") {
        test("returns the base icon") {
            val base = ColorIcon(32, Color.YELLOW)
            val fill = ColorIcon(32, Color.CYAN)
            val icon = OverlayIcon(base, fill)

            icon.get() shouldBeSameInstanceAs base
        }
    }
})

/**
 * Unit tests for [OverlayedIcon].
 */
object OverlayedIconTest : FunSpec({
    context("validation") {
        test("fails if there are more overlays than supported") {
            val overlays = listOf(OverlayIcon.ARRAY, OverlayIcon.REFERENCE, OverlayIcon.REPEAT)

            shouldThrow<IllegalArgumentException> { OverlayedIcon(typeIcon(), overlays) }
                .message shouldBe "A maximum of 2 overlays is currently the limit."
        }
    }


    context("plusOverlay") {
        test("does not alter the original icon") {
            val icon = OverlayedIcon(typeIcon(), listOf(OverlayIcon.ARRAY))
            icon.overlays should haveSize(1)

            val alteredIcon = icon.plusOverlay(OverlayIcon.SETTINGS)

            icon.overlays should haveSize(1)
            alteredIcon.overlays should haveSize(2)
        }

        test("returns a copy with the given overlay added") {
            val overlay = OverlayIcon.REPEAT
            val icon = OverlayedIcon(typeIcon(), listOf(OverlayIcon.ARRAY))
            icon.overlays should haveSize(1)
            icon.overlays shouldNot contain(overlay)

            val alteredIcon = icon.plusOverlay(overlay)

            alteredIcon.overlays should haveSize(2)
            alteredIcon.overlays.last() shouldBe overlay
        }
    }


    context("get").config(tags = setOf(Tags.IDEA_FIXTURE, Tags.SWING)) {
        lateinit var ideaFixture: IdeaTestFixture


        beforeSpec {
            FailOnThreadViolationRepaintManager.install()
        }

        afterSpec {
            FailOnThreadViolationRepaintManager.uninstall()
        }

        beforeNonContainer {
            ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
            ideaFixture.setUp()
        }

        afterNonContainer {
            ideaFixture.tearDown()
        }


        test("returns the base icon if no overlays are specified") {
            val base = typeIcon()

            val overlayed = OverlayedIcon(base)

            base should beSameIconAs(overlayed)
        }

        test("returns an icon that paints the base even if overlays are specified") {
            val icon = OverlayedIcon(typeIcon(color = Color.RED), listOf(OverlayIcon.REFERENCE)).get()

            val image = icon.render()

            image.getRGB(0, 16, 32, 16, null, 0, 32).forEach { it shouldBe Color.RED.rgb } // Bottom half
        }

        test("returns an icon that also paints the overlay") {
            val overlay = OverlayIcon(ColorIcon(32, Color.BLUE))
            val icon = OverlayedIcon(typeIcon(), listOf(overlay)).get()

            val image = icon.render()

            image.getRGB(2, 2, 12, 12, null, 0, 12).forEach { it shouldBe Color.BLUE.rgb } // Overlay base
        }

        test("returns an icon that fills the top-left quadrant with the overlay, with a 2-pixel empty margin") {
            val overlay = OverlayIcon(ColorIcon(32, Color.BLUE))
            val icon = OverlayedIcon(typeIcon(), listOf(overlay)).get()

            val image = icon.render()

            image.getRGB(2, 2, 12, 12, null, 0, 12).forEach { it shouldBe Color.BLUE.rgb } // Overlay base
            image.getRGB(0, 0, 16, 2, null, 0, 16).forEach { it shouldBe 0 } // Top margin
            image.getRGB(0, 0, 2, 16, null, 0, 2).forEach { it shouldBe 0 } // Left margin
            image.getRGB(0, 14, 16, 2, null, 0, 16).forEach { it shouldBe 0 } // Bottom margin
            image.getRGB(14, 0, 2, 16, null, 0, 2).forEach { it shouldBe 0 } // Right margin
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

        test("arranges four colors in clockwise order starting from the top") {
            val colors = listOf(Color.RED, Color.BLUE, Color.PINK, Color.GRAY)
            val filter = RadialColorReplacementFilter(colors, Pair(0, 0))

            // Uses pixel coordinates, so "up" is negative y and "down" is positive y
            filter.filterRGB(5, -5, Color.MAGENTA.rgb) shouldBe colors[0].rgb
            filter.filterRGB(5, 5, Color.MAGENTA.rgb) shouldBe colors[1].rgb
            filter.filterRGB(-5, 5, Color.MAGENTA.rgb) shouldBe colors[2].rgb
            filter.filterRGB(-5, -5, Color.MAGENTA.rgb) shouldBe colors[3].rgb
        }

        test("shifts the color") {
            val filter = RadialColorReplacementFilter(listOf(Color(105, 19, 53, 106), Color.DARK_GRAY), Pair(228, 12))

            val filtered = filter.filterRGB(229, 14, Color(148, 145, 135, 163).rgb)

            Color(filtered, true).alpha shouldBe 106 * 163 / 255
        }
    }
})

/**
 * Unit tests for [SubtractionFilter].
 */
object SubtractionFilterTest : FunSpec({
    context("filterRGB") {
        test("returns the original color if the mask is a zero-size image") {
            val filter = SubtractionFilter(ColorIcon(0, Color.BLACK))

            filter.filterRGB(0, 0, 520_794) shouldBe 520_794
            filter.filterRGB(108, 797, 33) shouldBe 33
            filter.filterRGB(996, 852, 77_517) shouldBe 77_517
        }

        test("returns the original color if the requested coordinate is beyond the mask's dimensions") {
            val filter = SubtractionFilter(colorIcon())

            filter.filterRGB(356, 874, 634) shouldBe 634
            filter.filterRGB(515, 482, 566) shouldBe 566
            filter.filterRGB(726, 924, 704) shouldBe 704
        }

        test("returns the original color if the coordinates refer to an empty pixel in the mask") {
            val filter = SubtractionFilter(EmptyIcon.create(32))

            filter.filterRGB(5, 14, 20_179) shouldBe 20_179
            filter.filterRGB(28, 4, 45_490) shouldBe 45_490
            filter.filterRGB(11, 28, 43_481) shouldBe 43_481
        }

        test("returns an empty pixel if the coordinates refer to a non-empty pixel in the mask") {
            val filter = SubtractionFilter(colorIcon())

            filter.filterRGB(21, 3, 58_419) shouldBe 0
            filter.filterRGB(22, 31, 50_969) shouldBe 0
            filter.filterRGB(0, 24, 15_447) shouldBe 0
        }
    }
})
