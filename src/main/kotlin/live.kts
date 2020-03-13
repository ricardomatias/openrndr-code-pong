@file:Suppress("UNUSED_LAMBDA_EXPRESSION")
import org.openrndr.Program
import org.openrndr.draw.*
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


{ program: Program ->
    program.apply {
        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 64.0)
        val w = width.toDouble()
        val h = height.toDouble()
        val w2 = w / 2.0
        val h2 = h / 2.0
        val w4 = w / 4.0
        val h4 = h / 4.0
        val TAU = 2.0 * PI

        val gui = GUI()
        val count = 30

        val c = contour {
            val theta = TAU / 30.0
            val radius = w4

            moveTo(Vector2(cos(TAU - theta) * radius, sin(TAU - theta) * radius))

            for (a in 0 until count) {
                val position = Vector2(cos(theta * a) * radius, sin(theta * a) * radius)
                val control = position / 2.0
                curveTo(control, position)
            }
        }

        val compositor = compose {
            layer {
                draw {
                    drawer.background(paletteStudio.background)
                    drawer.translate(w2, h2)

                    for ((idx, e) in c.exploded.withIndex()) {
                        drawer.stroke = paletteStudio.colors2[idx % paletteStudio.colors2.size]
                        drawer.strokeWeight = 2.0

                        drawer.contour(e)
                    }
                }
            }
        }

        paletteStudio.select(53)

        extend(paletteStudio)
        extend(gui)
        extend {
            compositor.draw(drawer)
        }
    }
}