package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.fasterxml.jackson.module.kotlin.*
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject

class DraftReviewRepository
@Inject
constructor (
    private val directoryProvider: IDirectoryProvider
) {

    fun readDraftReviewFile(workbook: Workbook, chapter: Chapter): ChapterDraftReview {
        val accessor = getAccessor(workbook)
        val mapper = jacksonObjectMapper()
        val dir: File = accessor.getDraftReviewFile()
        val file = File("${dir.absoluteFile}/ch${chapter.sort}.json")

        return mapper.readValue(file)
    }

    fun writeDraftReviewFile(workbook: Workbook, chapter: Chapter, questions: List<Question>) {
        val accessor = getAccessor(workbook)
        val mapper = jacksonObjectMapper()
        val dir: File = accessor.getDraftReviewFile()
        val file = File("${dir.absoluteFile}/ch${chapter.sort}.json")

        val draftReviews = ChapterDraftReview(
            workbook.source.language.name,
            workbook.target.language.name,
            workbook.target.title,
            chapter.sort,
            questions.map {
                QuestionDraftReview.mapFromQuestion(it)
            }
        )

        dir.mkdirs()
        file.printWriter().use { out ->
            out.println(mapper.writeValueAsString(draftReviews))
        }
    }

    private fun getAccessor(workbook: Workbook): ProjectFilesAccessor {
        return ProjectFilesAccessor(
            directoryProvider,
            workbook.target.resourceMetadata,
            workbook.source.resourceMetadata,
            workbook.target.toCollection()
        )
    }
}