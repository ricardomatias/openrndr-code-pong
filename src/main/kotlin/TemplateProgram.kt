import org.openrndr.application
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.gui.GUI
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

        val fx = GaussianBloom()

        val compositor = compose {
            layer {
                draw {
                    drawer.isolatedWithTarget(rt) {
                        drawer.stroke = null
                        drawer.fill = paletteStudio.background.opacify(0.09)
                        drawer.rectangle(0.0, 0.0, w, h)
//                        if (frameCount % (60 * 2) == 0) drawer.background(paletteStudio.background)
                        drawer.translate(w2, h2)
                        drawer.scale(cos(seconds * 0.6) * 0.5 + 0.6)
                        drawer.rotate(cos(seconds * 0.5) * 180.0)

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
                                radius += cos(seconds * 0.6) * 200.0
                            }
                        }

                        for ((idx, e) in c.exploded.withIndex()) {
                            drawer.stroke = paletteStudio.colors2[idx % paletteStudio.colors2.size]
                            drawer.strokeWeight = 2.0 + 4.0 * (cos((TAU / 5.0) + seconds * 0.6) * 0.5 + 0.5)

                            val s = sin((seconds * 0.6) + (idx * TAU)) * 0.5 + 0.5
                            drawer.contour(e.sub(0.0, s))
                        }
                    }

                    drawer.image(rt.colorBuffer(0))

                    val shadow = rt.colorBuffer(0).shadow
                    shadow.download()
                    for (x in 50 until width - 50 step 50) {
                        drawer.fill = shadow[x, height - 50]
                        drawer.rectangle(x.toDouble(), height - 100.0, 20.0, 80.0)
                    }
                }
            }
            fx.sigma = 3.0 // 10.0 * mouse.position.x / width // don't see it changing?
            fx.window = 2  // 10 * mouse.position.y / height  // don't see it changing?
            post(fx)
        }

        extend(paletteStudio)
//        extend(gui)
        extend {
            compositor.draw(drawer)
        }
    }
}