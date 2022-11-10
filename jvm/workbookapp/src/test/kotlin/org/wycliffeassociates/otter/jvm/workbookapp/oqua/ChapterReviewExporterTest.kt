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
import java.time.format.FormatStyle


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
        val directory = File("testDir")

        Assert.assertEquals(0, directory.listFiles()?.size ?: 0)
        val time = LocalDateTime.now()
        val result = exporter.exportChapter(workbook, chapter, time, dir, renderer).blockingGet()

        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        val timestamp = time.format(formatter)
        Assert.assertEquals(ExportResult.SUCCESS, result)
        Assert.assertEquals(1, directory.listFiles()!!.size)
        Assert.assertEquals(
            "Source-Target__Book_123__$timestamp.html",
            directory.listFiles()!![0].name
        )
    }

    @Test
    fun `writes data to the file`() {
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
        val time = LocalDateTime.now()
        exporter.exportChapter(workbook, chapter, time, dir, renderer).blockingGet()

        val file = File("testDir").listFiles()!![0]
        val lines = file.readLines()

        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val timestamp = formatter.format(time)
        Assert.assertArrayEquals(
            arrayOf(
                "Source -> Target - $timestamp",
                "Book 123",
                "q1 : a1 :: 1 - 1 = UNANSWERED : words"
            ),
            lines.toTypedArray()
        )
    }

    @Test
    fun `handles missing draft review and still creates file`() {
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
        val time = LocalDateTime.now()
        val result = exporter.exportChapter(failedWorkbook, failedChapter, time, dir, renderer).blockingGet()

        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        var timestamp = time.format(formatter)
        val file = File("testDir/Missing Source-Missing Target__Missing Book_0__$timestamp.html")
        val lines = file.readLines()

        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        timestamp = time.format(formatter)
        Assert.assertEquals(ExportResult.SUCCESS, result)
        Assert.assertArrayEquals(
            arrayOf(
                "Missing Source -> Missing Target - $timestamp",
                "Missing Book 0",
                "missing q1 : missing a1 :: 0 - 0 = INVALID_QUESTION : missing words"
            ),
            lines.toTypedArray()
        )
    }
}

class MockRenderer: IChapterReviewRenderer {
    override fun writeReviewsToFile(
        reviews: ChapterDraftReview,
        time: LocalDateTime,
        out: PrintWriter
    ): ExportResult {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val timestamp = formatter.format(time)

        out.println("${reviews.source} -> ${reviews.target} - $timestamp")
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
