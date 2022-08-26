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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectImporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import tornadofx.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class ImportViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(ImportViewModel::class.java)

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var importRcProvider: Provider<ImportResourceContainer>
    @Inject lateinit var importProvider: Provider<ProjectImporter>

    val showImportProperty = SimpleBooleanProperty(false)
    val showImportSuccessDialogProperty = SimpleBooleanProperty(false)
    val showImportErrorDialogProperty = SimpleBooleanProperty(false)
    val importErrorMessage = SimpleStringProperty(null)
    val importedProjectTitleProperty = SimpleStringProperty()

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun dock() {
        showImportProperty.set(false)
        showImportSuccessDialogProperty.set(false)
        showImportErrorDialogProperty.set(false)
    }

    fun onDropFile(files: List<File>) {
        if (validateImportFile(files)) {
            importResourceContainer(files.first())
        }
    }

    fun onChooseFile() {
        val file = chooseFile(
            FX.messages["importResourceFromZip"],
            arrayOf(
                FileChooser.ExtensionFilter(
                    messages["oratureFileTypes"],
                    *OratureFileFormat.extensionList.map { "*.$it" }.toTypedArray()
                )
            ),
            mode = FileChooserMode.Single
        ).firstOrNull()
        file?.let {
            setProjectInfo(file)
            importResourceContainer(file)
        }
    }

    private fun importResourceContainer(file: File) {
        showImportProperty.set(true)
        showImportSuccessDialogProperty.set(false)
        showImportErrorDialogProperty.set(false)

        importRcProvider.get()
            .import(file)
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in importing resource container $file", e)
            }
            .doFinally {
                importedProjectTitleProperty.set(null)
            }
            .subscribe { result: ImportResult ->
                when (result) {
                    ImportResult.SUCCESS -> {
                        showImportProperty.value = false
                        showImportSuccessDialogProperty.value = true
                    }
                    ImportResult.DEPENDENCY_ERROR -> {
                        importErrorMessage.set(messages["importErrorDependencyExists"])
                        showImportProperty.value = false
                        showImportErrorDialogProperty.value = true
                    }
                    else -> {
                        showImportProperty.value = false
                        showImportErrorDialogProperty.value = true
                    }
                }
                showImportProperty.value = false
            }
    }

    private fun validateImportFile(files: List<File>): Boolean {
        return when {
            files.size > 1 -> {
                snackBarObservable.onNext(messages["importMultipleError"])
                false
            }
            files.first().isDirectory -> {
                snackBarObservable.onNext(messages["importDirectoryError"])
                false
            }
            files.first().extension !in OratureFileFormat.extensionList -> {
                snackBarObservable.onNext(messages["importInvalidFileError"])
                false
            }
            else -> true
        }
    }

    private fun setProjectInfo(rc: File) {
        try {
            val project = ResourceContainer.load(rc, true).use { it.project() }
            project?.let {
                importProvider.get()
                    .getSourceMetadata(rc)
                    .doOnError {
                        logger.debug("Error in getSourceMetadata: $rc")
                    }
                    .onErrorComplete()
                    .subscribe { resourceMetadata ->
                        resourceMetadata?.let {
                            importedProjectTitleProperty.set(project.title)
                        }
                    }
            }
        } catch (e: Exception) {
            logger.error("Error in getting info from resource container $rc", e)
        }
    }
}
