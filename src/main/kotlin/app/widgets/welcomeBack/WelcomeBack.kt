package app.widgets.welcomeBack

import app.ui.imageLoader
import app.ui.styles.ButtonStyles.Companion.roundButtonLarge
import app.ui.styles.Styles.Companion.rectangleButtonAlternate
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*
import java.io.File

class WelcomeBack(ImageFile: File) : HBox() {
    private val rad = 125.0
    private val bigIcons = borderpane {
        //alignment must be outside of "style {}"
        alignment = Pos.CENTER
        style {
            backgroundColor += Color.valueOf("#FFFFFF")
        }

        top = stackpane {
            //Outer circle
            circle {
                radius = 120.0
                fill = c("#E5E5E5")
            }
            //Big Profile Icon
            //button( /* Insert "graphic = imageLoader(filePathtoUserImage)" here */)
            //filePathtoUserImage should be File type from java.io.File
            button(graphic = imageLoader(ImageFile)) {
                addClass(roundButtonLarge)
                graphic.scaleX = 1.9
                graphic.scaleY = 1.9
            }
            alignment = Pos.CENTER
        }
        //set its alignment to center it
        //alignment must be set on root, not on Widget itself
        //myBigUserIcon.root.alignment = Pos.CENTER

        center = label("Welcome Back!") {
            style {
                fontSize = 32.0.px
                FontWeight.BOLD
            }
        }
        val homeIcon = MaterialIconView(MaterialIcon.HOME,"25px")
        bottom = hbox {
            alignment = Pos.CENTER
            button("", homeIcon) {
                addClass(rectangleButtonAlternate)
                style {
                    minWidth = 175.0.px
                    homeIcon.fill = c("#FFFF")
                }
            }
        }
            //prevents from spreading out to take up whole screen when window maximized
            //note: 100 extra pixels hard coded in for space,
            // but we may change this val depending on size of home button and text
            setMaxSize(2 * rad, 3 * rad);
            setPrefSize(2 * rad + 100, 3 * rad);
            usePrefSize
        }
    }

