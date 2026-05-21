package pl.zosiaqucz.technologieinternetowe

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Technologie_internetowe_aplikacja",
    ) {
        App()
    }
}