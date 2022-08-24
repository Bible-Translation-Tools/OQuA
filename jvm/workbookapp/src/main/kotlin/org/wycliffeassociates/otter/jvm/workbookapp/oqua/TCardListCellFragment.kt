package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import tornadofx.*

class TCardListCellFragment: ListCellFragment<TranslationCard>() {
    private val sourceProperty = Bindings.createStringBinding(
        { itemProperty.value?.translation?.source?.name },
        itemProperty
    )
    private val targetProperty = Bindings.createStringBinding(
        { itemProperty.value?.translation?.target?.name },
        itemProperty
    )
    private val hasQuestionsProperty = Bindings.createBooleanBinding(
        { itemProperty.value?.hasQuestions ?: false },
        itemProperty
    )
    private val questionsURLProperty = Bindings.createStringBinding(
        { itemProperty.value?.translation?.source?.slug?.let { slug ->
            "https://content.bibletranslationtools.org/WA-Catalog/${slug}_tq/archive/master.zip"
        }},
        itemProperty
    )
    private val projects = Bindings.createObjectBinding(
        { itemProperty.value?.projects },
        itemProperty
    )

    override val root = vbox {
        hbox {
            text(sourceProperty)
            text(" -> ")
            text(targetProperty)
        }

        vbox {
            addClass("oqua-tcard")

            hiddenWhen(hasQuestionsProperty)
            managedWhen(visibleProperty())
            text("You do not have the questions downloaded for this language. Please follow the instructions below.") {
                addClass("oqua-missing-tq-header")
            }
            text("Use the link below to download the questions")
            hyperlink(questionsURLProperty) {
                action {
                    hostServices.showDocument(questionsURLProperty.value)
                }
            }
            text("Then import that file using the button above.")
            text("You will probably have to restart OQuA after doing so.") {
                addClass("oqua-missing-tq-header")
            }
        }

        listview<Workbook> {
            visibleWhen(hasQuestionsProperty)
            managedWhen(visibleProperty())
            itemsProperty().bind(projects)
            cellFragment(ProjectListCellFragment::class)
        }
    }
}