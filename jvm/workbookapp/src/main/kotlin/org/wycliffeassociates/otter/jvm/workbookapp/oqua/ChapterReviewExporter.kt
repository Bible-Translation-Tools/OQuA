package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class ChapterReviewExporter @Inject constructor (
    private val draftReviewRepo: DraftReviewRepository,
    private val questionsRepo: QuestionsRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var htmlRenderer = ChapterReviewHTMLRenderer()

    fun exportChapter(workbook: Workbook, chapter: Chapter, directory: File): Single<ExportResult> {
        return try {
            val reviews = draftReviewRepo.readDraftReviewFile(workbook, chapter)
            Single.fromCallable {
                return@fromCallable executeExport(reviews, directory)
            }
        } catch (_: FileNotFoundException) {
            logger.info("Review of ${workbook.target.title} ${chapter.sort} not found. Generating empty review.")
            handleMissingReview(
                workbook,
                chapter,
                directory
            )
        }
    }

    private fun handleMissingReview(workbook: Workbook, chapter: Chapter, directory: File): Single<ExportResult> {
        return workbook
            .source
            .chapters
            .filter { it.sort == chapter.sort }
            .firstOrError()
            .map { sourceChapter ->
                writeBlankReview(workbook, sourceChapter, directory).blockingGet()
            }
    }

    private fun writeBlankReview(workbook: Workbook, sourceChapter: Chapter, directory: File): Single<ExportResult> {
        return questionsRepo.loadQuestionsResource(sourceChapter)
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
                executeExport(reviews, directory)
            }
    }

    private fun executeExport(reviews: ChapterDraftReview, directory: File): ExportResult {
        lateinit var exportResult: ExportResult

        val file = getTargetFile(reviews, directory)
        logger.info("Writing ${reviews.book} ${reviews.chapter} into ${file.absolutePath}.")
        file.printWriter().use { out ->
            exportResult = htmlRenderer.writeReviewsToFile(reviews, out)
        }

        return exportResult
    }

    private fun getTargetFile(reviews: ChapterDraftReview, directory: File): File {
        val name = "${reviews.source}-${reviews.target}__${reviews.book}_${reviews.chapter}.html"
        return File("${directory.absolutePath}/$name")
    }
}