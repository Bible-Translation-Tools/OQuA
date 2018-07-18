package widgets.RecordButton

import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.shape.Circle
import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class DotsAnimation: HBox() {


    lateinit var cir1 :Circle
    lateinit var cir2 :Circle
    lateinit var cir3 :Circle

    init{
        spacing= 25.0
        alignment = Pos.CENTER
         cir1 = circle{
            //hide()
            centerX = 10.0
            centerY = 20.0
            radius = 20.0
            fill = c("#EDEDED")
        }

         cir2= circle{
             //hide()

            isVisible = true
            centerX = 100.0
            centerY = 20.0
            radius = 20.0
            fill = c("#EDEDED")
        }

         cir3=circle{
            // hide()
            isVisible = true
            centerX = 200.0
            centerY = 20.0
            radius = 20.0
            fill = c("#EDEDED")
        }



    }

    fun showCircles() {
        var timer = Timer()
        timer.schedule(timerTask { cir1.fill= c("#CC4141") }, 0)
        timer.schedule(timerTask { cir2.fill=c("#CC4141") }, 1000)
        timer.schedule(timerTask { cir3.fill=c("#CC4141") }, 2000)
    }



}