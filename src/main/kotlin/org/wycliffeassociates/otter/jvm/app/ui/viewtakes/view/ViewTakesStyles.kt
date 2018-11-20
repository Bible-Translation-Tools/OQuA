package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class ViewTakesStyles : Stylesheet() {
    companion object {
        val viewTakesTitle by cssclass()
        val deleteButton by cssclass()
        val dragTarget by cssclass()
        val takeCard by cssclass()
        val badge by cssclass()
        val placeholder by cssclass()
        val headerContainer by cssclass()
        val takeFlowPane by cssclass()
        val glow by cssclass()
        val recordButton by cssclass()
        val playPauseButton by cssclass()
        fun recordIcon(size: String) = MaterialIconView(MaterialIcon.MIC_NONE, size)
    }

    init {
        viewTakesTitle {
            fontSize = 40.px
            textFill = AppTheme.colors.defaultText
        }
        takeFlowPane {
            borderColor += box(Color.LIGHTGRAY)
            borderWidth += box(0.px, 0.px, 0.px, 0.px)
            backgroundColor += Color.TRANSPARENT
            spacing = 10.px
            padding = box(20.px)
            vgap = 16.px
            hgap = 16.px
        }

        glow {
            effect = DropShadow(5.0, AppTheme.colors.appBlue)
        }

        dragTarget {
            backgroundColor += AppTheme.colors.cardBackground.deriveColor(0.0, 1.0, 1.0, 0.8)
            borderRadius += box(10.px)
            backgroundRadius += box(10.px)
            maxHeight = 100.px
            maxWidth = 250.px
            label {
                fontSize = 16.px
            }
            child("*") {
                fill = AppTheme.colors.appBlue
            }
        }

        takeCard {
            borderRadius += box(10.px)
            borderColor += box(AppTheme.colors.defaultText)
            borderWidth += box(1.px)
            backgroundColor += AppTheme.colors.cardBackground
            label {
                textFill = AppTheme.colors.defaultText
            }
            badge {
                backgroundColor += AppTheme.colors.appRed
            }
            button {
                and(deleteButton) {
                    child("*") {
                        fill = AppTheme.colors.defaultText
                    }
                }
                and(playPauseButton) {
                    child("*") {
                        fill = AppTheme.colors.appRed
                    }
                }
            }
        }

        placeholder {
            backgroundColor += AppTheme.colors.disabledCardBackground
            borderRadius += box(10.px)
            backgroundRadius += box(10.px)
            minHeight = 100.px
            minWidth = 250.px
        }

        headerContainer {
            backgroundColor += AppTheme.colors.defaultBackground
            padding = box(20.px)
            label {
                textFill = AppTheme.colors.defaultText
            }
        }

        recordButton {
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += AppTheme.colors.base
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, AppTheme.colors.dropShadow)
            unsafe("-jfx-button-type", raw("RAISED"))
            child("*") {
                fill = AppTheme.colors.appRed
            }
        }
    }
}