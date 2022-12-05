package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import java.io.PrintWriter
import java.time.LocalDateTime

interface IChapterReviewRenderer {
    fun writeReviewsToFile(
        reviews: ChapterDraftReview,
        createdTime: LocalDateTime,
        out: PrintWriter
    ): ExportResult
}