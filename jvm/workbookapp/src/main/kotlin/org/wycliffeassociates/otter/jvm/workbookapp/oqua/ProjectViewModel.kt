package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import javax.inject.Inject

class ProjectViewModel: ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()

    @Inject lateinit var exportRepo: ChapterReviewExporter

    val chapters = observableListOf<Chapter>()
    val exportProgress = SimpleDoubleProperty(1.0)
    val exportComplete = SimpleBooleanProperty(false)

    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun dock() {
        getChapters(wbDataStore.workbook)
        exportProgress.set(1.0)
        exportComplete.set(false)
        wbDataStore.activeChapterProperty.set(null)
    }

    fun undock() {
        clearChapters()
        disposables.clear()
    }

    private fun getChapters(workbook: Workbook) {
        workbook
            .target
            .chapters
            .filter { it.hasAudio() }
            .toList()
            .map { chapters ->
                chapters.sortedBy { it.sort }
            }
            .subscribe(Consumer {
                chapters.setAll(it)
            })
            .addTo(disposables)
    }

    private fun clearChapters() {
        chapters.setAll()
    }

    fun exportProject() {
        val directory = chooseDirectory(FX.messages["exportChapter"])

        if (directory != null) {
            var completed = 0
            exportProgress.set(0.0)
            exportComplete.set(false)

            chapters.forEach { chapter ->
                exportRepo.exportChapter(
                    wbDataStore.workbook,
                    chapter,
                    directory
                ).subscribe { exportResult ->
                    if (exportResult == ExportResult.SUCCESS) {
                        completed++
                        exportProgress.set(completed.toDouble() / chapters.size.toDouble())
                        if (completed == chapters.size) {
                            exportComplete.set(true)
                        }
                    }
                }.addTo(disposables)
            }
        }
    }
}