package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import java.io.PrintWriter

class ChapterReviewHTMLRenderer {
    fun writeReviewsToFile(reviews: ChapterDraftReview, out: PrintWriter): ExportResult {
        writeHeaderHTML(reviews, out)
        writeBodyHTML(reviews, out)
        writeFooterHTML(out)

        return ExportResult.SUCCESS
    }

    private fun writeHeaderHTML(reviews: ChapterDraftReview, out: PrintWriter) {
        out.println("""
            |<!DOCTYPE html>
            |<html>
            |  <head>
            |    <title>${getTitle(reviews)}</title>
            |    <style>
            |      .piechart {
            |        display: block;
            |        border-radius: 50%;
            |        width: 100px;
            |        height: 100px;
            |        background-image: ${getPieChartBackgroundImage(reviews)};
            |      }
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
        """.trimMargin())
    }

    private fun getPieChartBackgroundImage(reviews: ChapterDraftReview): String {
        var correct = 0
        var incorrect = 0
        var invalid = 0
        var unanswered = 0

        reviews.draftReviews.forEach { review ->
            when (review.result.result) {
                ResultValue.CORRECT -> correct++
                ResultValue.INCORRECT -> incorrect++
                ResultValue.INVALID_QUESTION -> invalid++
                ResultValue.UNANSWERED -> unanswered++
            }
        }

        val correctAngle = convertAmountToAngle(correct, reviews.draftReviews.size)
        val incorrectAngle = convertAmountToAngle(incorrect, reviews.draftReviews.size) + correctAngle
        val invalidAngle = convertAmountToAngle(invalid, reviews.draftReviews.size) + incorrectAngle
        val unansweredAngle = convertAmountToAngle(unanswered, reviews.draftReviews.size) + invalidAngle

        return "conic-gradient(green 0 ${
            correctAngle
        }deg, red 0 ${
            incorrectAngle
        }deg, yellow 0 ${
            invalidAngle
        }deg, grey 0 ${
            unansweredAngle
        }deg)"
    }

    private fun convertAmountToAngle(amount: Int, outOf: Int): Int {
        return 360 * amount / outOf
    }

    private fun writeBodyHTML(reviews: ChapterDraftReview, out: PrintWriter) {
        out.println("""
            |  <body>
            |    <h1>${reviews.book} chapter ${reviews.chapter}</h1>
            |    <h2>${reviews.source} -> ${reviews.target}</h2>
            |    <div class="piechart"></div>
            |    <br>
            |    <table>
            |      <tr>
            |        <th>Verse(s)</th>
            |        <th>Result</th>
            |        <th>Question</th>
            |        <th>Answer</th>
            |        <th>Explanation</th>
            |      </tr>
        """.trimMargin())

        reviews.draftReviews.forEach { review ->
            writeReviewHTML(review, out)
        }
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