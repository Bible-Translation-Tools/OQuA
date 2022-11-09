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
    private val verseProperty = Bindings.createStringBinding(
        {
            itemProperty.value?.let { question ->
                getVerseLabel(question)
            }
        },
        itemProperty
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

    lateinit var correctButton: ToggleButton
    lateinit var incorrectButton: ToggleButton
    lateinit var invalidButton: ToggleButton

    private fun getVerseLabel(question: Question): String {
        return if (question.start == question.end) {
            "Verse ${question.start}"
        } else {
            "Verses ${question.start} - ${question.end}"
        }
    }

    override val root = vbox(5) {
        addClass("oqua-tq-card")

        hbox(5) {
            text(verseProperty)
            hbox(5) {
                visibleWhen(viewModel.hasAllMarkers)
                managedWhen(visibleProperty())

                button("Play/Pause") {
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