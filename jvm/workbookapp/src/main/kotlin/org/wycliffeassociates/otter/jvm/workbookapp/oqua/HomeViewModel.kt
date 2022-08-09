package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class HomeViewModel: ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()
    private val workbookRepo = (app as OQuAApp).dependencyGraph.injectWorkbookRepository()

    val tCards = observableListOf<TranslationCard>()

    private val disposables = CompositeDisposable()

    fun dock() {
        getTranslations()
        wbDataStore.activeWorkbookProperty.set(null)
    }

    fun undock() {
        clearTCards()
        disposables.clear()
    }

    private fun getTranslations() {
        workbookRepo
            .getProjects()
            .toObservable()
            .flatMap { Observable.fromIterable(it) }
            .toList()
            .map { workbooks ->
                workbooks.sortedBy { workbook ->
                    workbook.source.sort
                }
            }
            .subscribe { workbooks ->
                addWorkbooksToTCards(workbooks)
            }
            .addTo(disposables)
    }

    private fun clearTCards() {
        tCards.setAll()
    }

    private fun addWorkbooksToTCards(workbooks: List<Workbook>) {
        workbooks.forEach { workbook ->
            workbookHasAudio(workbook)
                .observeOnFx()
                .subscribe { hasAudio ->
                    if (hasAudio) {
                        val tCard = TranslationCard.mapFromWorkbook(workbook)
                        val existingSource = tCards.find { card -> card == tCard }
                        ((existingSource?.merge(tCard)) ?: tCards.add(tCard))
                    }
                }
                .addTo(disposables)
        }
    }

    private fun workbookHasAudio(workbook: Workbook): Single<Boolean> {
        return workbook
            .target
            .chapters
            .any { chapter ->
                chapter.hasAudio()
            }
    }
}