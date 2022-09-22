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
            .subscribe { workbook ->
                addWorkbookToTCards(workbook)
            }
            .addTo(disposables)
    }

    private fun clearTCards() {
        tCards.setAll()
    }

    private fun addWorkbookToTCards(workbook: Workbook) {
        workbookHasAudio(workbook)
            .observeOnFx()
            .subscribe { hasAudio ->
                if (hasAudio) {
                    val tCard = TranslationCard.mapFromWorkbook(workbook)
                    val existingSource = tCards.find { card -> card == tCard }
                    ((existingSource?.merge(tCard)) ?: tCards.add(tCard))
                }
                tCards.sortByDescending { tCard ->
                    if (tCard.hasQuestions) {
                        tCard.projects.size
                    } else {
                        0
                    }
                }
            }
            .addTo(disposables)
    }

    private fun workbookHasAudio(workbook: Workbook): Single<Boolean> {
        return workbook
            .target
            .chapters
            .any { it.hasAudio().blockingGet() }
    }
}