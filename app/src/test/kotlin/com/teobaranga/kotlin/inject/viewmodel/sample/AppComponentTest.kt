package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.lifecycle.SavedStateHandle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class AppComponentTest {

    @Test
    fun `Basic map contains ViewModel without dependencies`() {
        val component = AppComponent::class.create()
        val factory = requireNotNull(component.viewModelMap[BasicViewModel::class]) {
            "Factory for basic ViewModel without dependencies not found"
        }
        val viewModel = factory()
        assertNotNull(viewModel)
        viewModel as BasicViewModel
    }

    @Test
    fun `Basic map contains ViewModel with graph dependencies`() {
        val component = AppComponent::class.create()
        val factory = requireNotNull(component.viewModelMap[BasicDependencyViewModel::class]) {
            "Factory for basic ViewModel with graph dependencies not found"
        }
        val viewModel = factory()
        assertNotNull(viewModel)
        viewModel as BasicDependencyViewModel
    }

    @Test
    fun `SavedStateHandle map contains ViewModel with @Assisted SavedStateHandle`() {
        val component = AppComponent::class.create()
        val factory = requireNotNull(component.savedStateViewModelMap[SavedStateHandleViewModel::class]) {
            "Factory for ViewModel with @Assisted SavedStateHandle not found"
        }
        val viewModel = factory(SavedStateHandle())
        assertNotNull(viewModel)
        viewModel as SavedStateHandleViewModel
        assertNotNull(viewModel.userName)
    }

    @Test
    fun `Assisted map contains ViewModel with @Assisted non-SavedStateHandle dependency`() {
        val component = AppComponent::class.create()
        val factory = requireNotNull(component.viewModelFactoryMap[AssistedViewModel::class]) {
            "Factory for ViewModel with @Assisted dependency not found"
        }
        if (factory !is AssistedViewModelFactory) {
            error("Factory for ViewModel with @Assisted dependency does not have the correct type")
        }
        val viewModel = factory(123)
        assertNotNull(viewModel)
        assertEquals(123, viewModel.luckyNumber)
    }

    @Test
    fun `Assisted map contains ViewModel with @Assisted SavedStateHandle & non-SavedStateHandle dependency`() {
        val component = AppComponent::class.create()
        val factory = requireNotNull(component.viewModelFactoryMap[AssistedDepSavedStateHandleViewModel::class]) {
            "Factory for ViewModel with @Assisted SavedStateHandle & dependency not found"
        }
        if (factory !is AssistedDepSavedStateHandleViewModelFactory) {
            error("Factory for ViewModel with @Assisted SavedStateHandle & dependency does not have the correct type")
        }
        factory.savedStateHandle = SavedStateHandle()
        val viewModel = factory(Dep)
        assertNotNull(viewModel)
    }

    @Test
    fun `Assisted map contains ViewModel with @Assisted non-SavedStateHandle dep & SavedStateHandle (swapped order)`() {
        val component = AppComponent::class.create()
        val factory = requireNotNull(component.viewModelFactoryMap[OrderAssistedDepSavedStateHandleViewModel::class]) {
            "Factory for ViewModel with @Assisted dependency & SavedStateHandle not found"
        }
        if (factory !is OrderAssistedDepSavedStateHandleViewModelFactory) {
            error("Factory for ViewModel with @Assisted dependency & SavedStateHandle does not have the correct type")
        }
        factory.savedStateHandle = SavedStateHandle()
        val viewModel = factory(Dep)
        assertNotNull(viewModel)
    }
}
