import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.*
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
import org.openrndr.extra.timeoperators.LFO
import org.openrndr.extra.timeoperators.TimeOperators
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mod
import org.openrndr.math.transforms.transform
import org.openrndr.shape.LineSegment
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

        fun rotate2D(v: Vector2, degrees: Double, origin: Vector2 = Vector2.ZERO): Vector2 {
            val p = v - origin
            val a = Math.toRadians(degrees)

            val w = Vector2(
                    p.x * cos(a) - p.y * sin(a),
                    p.y * cos(a) + p.x * sin(a)
            )

            return w + origin
        }

        fun LineSegment.rotate(theta: Double): LineSegment {
            val mid = end.mix(start, 0.5)

            return LineSegment(
                    rotate2D(start, theta, mid),
                    rotate2D(end, theta, mid)
            )
        }

        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 64.0)
        val gui = GUI()

        /************** SKETCH *****************/

        class LAnim : Animatable() {
            var rot = 0.0
            var x = 0.0
            var y = -50.0
            var w = 0.0
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
                            val offset = Vector2.uniform(-5.0, 5.0)

                            anim.delay(3000 * Random.int(1, 4) - dur - dur / 4 - dur / 6)
                            anim.animate("w", 1.0, dur / 4)
                            anim.complete()
                            anim.animate("w", 0.0, dur, Easing.QuadOut)
                            anim.delay(dur / 6)
                            anim.animate("rot", 45.0 * Random.int(-4, 4), dur, Easing.CubicInOut)
                            anim.animate("x", w / 8.0 * Random.int(2, 6) + offset.x, dur, Easing.QuadInOut)
                            anim.animate("y", h / 12.0 * Random.int(2, 10) + offset.y, dur, Easing.QuartInOut)
                            anim.complete()
                        }
                        val pos = Vector2(anim.x, anim.y) + Random.vector2(-5.0, 5.0) * anim.w
                        barcode.parameter("c", pos)
                        barcode.parameter("id", it * 1.0)
                        barcode.parameter("time", (it * 4) % TAU + seconds + anim.rot)
                        drawer.strokeWeight = 2.0 + ((it * 19) % 4) * 2.0 * (1 - anim.w)
                        drawer.shadeStyle = barcode
                        drawer.lineSegment(pos, pos + Polar(anim.rot, 200.0).cartesian)
                    }
                }
            }
        }

        val lfo = LFO()

        val rAnims = List(11) { LAnim() }
        val rAnims1 = List(11) { LAnim() }
        val rAnims2 = List(11) { LAnim() }

        fun drawRight(anims: List<LAnim>, maxDur: Int) {
            val m = 40.0
            val rots = listOf(45.0, 90.0, 135.0, 180.0)

            val rotSeed = Random.int0(rots.size)
            val dur = Random.int(200, maxDur).toLong()

            for ((idx: Int, rAnim: LAnim) in anims.withIndex()) {
                rAnim.updateAnimation()

                if (!rAnim.hasAnimations()) {
                    rAnim.animate("rot", rots[mod(idx + rotSeed, rots.size)], dur, Easing.QuartInOut)
                }

                val r = (h - m) / 10.0
                val y = mod(idx * r, h)

                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = """
                                x_stroke.rgb *= step(p_time, 0.0);
                                x_stroke.rgb *= abs(p_time) + floor(v_ftcoord.x * 2.0) / 2.0;
                            """.trimIndent()
                    parameter("time", cos(idx * 20.0 % TAU + seconds * 1.0))
                }

                drawer.strokeWeight = 2.0 + (idx % 3) * 4.0

                val line = LineSegment(
                        Vector2(0.0, y),
                        Vector2(w, y)
                ).rotate(rAnim.rot)

                drawer.lineSegment(line)

                val t = abs((rAnim.rot / PI * 0.5))
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = abs(10.0 * sin(t * 2.0 * PI)) + 2.0
            }
        }

        val rightComp = compose {
            layer {
                draw {
                    drawer.clear(rgb(0.0))

                    drawer.stroke = ColorRGBa.WHITE

                    Random.resetState()
                    drawRight(rAnims, 4000)

                    drawer.isolated {
                        drawer.view = transform {
                            translate(w2, 0.0)
                            rotate(45.0)
                            scale(0.5)
                        }
                        drawRight(rAnims1, 1000)
                    }

                    drawer.isolated {
                        drawer.view = transform {
                            translate(0.0, h2)
                            rotate(315.0)
                            scale(0.75)
                        }
                        drawRight(rAnims2, 2000)
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
        extend(TimeOperators()) {
            track(lfo)
        }
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