import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.fastFloor
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
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

        paletteStudio.select(53)

        val rt = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        rt.colorBuffer(0).fill(paletteStudio.background)

        val compositor = compose {
            layer {
                draw {
                    drawer.isolatedWithTarget(rt) {
                        drawer.stroke = null
                        drawer.fill = paletteStudio.background.opacify(0.05)
                        drawer.rectangle(0.0, 0.0, w, h)
//                        if (frameCount % (60 * 2) == 0) drawer.background(paletteStudio.background)
                        drawer.translate(w2, h2)

                        val c = contour {
                            val theta = TAU / 30.0
                            var radius = w4
                            val mousePos = (Vector2(w2, h2) - mouse.position)

                            moveTo(Vector2(cos(TAU - theta) * radius, sin(TAU - theta) * radius))

                            for (b in 0 until 8) {
                                for (a in 0 until count) {
                                    val angle = theta * a + sin(seconds * 0.1 + a)
                                    val position = Vector2(cos(angle) * radius, sin(angle) * radius)
                                    val control = position / (2.0 - b * 0.2)
                                    curveTo(control - mousePos, position)
                                }
                                radius += 20.0
                            }
                        }

                        for ((idx, e) in c.exploded.withIndex()) {
                            drawer.stroke = paletteStudio.colors2[idx % paletteStudio.colors2.size]
                            drawer.strokeWeight = 2.0

                            drawer.contour(e)
                        }
                    }

                    drawer.image(rt.colorBuffer(0))
                }
            }
        }

        extend(paletteStudio)
//        extend(gui)
        extend {
            compositor.draw(drawer)
        }
    }
}