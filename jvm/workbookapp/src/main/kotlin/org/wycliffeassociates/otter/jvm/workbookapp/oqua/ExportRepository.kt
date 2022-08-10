package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.data.workbook.Workbook
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
        val source = getSourceChapter(workbook, chapterNumber)
        val target = getTargetChapter(workbook, chapterNumber)
    }

    private fun getSourceChapter(workbook: Workbook, chapterNumber: Int) {
        workbook
            .source
            .chapters
            .filter { chapter ->
                chapter.sort == chapterNumber
            }
            .blockingFirst()
    }

    private fun getTargetChapter(workbook: Workbook, chapterNumber: Int) {
        workbook
            .target
            .chapters
            .filter { chapter ->
                chapter.sort == chapterNumber
            }
            .blockingFirst()
    }
}