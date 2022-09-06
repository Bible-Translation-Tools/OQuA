package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Test
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.workbook.*
import org.junit.After
import org.junit.Before
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TestApp
import java.io.File
import java.io.PrintWriter

class ChapterReviewExporterTest {
    private lateinit var testApp: TestApp

    private lateinit var dir: File

    private val sourceLanguage = mock<Language> {
        on { name } doReturn "Source"
    }
    private val source = mock<Book> {
        on { language } doReturn sourceLanguage
    }

    private val targetLanguage = mock<Language> {
        on { name } doReturn "Target"
    }
    private val target = mock<Book> {
        on { language } doReturn targetLanguage
        on { title } doReturn "Book"
    }

    private val workbook = mock<Workbook> {
        on { source } doReturn source
        on { target } doReturn target
    }
    private val chapter = mock<Chapter> {
        on { sort } doReturn 123
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
                    QuestionResult(explanation = "Words")
                )
            )
        }

        val draftReviewRepo = mock<DraftReviewRepository> {
            on { readDraftReviewFile(workbook, chapter) } doReturn reviews
        }
        val questionsRepo = mock<QuestionsRepository> {
        }

        val renderer = MockRenderer()

        val exporter = ChapterReviewExporter(draftReviewRepo, questionsRepo)
        val result = exporter.exportChapter(workbook, chapter, dir, renderer).blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)
        Assert.assertTrue(File("testDir/Source-Target__Book_123.html").exists())
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
                    QuestionResult(explanation = "Words")
                )
            )
        }

        val draftReviewRepo = mock<DraftReviewRepository> {
            on { readDraftReviewFile(workbook, chapter) } doReturn reviews
        }
        val questionsRepo = mock<QuestionsRepository> {
        }

        val renderer = MockRenderer()

        val exporter = ChapterReviewExporter(draftReviewRepo, questionsRepo)
        exporter.exportChapter(workbook, chapter, dir, renderer).blockingGet()

        val file = File("testDir/Source-Target__Book_123.html")
        val lines = file.readLines()

        Assert.assertArrayEquals(arrayOf("HELLO", "WORLD"), lines.toTypedArray())
    }
}

class MockRenderer: IChapterReviewRenderer {
    override fun writeReviewsToFile(reviews: ChapterDraftReview, out: PrintWriter): ExportResult {
        out.println("HELLO")
        out.println("WORLD")
        return ExportResult.SUCCESS
    }
}
