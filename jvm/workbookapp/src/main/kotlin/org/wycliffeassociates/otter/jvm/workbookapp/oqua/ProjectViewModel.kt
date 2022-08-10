package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ProjectViewModel: ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()

    val chapters = observableListOf<Chapter>()

    private val disposables = CompositeDisposable()

    fun dock() {
        getChapters(wbDataStore.workbook)
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
}