package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.FileNotFoundException
import javax.inject.Inject

class ChapterViewModel : ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()
@Inject
    lateinit var draftReviewRepo: DraftReviewRepository

    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var take: Take
    private var numberOfVerses = 0
    private var numberOfFrames = 0
    private lateinit var verseMarkers: List<AudioCue>
    val hasAllMarkers = SimpleBooleanProperty()

    val questions = observableListOf<Question>()
    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()

    lateinit var workbook: Workbook
    var chapterNumber = 0

    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun dock() {
        workbook = wbDataStore.workbook
        chapterNumber = wbDataStore.chapter.sort

        loadChapterTake()
        loadAudio()
        loadVerseMarkers()
        loadQuestions()
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
        numberOfFrames = audioPlayerProperty.value.getDurationInFrames()
    }

    private fun loadVerseMarkers() {
        wbDataStore
            .getSourceChapter()
            .toSingle()
            .flatMap { it.chunks.count() }
            .map { it.toInt() }
            .subscribe { numberOfSourceChunks ->
                numberOfVerses = numberOfSourceChunks
                verseMarkers = AudioFile(take.file).metadata.getCues()

                hasAllMarkers.set(verseMarkers.size == numberOfVerses)
            }
            .addTo(disposables)
    }

    private fun loadQuestions() {
        loadQuestionsResource()
            .subscribe { newQuestions ->
                try {
                    draftReviewRepo
                        .readDraftReviewFile(workbook, chapterNumber)
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

    private fun loadQuestionsResource(): Single<List<Question>> {
        return wbDataStore
            .getSourceChapter()
            .flatMapObservable { chapter ->
                chapter.chunks
            }
            .flatMap { chunk ->
                Question.getQuestionsFromChunk(chunk).flattenAsObservable { it }
            }
            .toList()
            .observeOnFx()
            .map { questions ->
                questionsDedup(questions).sortedBy {
                    it.end
                }
            }
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
        draftReviewRepo.writeDraftReviewFile(workbook, chapterNumber, questions)
    }

    fun playVerseRange(start: Int, end: Int) {
        if (hasAllMarkers.value) {
            if ((start in 1..numberOfVerses) &&
                (end in start..numberOfVerses)
            ) {

                val startFrame = getVerseFrame(start)
                val endFrame = getVerseEndFrame(end)

                audioPlayerProperty.value.loadSection(take.file, startFrame, endFrame)
                audioPlayerProperty.value.seek(0)
                audioPlayerProperty.value.play()
            } else {
                throw IndexOutOfBoundsException(
                    "ChapterViewModel: Verse range [$start - $end] does not exist in $numberOfVerses verses"
                )
            }
        }
    }

    private fun getVerseEndFrame(verse: Int): Int {
        return if (verse == numberOfVerses) {
            numberOfFrames
        } else {
            getVerseFrame(verse + 1)
        }
    }

    private fun getVerseFrame(verse: Int): Int {
        return verseMarkers[verse - 1].location
    }
}