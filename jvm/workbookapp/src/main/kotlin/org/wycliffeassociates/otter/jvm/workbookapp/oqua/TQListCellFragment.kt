package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import tornadofx.*

class TQListCellFragment: ListCellFragment<Question>() {
    private val viewModel: ChapterViewModel by inject()

    private val toggleGroup = ToggleGroup()

    private val questionProperty = Bindings.createStringBinding(
        { itemProperty.value?.question },
        itemProperty
    )
    private val answerProperty = Bindings.createStringBinding(
        { itemProperty.value?.answer },
        itemProperty
    )
    private val sourceTextProperty = Bindings.createStringBinding(
        {
            itemProperty.value?.let { question ->
                viewModel.getSourceText(question.start, question.end)
            } ?: "Source text not loaded"
        },
        itemProperty,
        viewModel.sourceTextProperty
    )
    private val verseProperty = Bindings.createStringBinding(
        {
            itemProperty.value?.let { question ->
                getVerseLabel(question)
            }
        },
        itemProperty
    )
    private val playPauseProperty = Bindings.createStringBinding(
        {
            itemProperty.value?.let {
                if (viewModel.sectionIsLoaded(it.start, it.end) && viewModel.isPlayingProperty.value) {
                    "Pause"
                } else {
                    null
                }
            } ?: "Play"
        },
        itemProperty,
        viewModel.verseRangeProperty,
        viewModel.isPlayingProperty
    )
    private val restartProperty = Bindings.createBooleanBinding(
        {
            itemProperty.value?.let {
                viewModel.sectionIsLoaded(it.start, it.end)
            } ?: false
        },
        itemProperty,
        viewModel.verseRangeProperty
    )

    private lateinit var correctButton: ToggleButton
    private lateinit var incorrectButton: ToggleButton
    private lateinit var invalidButton: ToggleButton

    private fun getVerseLabel(question: Question): String {
        return if (question.start == question.end) {
            "Verse ${question.start}"
        } else {
            "Verses ${question.start} - ${question.end}"
        }
    }

    override val root = borderpane {
        addClass("oqua-tq-card")
        right = label(sourceTextProperty) {
            fitToParentWidth()
            addClass("oqua-source-text")
            isWrapText = true
        }
        center = vbox(5) {
            fitToParentWidth()
            hbox(5) {
                text(verseProperty)
                hbox(5) {
                    addClass("oqua-question-box")
                    visibleWhen(viewModel.hasAllMarkers)
                    managedWhen(visibleProperty())

                    button(playPauseProperty) {
                        action {
                            itemProperty.value?.let {
                                viewModel.playVerseRange(it.start, it.end)
                            }
                        }
                    }
                    button("Restart") {
                        action {
                            itemProperty.value?.let {
                                viewModel.playVerseRangeFromBeginning(it.start, it.end)
                            }
                        }
                        visibleWhen(restartProperty)
                        managedWhen(visibleProperty())
                    }
                }
            }

            label(questionProperty) {
                addClass("oqua-question-text")
                isWrapText = true
            }
            label(answerProperty) {
                addClass("oqua-answer-text")
                isWrapText = true
            }

            hbox(5) {
                correctButton = togglebutton("Approved", toggleGroup) {
                    addClass("oqua-btn-approved")
                    action {
                        item.result.result = ResultValue.APPROVED
                    }
                }
                incorrectButton = togglebutton("Needs work", toggleGroup) {
                    addClass("oqua-btn-needs-work")
                    action {
                        item.result.result = ResultValue.NEEDS_WORK
                    }
                }
                invalidButton = togglebutton("Invalid Question", toggleGroup) {
                    addClass("oqua-btn-invalid")
                    action {
                        item.result.result = ResultValue.INVALID_QUESTION
                    }
                }

                itemProperty.onChange {
                    when (it?.result?.result) {
                        ResultValue.APPROVED -> toggleGroup.selectToggle(correctButton)
                        ResultValue.NEEDS_WORK -> toggleGroup.selectToggle(incorrectButton)
                        ResultValue.INVALID_QUESTION -> toggleGroup.selectToggle(invalidButton)
                        ResultValue.UNANSWERED -> toggleGroup.selectToggle(null)
                    }
                }
            }

            textfield {
                hgrow = Priority.ALWAYS

                itemProperty.onChange {
                    text = it?.result?.explanation
                }
                textProperty().onChange {
                    item?.result?.explanation = it ?: ""
                }
            }
        }
    }
}