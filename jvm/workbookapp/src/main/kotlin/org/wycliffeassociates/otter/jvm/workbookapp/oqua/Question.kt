package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import java.lang.Math.max
import java.lang.Math.min
import java.util.*

fun questionsDedup(questions: List<Question>): List<Question> {
    val filteredQuestions = mutableListOf<Question>()
    questions.forEach {question ->
        val match = filteredQuestions.find { it == question }
        if (match != null) {
            match.start = min(match.start, question.start)
            match.end = max(match.end, question.end)
        } else {
            filteredQuestions.add(question)
        }
    }
    return filteredQuestions
}

data class Question(
    var start: Int,
    var end: Int,
    val resource: Resource?
) {

    val question: String?
        get() = resource
            ?.title
            ?.textItem
            ?.text
            ?.substring(2)

    val answer: String?
        get() = resource
            ?.body
            ?.textItem
            ?.text

    var result = QuestionResult()

    override fun equals(other: Any?): Boolean =
        (other is Question)
                && (question == other.question)
                && (answer == other.answer)

    override fun hashCode(): Int = Objects.hash(start, end, question, answer)

    companion object {
        fun getQuestionsFromChunk(chunk: Chunk): Observable<Question> {
            val resourceGroup = chunk.resources.find {
                it.metadata.identifier == "tq"
            }

            return (
                    resourceGroup
                        ?.resources
                        ?.map { resource ->
                            Question(chunk.start, chunk.end, resource)
                        }
                        ?: Observable.empty()
                    )
        }
    }
}