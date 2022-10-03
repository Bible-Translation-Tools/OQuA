package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Test
import java.io.PrintWriter
import java.io.StringWriter

class ChapterReviewHTMLRendererTest {

    private val htmlRenderer = ChapterReviewHTMLRenderer()

    private fun header(correct: Int, incorrect: Int, invalid: Int) = """
            |<!DOCTYPE html>
            |<html>
            |  <head>
            |    <title>Source-Target : Book 123</title>
            |    <style>
            |      .piechart {
            |        display: block;
            |        border-radius: 50%;
            |        width: 100px;
            |        height: 100px;
            |        background-image: conic-gradient(green 0 ${
                correct
            }deg, red 0 ${
                correct + incorrect
            }deg, yellow 0 ${
                correct + incorrect + invalid
            }deg, grey 0 360deg);
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
        """.trimMargin()

    private val body = """
            |  <body>
            |    <h1>Book chapter 123</h1>
            |    <h2>Source -> Target</h2>
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
        """.trimMargin()

    private val footer = """
            |    </table>
            |  </body>
            |</html>
        """.trimMargin()

    @Test
    fun `print header and footer`() {
        val reviews = mock<ChapterDraftReview> {
            on { source } doReturn "Source"
            on { target } doReturn "Target"
            on { book } doReturn "Book"
            on { chapter } doReturn 123
            on { draftReviews } doReturn listOf(
                QuestionDraftReview(
                    "q1",
                    "a1",
                    1,
                    1,
                    QuestionResult(explanation = "Words")
                )
            )
        }

        val output = StringWriter()
        val out = PrintWriter(output)

        htmlRenderer.writeReviewsToFile(reviews, out)

        Assert.assertEquals("""
            |${header(0, 0, 0)}
            |$body
            |      <tr>
            |        <td>1</td>
            |        <td><span class="dot" style="background-color:white"></span></td>
            |        <td>q1</td>
            |        <td>a1</td>
            |        <td>Words</td>
            |      </tr>
            |$footer
            |""".trimMargin(),
            output.toString()
        )
    }

    @Test
    fun `print proportions`() {
        val reviews = mock<ChapterDraftReview> {
            on { source } doReturn "Source"
            on { target } doReturn "Target"
            on { book } doReturn "Book"
            on { chapter } doReturn 123
            on { draftReviews } doReturn listOf(
                QuestionDraftReview(
                    "q1",
                    "a1",
                    1,
                    1,
                    QuestionResult(ResultValue.APPROVED, "")
                ),
                QuestionDraftReview(
                    "q2",
                    "a2",
                    2,
                    2,
                    QuestionResult(ResultValue.APPROVED, "")
                ),
                QuestionDraftReview(
                    "q3",
                    "a3",
                    3,
                    3,
                    QuestionResult(ResultValue.NEEDS_WORK, "")
                ),
                QuestionDraftReview(
                    "q4",
                    "a4",
                    4,
                    4,
                    QuestionResult(ResultValue.INVALID_QUESTION, "")
                )
            )
        }

        val output = StringWriter()
        val out = PrintWriter(output)

        htmlRenderer.writeReviewsToFile(reviews, out)

        Assert.assertEquals("""
            |${ header(180, 90, 90) }
            |$body
            |      <tr>
            |        <td>1</td>
            |        <td><span class="dot" style="background-color:green"></span></td>
            |        <td>q1</td>
            |        <td>a1</td>
            |        <td></td>
            |      </tr>
            |      <tr>
            |        <td>2</td>
            |        <td><span class="dot" style="background-color:green"></span></td>
            |        <td>q2</td>
            |        <td>a2</td>
            |        <td></td>
            |      </tr>
            |      <tr>
            |        <td>3</td>
            |        <td><span class="dot" style="background-color:red"></span></td>
            |        <td>q3</td>
            |        <td>a3</td>
            |        <td></td>
            |      </tr>
            |      <tr>
            |        <td>4</td>
            |        <td><span class="dot" style="background-color:yellow"></span></td>
            |        <td>q4</td>
            |        <td>a4</td>
            |        <td></td>
            |      </tr>
            |$footer
            |""".trimMargin(), output.toString())
    }
}
