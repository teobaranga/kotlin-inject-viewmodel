package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesViewModel(AppScope::class)
class AssistedDepSavedStateHandleViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted myDep: Dep,
) : ViewModel() {

    init {
        println(savedStateHandle)
        println(myDep)
    }
}
