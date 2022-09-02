package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import java.io.PrintWriter

interface IChapterReviewRenderer {
    fun writeReviewsToFile(reviews: ChapterDraftReview, out: PrintWriter): ExportResult
}