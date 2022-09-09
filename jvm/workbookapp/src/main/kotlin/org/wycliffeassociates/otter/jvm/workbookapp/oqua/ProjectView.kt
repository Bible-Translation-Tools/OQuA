package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.scene.layout.Priority
import tornadofx.*

class ProjectView : View() {
    private val viewModel: ProjectViewModel by inject()

    override fun onDock() {
        viewModel.dock()
    }

    override fun onUndock() {
        viewModel.undock()
    }

    override val root = vbox {
        borderpane {
            left = button("Export") {
                action {
                    viewModel.exportProject()
                }
            }
            right = vbox {
                progressbar {
                    progressProperty().bind(viewModel.exportProgress)
                    visibleWhen(progressProperty().isNotEqualTo(1.0, Double.MIN_VALUE))
                    managedWhen(visibleProperty())
                }
                text("Export Complete") {
                    visibleWhen(viewModel.exportComplete)
                    managedWhen(visibleProperty())
                }
            }
        }
        listview(viewModel.chapters) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            cellFragment(ChapterListCellFragment::class)
        }
    }
}