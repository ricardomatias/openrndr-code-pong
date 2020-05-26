import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import kotlin.math.PI


fun main() = application {
    configure {
        width = 800
        height = 600
    }

    oliveProgram {
        val paletteStudio = PaletteStudio()
        val w = 300.0
        val h = height.toDouble()
        val w2 = w / 2.0
        val h2 = h / 2.0
        val w4 = w / 4.0
        val h4 = h / 4.0
        val TAU = 2.0 * PI

        val leftSide = renderTarget(w.toInt(), height) {
            colorBuffer()
        }
        val rightSide = renderTarget(w.toInt(), height) {
            colorBuffer()
        }

        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 64.0)
        val gui = GUI()

        /************** SKETCH *****************/

        val leftComp = compose {
            layer {
                draw {
                    drawer.clear(ColorRGBa.WHITE)

                    (1..10).forEach {
                        drawer.fill = ColorRGBa.BLACK
                        drawer.strokeWeight = 2.0 + ((it*19) % 4) * 2
                        drawer.lineSegment(
                            Vector2.ZERO - 100.0,
                            Polar(it * 8.0, 2000.0).cartesian
                        )
                    }
                }

            }
        }

        val rightComp = compose {
            layer {
                draw {
                    drawer.clear(rgb(0.94))

                    drawer.stroke = ColorRGBa.BLACK

                    Random.resetState()

                    val m = 40.0

                    (1..10).forEach {
                        val r = (h - m) / 10.0
                        val y = it * r

                        drawer.strokeWeight = if (it % 3 == 0) 4.0 else 1.0
                        drawer.lineSegment(
                            Vector2(Random.double(m / 2.0, m), y),
                            Vector2(Random.double(w - m, w - (m / 2)), y)
                        )
                    }
                }

            }
        }

        extend(Screenshots())
        extend(paletteStudio)
        extend(gui)
        extend {
            drawer.isolatedWithTarget(leftSide) {
                drawer.ortho(leftSide)
                leftComp.draw(drawer)
            }

            drawer.image(leftSide.colorBuffer(0), 200.0, 0.0)

            drawer.isolatedWithTarget(rightSide) {
                drawer.ortho(rightSide)
                rightComp.draw(drawer)
            }
            drawer.image(rightSide.colorBuffer(0), 500.0, 0.0)
        }
    }
}