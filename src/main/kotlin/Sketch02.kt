import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
import org.openrndr.draw.shadeStyle
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
import kotlin.math.cos
import kotlin.math.sin


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

                    (1..45).forEach {
                        val c = Vector2(100.0, h2 * 1.1 * sin(seconds * 0.1 + it) - h2)
                        drawer.fill = ColorRGBa.BLACK
                        drawer.strokeWeight = 2.0 + ((it * 19) % 4) * 2
                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = "float l = length(v_viewPosition.xy+p_c); x_stroke.rgb += cos(l*0.1 + p_time + cos(l*0.4)) + 0.5 + 0.5*cos(l*cos(l));"
                            parameter("c", c)
                            parameter("time", (it * 4) % TAU + seconds)
                        }
                        drawer.lineSegment(-c, Polar(it * 8.0, 2000.0).cartesian)
                    }
                }

            }
        }

        val rightComp = compose {
            layer {
                draw {
                    drawer.clear(rgb(0.0))

                    drawer.stroke = ColorRGBa.WHITE

                    Random.resetState()

                    val m = 40.0

                    (1..10).forEach {
                        val r = (h - m) / 10.0
                        val y = it * r

                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = """
                                x_stroke.rgb *= step(p_time, 0.0);
                                x_stroke.rgb *= abs(p_time) + floor(v_ftcoord.x * 2.0) / 2.0;
                            """.trimIndent()
                            parameter("time", cos(it * 20.0 % TAU + seconds))
                        }

                        drawer.strokeWeight = 2.0 + (it % 3) * 2.0
                        drawer.lineSegment(
                            Vector2(0.0, y),
                            Vector2(w2 + it * (w2 / 10.0), y)
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