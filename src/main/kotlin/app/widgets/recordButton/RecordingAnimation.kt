package app.widgets.recordButton;

import app.MyApp
import app.MyApp.Companion.Colors
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import tornadofx.*

class RecordingAnimation : HBox() {

     var animation: Timeline? = null


    val bigCircle = circle {
        centerX = 120.0
        centerY = 120.0
        radius = 120.0;
        fill = c(Colors["lightGray"]);
    }

    val arc = arc {
        fill = c(Colors["accent"]);
        centerX = 120.0
        centerY = 120.0
        radiusX = 120.0
        radiusY = 120.0
        startAngle = -270.0
        type = ArcType.ROUND

        style {
            backgroundColor += Color.TRANSPARENT
        }

    }

    val root = pane {

        alignment = Pos.CENTER

        add(bigCircle)
        add(arc)
    }

    fun animate() {

        animation = timeline {
            keyframe(javafx.util.Duration.millis(3000.0)) {
                keyvalue(arc.lengthProperty(), -360.0)
            }
        }

    }
    fun stop() {
        animation?.pause()

    }

    init {
        with(root) {

        }
    }

}


