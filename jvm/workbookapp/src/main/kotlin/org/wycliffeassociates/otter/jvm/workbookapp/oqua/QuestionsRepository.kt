package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import javax.inject.Inject

class QuestionsRepository @Inject constructor() {
    fun loadQuestionsResource(chapter: Chapter): Single<List<Question>> {
        return chapter.chunks
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