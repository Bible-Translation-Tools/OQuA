package org.wycliffeassociates.otter.jvm.workbookapp.oqua

enum class ResultValue {
    APPROVED,
    NEEDS_WORK,
    INVALID_QUESTION,
    UNANSWERED
}

data class QuestionResult(
    var result: ResultValue = ResultValue.UNANSWERED,
    var explanation: String = ""
)