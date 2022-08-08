package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.text.Text
import tornadofx.*
import java.lang.Double.min

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

    val textElements = mutableListOf<Text>()

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

    override val root = vbox {
        addClass("oqua-tq-card")

        button(verseProperty) {
            action {
                itemProperty.value?.let {
                    viewModel.playVerseRange(it.start, it.end)
                }
            }
        }

        textElements.add(text(questionProperty) {
            addClass("oqua-question-text")
        })
        textElements.add(text(answerProperty))

        hbox {
            correctButton = togglebutton("Correct", toggleGroup) {
                action {
                    item.result.result = ResultValue.CORRECT
                }
            }
            incorrectButton = togglebutton("Incorrect", toggleGroup) {
                action {
                    item.result.result = ResultValue.INCORRECT
                }
            }
            invalidButton = togglebutton("Invalid Question", toggleGroup) {
                action {
                    item.result.result = ResultValue.INVALID_QUESTION
                }
            }

            itemProperty.onChange {
                when (it?.result?.result) {
                    ResultValue.CORRECT -> toggleGroup.selectToggle(correctButton)
                    ResultValue.INCORRECT -> toggleGroup.selectToggle(incorrectButton)
                    ResultValue.INVALID_QUESTION -> toggleGroup.selectToggle(invalidButton)
                    ResultValue.UNANSWERED -> toggleGroup.selectToggle(null)
                }
            }
        }

        textfield {
            visibleWhen(invalidButton.selectedProperty())
            managedWhen(visibleProperty())

            itemProperty.onChange {
                text = it?.result?.explanation
            }
            textProperty().onChange {
                item?.result?.explanation = it ?: ""
            }
        }


        textElements.forEach { element ->
            element.wrappingWidthProperty().bind(
                Bindings.createDoubleBinding(
                    {
                        /**
                         * For every text element in this cell,
                         * recalculate the wrapping width whenever
                         * the window resizes or the item changes.
                         */
                        val listViewPadding = 15.0
                        val paddingForScrollBar = 40.0
                        (min(
                            width,
                            ((cell?.listView?.width ?: 0.0) - listViewPadding)
                        ) - paddingForScrollBar)
                    },
                    widthProperty(),
                    cellProperty
                )
            )
        }
    }
}