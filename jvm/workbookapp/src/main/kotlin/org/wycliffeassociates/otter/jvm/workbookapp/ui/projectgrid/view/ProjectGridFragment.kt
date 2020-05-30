package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.view

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.*
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.card.Action
import org.wycliffeassociates.otter.jvm.utils.images.ImageLoader
import org.wycliffeassociates.otter.jvm.utils.images.SVGImage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.card.projectcard
import org.wycliffeassociates.otter.jvm.controls.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import tornadofx.*

class ProjectGridFragment : Fragment() {

    private val viewModel: ProjectGridViewModel by inject()
    private val noProjectsProperty: ReadOnlyBooleanProperty

    init {
        importStylesheet<ProjectGridStyles>()
        importStylesheet<DefaultStyles>()
        // Setup property bindings to bind to empty property
        // https://stackoverflow.com/questions/21612969/is-it-possible-to-bind-the-non-empty-state-of-
        // an-observablelist-inside-an-object
        val listProperty = SimpleListProperty<Workbook>()
        listProperty.bind(SimpleObjectProperty(viewModel.projects))
        noProjectsProperty = listProperty.emptyProperty()
        initializeProgressDialogs()
    }

    override val root = anchorpane {

        importStylesheet(javaClass.getResource("/css/root.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/button.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/projectcard.css").toExternalForm())

        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        style {
            unsafe("-fx-background-color", "#F7FAFF")
        }

        datagrid(viewModel.projects) {
            anchorpaneConstraints {
                topAnchor = 0
                rightAnchor = 0
                bottomAnchor = 0
                leftAnchor = 0
            }
            addClass(ProjectGridStyles.projectsGrid)
            cellWidthProperty.set(176.0)
            cellHeightProperty.set(224.0)
            cellCache { item ->
                projectcard {
                    titleTextProperty().set(item.target.title)
                    slugTextProperty().set(item.target.slug)
                    actionTextProperty().set(messages["openProject"])
                    languageTextProperty().set(item.target.resourceMetadata.language.name)
                    coverArtProperty().set(item.coverArtAccessor.getArtwork())
                    setOnAction {
                        viewModel.selectProject(item)
                    }
                    addActions(
                        Action(
                            text = messages["delete"],
                            iconCode = "gmi-delete",
                            onClicked = {
                                viewModel.deleteProject(item)
                            }
                        )
                    )
                }
            }
        }

        vbox {
            anchorpaneConstraints {
                topAnchor = 0
                leftAnchor = 0
                bottomAnchor = 0
                rightAnchor = 0
            }
            alignment = Pos.CENTER
            vgrow = Priority.ALWAYS
            label(messages["noProjects"]) {
                addClass(ProjectGridStyles.noProjectsLabel)
            }
            label(messages["noProjectsSubtitle"]) {
                addClass(ProjectGridStyles.tryCreatingLabel)
            }

            visibleProperty().bind(noProjectsProperty)
            managedProperty().bind(visibleProperty())
        }

        add(JFXButton("", MaterialIconView(MaterialIcon.ADD, "25px")).apply {
            addClass(ProjectGridStyles.addProjectButton)
            isDisableVisualFocus = true
            anchorpaneConstraints {
                bottomAnchor = 25
                rightAnchor = 25
            }
            action { viewModel.createProject() }
        })
    }

    init {
        with(root) {
            add(ImageLoader.load(
                ClassLoader.getSystemResourceAsStream("images/project_home_arrow.svg"),
                ImageLoader.Format.SVG
            ).apply {
                if (this is SVGImage) preserveAspect = false
                root.widthProperty().onChange {
                    anchorpaneConstraints { leftAnchor = it / 2.0 }
                }
                root.heightProperty().onChange {
                    anchorpaneConstraints { topAnchor = it / 2.0 + 75.0 }
                }
                anchorpaneConstraints {
                    rightAnchor = 110
                    bottomAnchor = 10
                }

                visibleProperty().bind(noProjectsProperty)
                managedProperty().bind(visibleProperty())
            })
        }
    }

    override fun onDock() {
        viewModel.loadProjects()
        viewModel.clearSelectedProject()
    }

    private fun initializeProgressDialogs() {
        val deletingProjectDialog = progressdialog {
            text = messages["deletingProject"]
            graphic = ProjectGridStyles.deleteIcon("60px")
            root.addClass(AppStyles.progressDialog)
        }
        viewModel.showDeleteDialogProperty.onChange {
            Platform.runLater { if (it) deletingProjectDialog.open() else deletingProjectDialog.close() }
        }
    }
}