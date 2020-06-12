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
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mod
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
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

        rightSide.colorBuffer(0).fill(ColorRGBa.BLACK)

        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 64.0)
        val gui = GUI()

        /************** SKETCH *****************/

        class LAnim : Animatable() {
            var rot = 0.0
            var x = 0.0
            var y = 0.0
        }

        val lAnims = List(45) { LAnim() }
        val barcode = shadeStyle {
            fragmentTransform = """
                float l = length(v_viewPosition.xy-p_c);
                float modt = mod(p_id + p_time, 17.0);
                float r = smoothstep(4.5, 5.0, modt) - smoothstep(6.0, 6.5, modt);
                x_stroke.rgb += cos(l * 0.1 + p_time + cos(l * 0.4)) + 0.5 + 0.5 * cos(l * cos(l)) + vec3(r, r * 0.6, 0);
                x_stroke.rgb = mix(x_stroke.rgb, vec3(1.0), pow(1.0 - p_id / 45.0, 2.5));
                """.trimIndent()
        }
        val leftComp = compose {
            layer {
                draw {
                    drawer.clear(ColorRGBa.WHITE)
                    Random.seed = System.currentTimeMillis().toString()

                    lAnims.forEachIndexed { it, anim ->
                        anim.updateAnimation()
                        if (!anim.hasAnimations()) {
                            val dur = Random.int(200, 2000).toLong()
                            anim.animate("rot", 45.0 * Random.int(-4, 4), dur, Easing.CubicInOut)
                            val offset = Vector2.uniform(-5.0, 5.0)
                            anim.animate("x", w / 8.0 * Random.int(2, 6) + offset.x, dur, Easing.QuadInOut)
                            anim.animate("y", h / 12.0 * Random.int(2, 10) + offset.y, dur, Easing.QuartInOut)
                            anim.complete()
                            anim.delay(3000 * Random.int(1, 4) - dur)
                        }
                        val pos = Vector2(anim.x, anim.y)
                        barcode.parameter("c", pos)
                        barcode.parameter("id", it * 1.0)
                        barcode.parameter("time", (it * 4) % TAU + seconds + anim.rot)
                        drawer.strokeWeight = 2.0 + ((it * 19) % 4) * 2
                        drawer.shadeStyle = barcode
                        drawer.lineSegment(pos, pos + Polar(anim.rot, 200.0).cartesian)
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
                        val y = mod(it * r + lAnims[0].rot * 10.0, h)

                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = """
                                x_stroke.rgb *= step(p_time, 0.0);
                                x_stroke.rgb *= abs(p_time) + floor(v_ftcoord.x * 2.0) / 2.0;
                            """.trimIndent()
                            parameter("time", cos(it * 20.0 % TAU + seconds * 5.0))
                        }

                        drawer.strokeWeight = 2.0 + (it % 3) * 4.0
                        drawer.lineSegment(
                            Vector2(lAnims[0].rot * 10.0, y),
                            Vector2(w2 + w2 - lAnims[0].rot * 10.0, it * r)
                        )

                        val t = abs((lAnims[frameCount % 45].rot / 90.0))
                        drawer.stroke = ColorRGBa.WHITE
                        drawer.strokeWeight = abs(10.0 * sin(t * 2.0 * PI)) + 2.0
                        drawer.lineSegment(
                            Vector2(t * w, 0.0),
                            Vector2(t * w, h)
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
                ortho(leftSide)
                leftComp.draw(this)
            }

            drawer.image(leftSide.colorBuffer(0), 200.0, 0.0)

            drawer.isolatedWithTarget(rightSide) {
                ortho(rightSide)
                rightComp.draw(this)
            }
            drawer.image(rightSide.colorBuffer(0), 500.0, 0.0)
        }
    }
}