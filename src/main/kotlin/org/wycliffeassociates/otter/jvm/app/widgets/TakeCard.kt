package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.ActionEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class TakeCard(take: Take, player: IAudioPlayer) : AnchorPane() {
    private val badge = stackpane {
        style {
            backgroundColor += c(Colors["primary"])
            padding = box(10.px)
            backgroundRadius += box(0.px, 10.px, 0.px, 10.px)
        }
        label("NEW").style {
            textFill = Color.WHITE
        }
        isVisible = !take.played
    }


    init {
        setRightAnchor(badge, 0.0)
        setTopAnchor(badge, 0.0)
        vbox {
            style {
                maxWidth = 300.px
                maxHeight = 200.px
                backgroundColor += Color.WHITE
                backgroundRadius += box(10.px)
                padding = box(10.px)
            }
            hbox {
                style {
                    spacing = 10.px
                }
                label("%02d".format(take.number)) {
                    style {
                        fontSize = 20.px
                    }
                }
                label("%tD".format(take.date)) {
                    style {
                        fontSize = 12.px
                    }
                }
            }
            simpleaudioplayer(take.file, player) {
                playGraphic = MaterialIconView(MaterialIcon.PLAY_CIRCLE_OUTLINE, "30px")
                playGraphic?.apply {
                    style(true) {
                        fill = c(Colors["primary"])
                    }
                }
                pauseGraphic = MaterialIconView(MaterialIcon.PAUSE_CIRCLE_OUTLINE, "30px")
                pauseGraphic?.apply {
                    style(true) {
                        fill = c(Colors["primary"])
                    }
                }
                with(playPauseButton) {
                    style(true) {
                        backgroundColor += Color.TRANSPARENT
                    }
                    addEventHandler(ActionEvent.ACTION) {
                        if (!take.played) {
                            take.played = true
                            badge.isVisible = false
                        }
                    }
                }
            }
        }
        // Make sure badge appears on top
        badge.toFront()
    }
}

fun Pane.takecard(take: Take, player: IAudioPlayer, init: TakeCard.() -> Unit = {}): TakeCard {
    val takeCard = TakeCard(take, player)
    takeCard.init()
    add(takeCard)
    return takeCard
}