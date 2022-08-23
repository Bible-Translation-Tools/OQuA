package org.wycliffeassociates.otter.jvm.workbookapp.oqua

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

class ExportRepository
@Inject
constructor (
    private val draftReviewRepo: DraftReviewRepository
) {

    fun exportChapter(workbook: Workbook, chapterNumber: Int) {
        val sourceChapter = getSourceChapter(workbook, chapterNumber)
        val targetChapter = getTargetChapter(workbook, chapterNumber)

        lateinit var reviews: ChapterDraftReview
        try {
            reviews = draftReviewRepo.readDraftReviewFile(workbook, chapterNumber)
        } catch (_: FileNotFoundException) {
            /**
             * Chapter was not graded
             * Come back here to fill in empty batch
             */
        }
        writeReviewsToFile(reviews)
    }

    private fun getSourceChapter(workbook: Workbook, chapterNumber: Int): Chapter? { // TODO Remove nullable chapter
        return workbook
            .source
            .chapters
            .filter { chapter ->
                chapter.sort == chapterNumber
            }
            .blockingFirst() // TODO remove blocking first
    }

    private fun getTargetChapter(workbook: Workbook, chapterNumber: Int): Chapter? {
        return workbook
            .target
            .chapters
            .filter { chapter ->
                chapter.sort == chapterNumber
            }
            .blockingFirst()
    }

private fun writeReviewsToFile(reviews: ChapterDraftReview) {
        val file = File("${reviews.book}_${reviews.chapter}")
        file.printWriter().use { out ->
            reviews.draftReviews.forEach { review ->
            }
        }
    }
}