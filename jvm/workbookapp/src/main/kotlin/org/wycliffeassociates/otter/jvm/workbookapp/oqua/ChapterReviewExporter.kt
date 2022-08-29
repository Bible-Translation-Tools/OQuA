package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class ChapterReviewExporter @Inject constructor (
    private val draftReviewRepo: DraftReviewRepository,
    private val questionsRepo: QuestionsRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val disposables = CompositeDisposable()
    private var htmlRenderer = ChapterReviewHTMLRenderer()

    fun exportChapter(workbook: Workbook, chapter: Chapter, directory: File, callback:(Workbook, Chapter, Boolean) -> Unit) {
        try {
            val reviews = draftReviewRepo.readDraftReviewFile(workbook, chapter)
            executeExport(reviews, directory, packageCallback(callback, workbook, chapter))
        } catch (_: FileNotFoundException) {
            logger.info("Review of ${workbook.target.title} ${chapter.sort} not found. Generating empty review.")
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
                executeExport(reviews, directory, callback)
            }
            .addTo(disposables)
    }

    private fun executeExport(reviews: ChapterDraftReview, directory: File, callback: (Boolean) -> Unit) {
        val file = getTargetFile(reviews, directory)
        logger.info("Writing ${reviews.book} ${reviews.chapter} into ${file.absolutePath}.")
        file.printWriter().use { out ->
            htmlRenderer.writeReviewsToFile(reviews, out, callback)
        }
    }

    private fun getTargetFile(reviews: ChapterDraftReview, directory: File): File {
        val name = "${reviews.source}-${reviews.target}__${reviews.book}_${reviews.chapter}.html"
        return File("${directory.absolutePath}/$name")
    }
}