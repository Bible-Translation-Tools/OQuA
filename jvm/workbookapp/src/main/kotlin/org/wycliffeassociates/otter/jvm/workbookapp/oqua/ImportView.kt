/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import tornadofx.*
import java.text.MessageFormat

class ImportView : View() {
    private val logger = LoggerFactory.getLogger(ImportView::class.java)

    private val viewModel: ImportViewModel by inject()

    override val root = vbox {
        addClass("app-drawer__content")

        scrollpane {
            addClass("app-drawer__scroll-pane")
            fitToParentHeight()

            vbox {
                isFitToWidth = true
                isFitToHeight = true

                addClass("app-drawer-container")

                hbox {
                    label(messages["importFiles"]).apply {
                        addClass("app-drawer__title")
                    }
                    region { hgrow = Priority.ALWAYS }
                }

                vbox {
                    addClass("app-drawer__section")
                    label(messages["dragAndDrop"]).apply {
                        addClass("app-drawer__subtitle")
                    }
                }

                vbox {
                    addClass("app-drawer__drag-drop-area")

                    vgrow = Priority.ALWAYS

                    label {
                        addClass("app-drawer__drag-drop-area__icon")
                        graphic = FontIcon(MaterialDesign.MDI_FILE_MULTIPLE)
                    }

                    label(messages["dragToImport"]) {
                        fitToParentWidth()
                        addClass("app-drawer__text--centered")
                    }

                    button(messages["browseFiles"]) {
                        addClass(
                            "btn",
                            "btn--primary"
                        )
                        tooltip {
                            textProperty().bind(this@button.textProperty())
                        }
                        graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                        action {
                            viewModel.onChooseFile()
                        }
                    }

                    onDragOver = onDragOverHandler()
                    onDragDropped = onDragDroppedHandler()
                }
            }
        }
    }

    init {
        tryImportStylesheet(resources["/css/app-drawer.css"])
        tryImportStylesheet(resources["/css/confirm-dialog.css"])

        initImportDialog()
        initSuccessDialog()
        initErrorDialog()
        createSnackBar()
    }

    override fun onDock() {
        super.onDock()
    }

    private fun initImportDialog() {
        val importDialog = confirmdialog {
            titleTextProperty.bind(
                viewModel.importedProjectTitleProperty.stringBinding {
                    it?.let {
                        MessageFormat.format(
                            messages["importProjectTitle"],
                            messages["import"],
                            it
                        )
                    } ?: messages["importResource"]
                }
            )
            messageTextProperty.set(messages["importResourceMessage"])
            backgroundImageFileProperty.bind(viewModel.importedProjectCoverProperty)
            progressTitleProperty.set(messages["pleaseWait"])
            showProgressBarProperty.set(true)
        }

        viewModel.showImportDialogProperty.onChange {
            Platform.runLater { if (it) importDialog.open() else importDialog.close() }
        }
    }

    private fun initSuccessDialog() {
        val successDialog = confirmdialog {
            titleTextProperty.bind(
                viewModel.importedProjectTitleProperty.stringBinding {
                    it?.let {
                        MessageFormat.format(
                            messages["importProjectTitle"],
                            messages["import"],
                            it
                        )
                    } ?: messages["importResource"]
                }
            )
            messageTextProperty.set(messages["importResourceSuccessMessage"])
            backgroundImageFileProperty.bind(viewModel.importedProjectCoverProperty)

            cancelButtonTextProperty.set(messages["close"])
            onCloseAction { viewModel.showImportSuccessDialogProperty.set(false) }
            onCancelAction { viewModel.showImportSuccessDialogProperty.set(false) }
        }

        viewModel.showImportSuccessDialogProperty.onChange {
            Platform.runLater { if (it) successDialog.open() else successDialog.close() }
        }
    }

    private fun initErrorDialog() {
        val errorDialog = confirmdialog {
            titleTextProperty.bind(
                viewModel.importedProjectTitleProperty.stringBinding {
                    it?.let {
                        MessageFormat.format(
                            messages["importProjectTitle"],
                            messages["import"],
                            it
                        )
                    } ?: messages["importResource"]
                }
            )
            messageTextProperty.bind(viewModel.importErrorMessage.stringBinding {
                it ?: messages["importResourceFailMessage"]
            })
            backgroundImageFileProperty.bind(viewModel.importedProjectCoverProperty)

            cancelButtonTextProperty.set(messages["close"])
            onCloseAction { viewModel.showImportErrorDialogProperty.set(false) }
            onCancelAction { viewModel.showImportErrorDialogProperty.set(false) }
        }

        viewModel.showImportErrorDialogProperty.onChange {
            Platform.runLater { if (it) errorDialog.open() else errorDialog.close() }
        }
    }

    private fun onDragOverHandler(): EventHandler<DragEvent> {
        return EventHandler {
            if (it.gestureSource != this && it.dragboard.hasFiles()) {
                it.acceptTransferModes(TransferMode.COPY)
            }
            it.consume()
        }
    }

    private fun onDragDroppedHandler(): EventHandler<DragEvent> {
        return EventHandler {
            var success = false
            if (it.dragboard.hasFiles()) {
                viewModel.onDropFile(it.dragboard.files)
                success = true
            }
            it.isDropCompleted = success
            it.consume()
        }
    }

    private fun createSnackBar() {
        viewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating add files snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                SnackbarHandler.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        JFXSnackbarLayout(pluginErrorMessage),
                        Duration.millis(5000.0),
                        null
                    )
                )
            }
    }
}
