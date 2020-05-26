import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
import org.openrndr.extra.compositor.*
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.palette.PaletteStudio
import kotlin.math.PI


fun main() = application {
    configure {
        width = 800
        height = 600
    }

    oliveProgram {
        val paletteStudio = PaletteStudio()
        val w = width.toDouble()
        val h = height.toDouble()
        val w2 = w / 2.0
        val h2 = h / 2.0
        val w4 = w / 4.0
        val h4 = h / 4.0
        val TAU = 2.0 * PI

        val canvas = renderTarget(width - 200, height) {
            colorBuffer()
        }

        val font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 64.0)
        val gui = GUI()

        /************** SKETCH *****************/

        val compositor = compose {
            layer {
                draw {
                    drawer.clear(ColorRGBa.PINK)
                }

            }
        }

        extend(paletteStudio)
        extend(gui)
        extend {
            drawer.isolatedWithTarget(canvas) {
                drawer.ortho(canvas)
                compositor.draw(drawer)
            }
            drawer.image(canvas.colorBuffer(0), 200.0, 0.0)
        }
    }
}