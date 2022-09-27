package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TestApp
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChapterReviewExporterTest {
    private lateinit var testApp: TestApp

    private lateinit var dir: File

    /** Workbook and chapters for successful finds */
    private val workbook = mock<Workbook> {}
    private val chapter = mock<Chapter> {}

    /** Workbook and chapter for unsuccessful finds */
    private val failedChapter = mock<Chapter> {
        on { sort } doReturn 0
    }

    private val failedSourceLanguage = mock<Language> {
        on { name } doReturn "Missing Source"
    }
    private val failedTargetLanguage = mock<Language> {
        on { name } doReturn "Missing Target"
    }

    private val failedSource = mock<Book> {
        on { language } doReturn failedSourceLanguage
        on { chapters } doReturn Observable.fromCallable {
            return@fromCallable failedChapter
        }
    }
    private val failedTarget = mock<Book> {
        on { language } doReturn failedTargetLanguage
        on { title } doReturn "Missing Book"
    }

    private val failedWorkbook = mock<Workbook> {
        on { source } doReturn failedSource
        on { target } doReturn failedTarget
    }



    @Before
    fun setup() {
        testApp = TestApp()
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }

        dir = File("testDir")
        dir.mkdirs()
    }

    @After
    fun teardown() {
        dir.deleteRecursively()

        FxToolkit.hideStage()
        FxToolkit.cleanupStages()
        FxToolkit.cleanupApplication(testApp)
    }

    @Test
    fun `creates file in directory with proper name`() {
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
                    QuestionResult(explanation = "words")
                )
            )
        }

        val draftReviewRepo = mock<DraftReviewRepository> {
            on { readDraftReviewFile(any(), any()) } doReturn reviews
        }
        val questionsRepo = mock<QuestionsRepository> {
        }

        val renderer = MockRenderer()

        val exporter = ChapterReviewExporter(draftReviewRepo, questionsRepo)
        val result = exporter.exportChapter(workbook, chapter, dir, renderer).blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        val timestamp = LocalDateTime.now().format(formatter)
        Assert.assertTrue(File("testDir/Source-Target__Book_123__${timestamp}.html").exists())
    }

    @Test
    fun `writes correct data to file`() {
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
                    QuestionResult(explanation = "words")
                )
            )
        }

        val draftReviewRepo = mock<DraftReviewRepository> {
            on { readDraftReviewFile(any(), any()) } doReturn reviews
        }
        val questionsRepo = mock<QuestionsRepository> {
        }

        val renderer = MockRenderer()

        val exporter = ChapterReviewExporter(draftReviewRepo, questionsRepo)
        exporter.exportChapter(workbook, chapter, dir, renderer).blockingGet()

        val file = File("testDir/Source-Target__Book_123.html")
        val lines = file.readLines()

        Assert.assertArrayEquals(
            arrayOf(
                "Source -> Target",
                "Book 123",
                "q1 : a1 :: 1 - 1 = UNANSWERED : words"
            ),
            lines.toTypedArray()
        )
    }

    @Test
    fun `handles missing draft review`() {
        val draftReviewRepo = mock<DraftReviewRepository> {
            on { readDraftReviewFile(any(), any()) } doAnswer {
                _ -> throw FileNotFoundException("Mock File Not Found")
            }
        }
        val questionsRepo = mock<QuestionsRepository> {
            on { loadQuestionsResource(any()) } doReturn Single.fromCallable {
                return@fromCallable listOf(
                    mock<Question> {
                        on { question } doReturn "missing q1"
                        on { answer } doReturn "missing a1"
                        on { start } doReturn 0
                        on { end } doReturn 0
                        on { result } doReturn QuestionResult(
                            ResultValue.INVALID_QUESTION,
                            "missing words"
                        )
                    }
                )
            }
        }

        val renderer = MockRenderer()

        val exporter = ChapterReviewExporter(draftReviewRepo, questionsRepo)
        val result = exporter.exportChapter(failedWorkbook, failedChapter, dir, renderer).blockingGet()

        val file = File("testDir/Missing Source-Missing Target__Missing Book_0.html")
        val lines = file.readLines()

        Assert.assertEquals(ExportResult.SUCCESS, result)
        Assert.assertTrue(File("testDir/Missing Source-Missing Target__Missing Book_0.html").exists())
        Assert.assertArrayEquals(
            arrayOf(
                "Missing Source -> Missing Target",
                "Missing Book 0",
                "missing q1 : missing a1 :: 0 - 0 = INVALID_QUESTION : missing words"
            ),
            lines.toTypedArray()
        )
    }
}

class MockRenderer: IChapterReviewRenderer {
    override fun writeReviewsToFile(reviews: ChapterDraftReview, out: PrintWriter): ExportResult {
        out.println("${reviews.source} -> ${reviews.target}")
        out.println("${reviews.book} ${reviews.chapter}")
        reviews.draftReviews.forEach { review ->
            out.println(
                "${
                    review.question
                } : ${
                    review.answer
                } :: ${
                    review.start
                } - ${
                    review.end
                } = ${
                    review.result.result
                } : ${
                    review.result.explanation
                }"
            )
        }
        return ExportResult.SUCCESS
    }
}
