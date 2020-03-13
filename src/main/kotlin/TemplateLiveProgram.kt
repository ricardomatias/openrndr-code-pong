import org.openrndr.Program
import org.openrndr.application
import org.openrndr.extra.olive.Olive
import org.openrndr.extra.olive.Resources
import org.openrndr.extra.palette.PaletteStudio

/**
 *  This is a template for a live program.
 *  The first you will run this program it will create a script file at src/main/kotlin/live.kts
 *  This script file can be modified while the program is running.
 *
 *  Please refer to https://guide.openrndr.org/#/10_OPENRNDR_Extras/C03_Live_coding for more
 *  instructions on using the live coding environment.
 */

val paletteStudio = PaletteStudio()

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        extend(Olive<Program>(Resources(listOf(".yaml"))))
    }
}