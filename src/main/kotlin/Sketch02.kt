import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
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
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blur.LaserBlur
import org.openrndr.extra.fx.edges.LumaSobel
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.noise.Random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mod
import java.io.File
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

        class LAnim() : Animatable() {
            var rot: Double = 0.0
        }
        val lAnim = LAnim()
        val leftComp = compose {
             layer {
                draw {
                    lAnim.updateAnimation()
                    if (!lAnim.hasAnimations()) {
                        Random.seed = System.currentTimeMillis().toString()
                        lAnim.animate("rot", lAnim.rot + Random.double(-45.0, 45.0), Random.int(200, 2000).toLong(), Easing.CubicInOut)
                        lAnim.complete()
                        lAnim.delay(Random.int(1000, 10000).toLong())
                    }
                    drawer.clear(ColorRGBa.WHITE)

                    (1..45).forEach {
                        val c = Vector2(100.0, h2 * 1.1 * sin((seconds + lAnim.rot) * 0.1  + it) - h2)
                        drawer.strokeWeight = 2.0 + ((it * 19) % 4) * 2
                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = "float l = length(v_viewPosition.xy+p_c); x_stroke.rgb += cos(l*0.1 + p_time + cos(l*0.4)) + 0.5 + 0.5*cos(l*cos(l)) + vec3(0.0, p_r, -p_r);"
                            parameter("c", c)
                            parameter("r", if(it % 17 == 3) 1.0 else 0.0)
                            parameter("time", (it * 4) % TAU + seconds + lAnim.rot)
                        }
                        drawer.lineSegment(-c, Polar(it * 8.0 + lAnim.rot, 2000.0).cartesian)
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
                        val y = mod(it * r + lAnim.rot * 10.0, h)

                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = """
                                x_stroke.rgb *= step(p_time, 0.0);
                                x_stroke.rgb *= abs(p_time) + floor(v_ftcoord.x * 2.0) / 2.0;
                            """.trimIndent()
                            parameter("time", cos(it * 20.0 % TAU + seconds * 5.0))
                        }

                        drawer.strokeWeight = 2.0 + (it % 3) * 4.0
                        drawer.lineSegment(
                            Vector2(lAnim.rot * 10.0, y),
                            Vector2(w2 + w2 - lAnim.rot * 10.0, it * r)
                        )
                    }
                }
                post(LaserBlur().addTo(gui))
                post(LumaSobel())
            }
        }

        gui.loadParameters(File("data/parameters/params.json"))

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