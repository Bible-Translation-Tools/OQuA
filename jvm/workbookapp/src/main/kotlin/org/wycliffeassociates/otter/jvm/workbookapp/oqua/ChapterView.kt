package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import tornadofx.*

class ChapterView : View() {
    private val viewModel: ChapterViewModel by inject()

    override fun onDock() {
        super.onDock()
        viewModel.dock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }

    override val root = vbox {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        hbox(5) {
            button("Export") {
                action {
                    viewModel.exportChapter()
                }
            }
            text("Export Complete") {
                visibleWhen(viewModel.exportComplete)
                managedWhen(visibleProperty())
            }
        }
        simpleaudioplayer {
            hgrow = Priority.ALWAYS
            playerProperty.bind(viewModel.audioPlayerProperty)
            hiddenWhen(viewModel.hasAllMarkers)
            managedProperty().bind(visibleProperty())
        }
        listview(viewModel.questions) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            cellFragment(TQListCellFragment::class)
        }
    }
}