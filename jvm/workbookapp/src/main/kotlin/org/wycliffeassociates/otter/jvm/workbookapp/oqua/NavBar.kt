package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import tornadofx.*

class NavBar : View() {
    private val viewModel: NavBarViewModel by inject()

    override val root = borderpane {

        center = hbox(5) {
            addClass("oqua-nav-bar")

            button("OQuA") {
                action {
                    workspace.dock(find<HomeView>())
                }
            }
            button(viewModel.projectTitleProperty){
                visibleWhen(viewModel.projectTitleProperty.isNotNull)
                action {
                    workspace.dock(find<ProjectView>())
                }
            }
            button(viewModel.chapterTitleProperty) {
                visibleWhen(viewModel.chapterTitleProperty.isNotNull)
            }
        }

        right = hbox(5) {
            addClass("oqua-nav-bar")
            button("Import Resources") {
                action {
                    workspace.dock(find<ImportView>())
                }
            }
        }
    }
}