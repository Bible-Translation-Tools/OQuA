package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import javax.inject.Inject

class QuestionsRepository @Inject constructor (
    private val wbDataStore: WorkbookDataStore
) {
    fun loadQuestionsResource(): Single<List<Question>> {
        return wbDataStore
            .getSourceChapter()
            .flatMapObservable { chapter ->
                chapter.chunks
            }
            .flatMap { chunk ->
                Question.getQuestionsFromChunk(chunk)
            }
            .toList()
            .observeOnFx()
            .map { questions ->
                questionsDedup(questions).sortedBy {
                    it.end
                }
            }
    }
}