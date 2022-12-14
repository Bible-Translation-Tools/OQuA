package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ChapterReviewExporter @Inject constructor (
    private val draftReviewRepo: DraftReviewRepository,
    private val questionsRepo: QuestionsRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var createdTime: LocalDateTime

    fun exportChapter(
        workbook: Workbook,
        chapter: Chapter,
        directory: File,
        renderer: IChapterReviewRenderer
    ): Single<ExportResult> {
        createdTime = LocalDateTime.now()
        return getDraftReviewAndExport(workbook, chapter, directory, renderer)
            .doOnError { error ->
                logger.error(error.message)
            }
            .onErrorReturnItem(ExportResult.FAILURE)
    }

    private fun getDraftReviewAndExport(
        workbook: Workbook,
        chapter: Chapter,
        directory: File,
        renderer: IChapterReviewRenderer
    ): Single<ExportResult> {
        return try {
            /** If you are able to find the draft review file, export it */
            val reviews = draftReviewRepo.readDraftReviewFile(workbook, chapter)
            Single.fromCallable {
                return@fromCallable executeExport(reviews, directory, renderer)
            }
        } catch (_: FileNotFoundException) {
            /** If you are unable to find it, create an empty one */
            logger.info("Review of ${workbook.target.title} ${chapter.sort} not found. Generating empty review.")
            handleMissingReview(
                workbook,
                chapter,
                directory,
                renderer
            )
        }
    }

    private fun handleMissingReview(
        workbook: Workbook,
        chapter: Chapter,
        directory: File,
        renderer: IChapterReviewRenderer
    ): Single<ExportResult> {
        return getSourceChapter(workbook, chapter)
            .map { sourceChapter ->
                writeBlankReview(workbook, sourceChapter, directory, renderer).blockingGet()
            }
    }

    private fun getSourceChapter(workbook: Workbook, chapter: Chapter): Single<Chapter> {
        return workbook
            .source
            .chapters
            .filter { it.sort == chapter.sort }
            .firstOrError()
    }

    private fun writeBlankReview(
        workbook: Workbook,
        sourceChapter: Chapter,
        directory: File,
        renderer: IChapterReviewRenderer
    ): Single<ExportResult> {
        return questionsRepo
            .loadQuestionsResource(sourceChapter)
            .map { questions ->
                ChapterDraftReview(
                    workbook.source.language.name,
                    workbook.target.language.name,
                    workbook.target.title,
                    sourceChapter.sort,
                    questions.map { QuestionDraftReview.mapFromQuestion(it) }
                )
            }
            .map { reviews ->
                executeExport(reviews, directory, renderer)
            }
    }

    private fun executeExport(
        reviews: ChapterDraftReview,
        directory: File,
        renderer: IChapterReviewRenderer
    ): ExportResult {
        var exportResult = ExportResult.FAILURE

        val file = getTargetFile(reviews, directory)
        logger.info("Writing ${reviews.book} ${reviews.chapter} into ${file.absolutePath}.")
        file.printWriter().use { out ->
            exportResult = renderer.writeReviewsToFile(reviews, createdTime, out)
        }

        return exportResult
    }

    private fun getTargetFile(
        reviews: ChapterDraftReview,
        directory: File
    ): File {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        val timestamp = createdTime.format(formatter)
        val name = "${reviews.source}-${reviews.target}__${reviews.book}_${reviews.chapter}__${timestamp}.html"
        return File("${directory.absolutePath}/$name")
    }
}