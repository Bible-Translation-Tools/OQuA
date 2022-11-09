package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.slf4j.LoggerFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import java.io.FileNotFoundException
import tornadofx.*
import javax.inject.Inject

class VerseRange (val start: Int, val end: Int)

class ChapterViewModel : ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()

    @Inject
    lateinit var draftReviewRepo: DraftReviewRepository
    @Inject
    lateinit var exportRepo: ChapterReviewExporter
    @Inject
    lateinit var questionsRepo: QuestionsRepository

    val settingsViewModel: SettingsViewModel by inject()

    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var take: Take
    private var totalVerses = 0
    private var totalFrames = 0
    private lateinit var verseMarkers: List<AudioCue>
    val hasAllMarkers = SimpleBooleanProperty()

    val questions = observableListOf<Question>()
    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val verseRangeProperty = SimpleObjectProperty<VerseRange>()

    val exportComplete = SimpleBooleanProperty(false)

    lateinit var workbook: Workbook
    lateinit var chapter: Chapter

    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun dock() {
        workbook = wbDataStore.workbook
        chapter = wbDataStore.chapter

        exportComplete.set(false)

        loadChapterTake()
        loadAudio()
        loadVerseMarkers()
        loadQuestions()
        // Verse -1 to -1 is an initial case that shouldn't match any possible verse range
        verseRangeProperty.set(VerseRange(-1, -1))
    }

    fun undock() {
        closeAudio()
        saveDraftReview()
        disposables.clear()
    }

    private fun loadChapterTake() {
        take = wbDataStore
            .chapter
            .audio
            .selected
            .value
            ?.value
            ?: throw NullPointerException("ChapterViewModel: Could not find selected chapter take")
    }

    private fun loadAudio() {
        val audioPlayer = (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
        audioPlayer.load(take.file)
        audioPlayerProperty.set(audioPlayer)
        totalFrames = audioPlayerProperty.value.getDurationInFrames()
    }

    private fun loadVerseMarkers() {
        wbDataStore
            .getSourceChapter()
            .toSingle()
            .flatMap { it.chunks.count() }
            .map { it.toInt() }
            .subscribe { numberOfSourceChunks ->
                totalVerses = numberOfSourceChunks
                verseMarkers = AudioFile(take.file).metadata.getCues()

                hasAllMarkers.set(verseMarkers.size == totalVerses)
            }
            .addTo(disposables)
    }

    private fun loadQuestions() {
        wbDataStore
            .getSourceChapter()
            .subscribe { chapter ->
                loadQuestionsFromChapter(chapter)
            }
            .addTo(disposables)
    }

    private fun loadQuestionsFromChapter(chapter: Chapter) {
        questionsRepo.loadQuestionsResource(chapter)
            .subscribe { newQuestions ->
                try {
                    draftReviewRepo
                        .readDraftReviewFile(workbook, chapter)
                        .draftReviews
                        .let { draftReviews ->
                            loadDraftReviewIntoQuestions(newQuestions, draftReviews)
                        }
                } catch (_: FileNotFoundException) {
                    /**
                     * Nothing needs to be done with the error
                     * Because it could just be a new chapter
                     * that hasn't been graded yet.
                     */
                }

                questions.setAll(newQuestions)
            }
            .addTo(disposables)
    }

    private fun loadDraftReviewIntoQuestions(questions: List<Question>, draftReviews: List<QuestionDraftReview>) {
        questions.forEach { question ->
            draftReviews.find { loadedReview ->
                (question.question == loadedReview.question)
                        && (question.answer == loadedReview.answer)
            }?.run {
                question.result = this.result
            }
        }
    }

    private fun closeAudio() {
        audioPlayerProperty.value.close()
        audioPlayerProperty.set(null)
    }

    private fun saveDraftReview() {
        draftReviewRepo.writeDraftReviewFile(workbook, chapter, questions)
    }

    fun playVerseRangeFromBeginning(start: Int, end: Int) {
        if (hasAllMarkers.value) {
            verseRangeProperty.set(VerseRange(start, end))

            val startFrame = getVerseFrame(start)
            val endFrame = getVerseEndFrame(end)

            audioPlayerProperty.value.loadSection(take.file, startFrame, endFrame)
            audioPlayerProperty.value.seek(0)
            audioPlayerProperty.value.play()
        }
    }

    fun playVerseRange(start: Int, end: Int) {
        if (hasAllMarkers.value) {
            if ((start in 1..totalVerses) &&
                (end in start..totalVerses)
            ) {
                if (sectionIsLoaded(start, end)) {
                    audioPlayerProperty.value.toggle()
                } else {
                    playVerseRangeFromBeginning(start, end)
                }
            } else {
                throw IndexOutOfBoundsException(
                    "ChapterViewModel: Verse range [$start - $end] does not exist in $totalVerses verses"
                )
            }
        }
    }

    private fun getVerseFrame(verse: Int): Int {
        return verseMarkers[verse - 1].location
    }

    private fun getVerseEndFrame(verse: Int): Int {
        val frame = getVerseFrame(verse)
        for (i in (verse + 1) .. totalVerses) {
            val newFrame = getVerseFrame(i)
            if (newFrame > frame) {
                return newFrame
            }
        }
        return totalFrames
    }

    fun sectionIsLoaded(start: Int, end: Int): Boolean = verseRangeProperty.value?.let { verseRange ->
        (verseRange.start == start) && (verseRange.end == end)
    } ?: false

    fun exportChapter() {
        val directory = chooseDirectory(FX.messages["exportChapter"])

        if (directory != null) {
            exportComplete.set(false)
            saveDraftReview()
            exportRepo.exportChapter(
                workbook,
                chapter,
                directory,
                ChapterReviewHTMLRenderer()
            ).subscribe { exportResult ->
                if (exportResult == ExportResult.SUCCESS) {
                    exportComplete.set(true)
                } else {
                    logger.error("Failed to export ${workbook.target.title} ${chapter.sort}")

                    exportComplete.set(true)
                }
            }.addTo(disposables)
        }
    }
}