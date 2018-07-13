package widgets.createNewProfileButton.view

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*

class CreateNewProfileButtonStyle: Stylesheet() {
    companion object {
        val NewProfIcon by cssclass()
    }
    init {
        NewProfIcon {
            backgroundColor += c("#ffffff")
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0, Color.GRAY)

            and(hover) {
                opacity = 0.9
            }
        }
    }
}