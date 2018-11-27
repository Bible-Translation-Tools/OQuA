package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.viewmodel.ProjectHomeViewModel
import tornadofx.ViewModel
import tornadofx.Wizard

class ProjectWizardViewModel : ViewModel() {
    private val languageRepo = Injector.languageRepo
    private val collectionRepo = Injector.collectionRepo

    val sourceLanguageProperty = bind { SimpleObjectProperty<Language>() }
    val targetLanguageProperty = bind { SimpleObjectProperty<Language>() }
    val collections: ObservableList<Collection> = FXCollections.observableArrayList()
    val languages: ObservableList<Language> = FXCollections.observableArrayList()

    private val collectionHierarchy: ArrayList<List<Collection>> = ArrayList()

    private val projects = FXCollections.observableArrayList<Collection>()

    private val existingProjects: ObservableList<Collection> = FXCollections.observableArrayList()
    val showOverlayProperty = SimpleBooleanProperty(false)
    val creationCompletedProperty = SimpleBooleanProperty(false)

    private val creationUseCase = CreateProject(collectionRepo)

    init {
        languageRepo
                .getAll()
                .observeOnFx()
                .subscribe { retrieved ->
                    languages.setAll(retrieved)
                }

        collectionRepo
                .getRootProjects()
                .subscribe { retrieved ->
                    projects.setAll(retrieved)
                }

        targetLanguageProperty.toObservable().subscribe { language ->
            existingProjects.setAll(projects.filter { it.resourceContainer?.language == language })
        }
    }

    fun getRootSources() {
        collectionRepo
                .getRootSources()
                .observeOnFx()
                .subscribe { retrieved ->
                    collectionHierarchy.add(retrieved.filter {
                        it.resourceContainer?.language == sourceLanguageProperty.value
                    })
                    collections.setAll(collectionHierarchy.last())
                }
    }

    fun doOnUserSelection(selectedCollection: Collection) {
        if (selectedCollection.labelKey == "project") {
            createProject(selectedCollection)
        } else {
            showSubcollections(selectedCollection)
        }
    }

    private fun showSubcollections(collection: Collection) {
        collectionRepo
                .getChildren(collection)
                .observeOnFx()
                .doOnSuccess { subcollections ->
                    collectionHierarchy.add(subcollections)
                    collections.setAll(collectionHierarchy.last().sortedBy { it.sort })
                }
                .subscribe()
    }

    private fun createProject(selectedCollection: Collection) {
        targetLanguageProperty.value?.let { language ->
            showOverlayProperty.value = true
            creationUseCase
                    .create(selectedCollection, language)
                    .subscribe {
                        tornadofx.find(ProjectHomeViewModel::class).loadProjects()
                        showOverlayProperty.value = false
                        creationCompletedProperty.value = true
                    }
        }
    }

    fun goBack(projectWizard: Wizard) {
        when {
            collectionHierarchy.size > 1 -> {
                collectionHierarchy.removeAt(collectionHierarchy.lastIndex)
                collections.setAll(collectionHierarchy.last().sortedBy { it.sort })
            }
            collectionHierarchy.size == 1 -> {
                collectionHierarchy.removeAt(0)
                projectWizard.back()
            }
            else -> projectWizard.back()
        }
    }

    fun doesProjectExist(project: Collection): Boolean {
        return existingProjects.map { it.titleKey }.contains(project.titleKey)
    }

    fun reset() {
        sourceLanguageProperty.value = null
        targetLanguageProperty.value = null
        collections.setAll()
        collectionHierarchy.clear()
        existingProjects.clear()
        creationCompletedProperty.value = false
    }

    fun languagesValid() = valid(sourceLanguageProperty, targetLanguageProperty)
}