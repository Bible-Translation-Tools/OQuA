package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import javax.inject.Inject

/** TODO
 * Implement Export
 * Must be a function that takes a chapter and exports its saved results
 *
 * When in chapter view, save results and export the chapter
 * When in project view, export each chapter
 *
 * Must be able to grab questions from chapters using the draftReviewRepo
 * Probably will be an interface that gets injected into both project and chapter view model
 *
 * Maybe have it be a view model? Sounds wrong
 * -- Have it be a Component
 * -- Nope! Check out DraftReviewRepository. It has an injected Directory Provider
 */

class ExportRepository @Inject constructor (
    private val draftReviewRepo: DraftReviewRepository,
    private val questionsRepo: QuestionsRepository
) {
    private val disposables = CompositeDisposable()

    fun exportChapter(workbook: Workbook, chapter: Chapter, directory: File, callback:(Workbook, Chapter, Boolean) -> Unit) {
        lateinit var reviews: ChapterDraftReview
        try {
            reviews = draftReviewRepo.readDraftReviewFile(workbook, chapter)
            writeReviewsToFile(
                reviews,
                directory,
                packageCallback(callback, workbook, chapter)
            )
        } catch (_: FileNotFoundException) {
            handleMissingReview(
                workbook,
                chapter,
                directory,
                packageCallback(callback, workbook, chapter)
            )
        }
    }

    private fun packageCallback(
        callback: (Workbook, Chapter, Boolean) -> Unit,
        workbook: Workbook,
        chapter: Chapter
    ): (Boolean) -> Unit = { success ->
        callback(workbook, chapter, success)
    }

    private fun handleMissingReview(workbook: Workbook, chapter: Chapter, directory: File, callback:(Boolean) -> Unit) {
        workbook
            .source
            .chapters
            .filter { it.sort == chapter.sort }
            .subscribe { sourceChapter ->
                writeBlankReview(workbook, sourceChapter, directory, callback)
            }
            .addTo(disposables)
    }

    private fun writeBlankReview(workbook: Workbook, sourceChapter: Chapter, directory: File, callback:(Boolean) -> Unit) {
        questionsRepo.loadQuestionsResource(sourceChapter)
            .subscribe { questions ->
                val reviews = ChapterDraftReview(
                    workbook.source.language.name,
                    workbook.target.language.name,
                    workbook.target.title,
                    sourceChapter.sort,
                    questions.map { QuestionDraftReview.mapFromQuestion(it) }
                )
                writeReviewsToFile(reviews, directory, callback)
            }
            .addTo(disposables)
    }

    private fun writeReviewsToFile(reviews: ChapterDraftReview, directory: File, callback: (Boolean) -> Unit) {
        val file = getTargetFile(reviews, directory)
        file.printWriter().use { out ->
            writeHeaderHTML(reviews, out)
            reviews.draftReviews.forEach { review ->
                writeReviewHTML(review, out)
            }
            writeFooterHTML(out)
        }
        callback(true)
    }

    private fun getTargetFile(reviews: ChapterDraftReview, directory: File): File {
        val name = "${reviews.source}-${reviews.target}__${reviews.book}_${reviews.chapter}.html"
        return File("${directory.absolutePath}/$name")
    }

    private fun writeHeaderHTML(reviews: ChapterDraftReview, out: PrintWriter) {
        out.println("""
            |<!DOCTYPE html>
            |<html>
            |  <head>
            |    <title>${getTitle(reviews)}</title>
            |    <style>
            |      table, th, td {
            |        border: 1px solid black;
            |      }
            |      .dot {
            |        height: 25px;
            |        width: 25px;
            |        border-radius: 50%;
            |        border: 1px solid black;
            |        display: inline-block;
            |      }
            |    </style>
            |  </head>
            |  <body>
            |    <table>
            |      <tr>
            |        <th>Verse(s)</th>
            |        <th>Result</th>
            |        <th>Question</th>
            |        <th>Answer</th>
            |        <th>Explanation</th>
            |      </tr>
        """.trimMargin())
    }

    private fun writeReviewHTML(review: QuestionDraftReview, out: PrintWriter) {
        out.println("""
            |      <tr>
            |        <td>${getVerseLabel(review)}</td>
            |        <td>${getResultCircle(review)}</td>
            |        <td>${review.question}</td>
            |        <td>${review.answer}</td>
            |        <td>${review.result.explanation}</td>
            |      </tr>
        """.trimMargin())
    }

    private fun writeFooterHTML(out: PrintWriter) {
        out.println("""
            |    </table>
            |  </body>
            |</html>
        """.trimMargin())
    }

    private fun getTitle(reviews: ChapterDraftReview): String {
        return "${reviews.source}-${reviews.target} : ${reviews.book} ${reviews.chapter}"
    }

    private fun getVerseLabel(review: QuestionDraftReview): String {
        return if (review.start == review.end) {
            "${review.start}"
        } else {
            "${review.start} - ${review.end}"
        }
    }

    private fun getResultCircle(review: QuestionDraftReview): String {
        val color = when (review.result.result) {
            ResultValue.CORRECT -> "green"
            ResultValue.INCORRECT -> "red"
            ResultValue.INVALID_QUESTION -> "yellow"
            else -> "white"
        }
        return "<span class=\"dot\" style=\"background-color:$color\"></span>"
    }
}