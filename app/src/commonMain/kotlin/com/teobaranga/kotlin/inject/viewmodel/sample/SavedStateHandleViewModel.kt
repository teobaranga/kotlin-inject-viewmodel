package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesViewModel(AppScope::class)
class SavedStateHandleViewModel(
    @Assisted
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // TODO: add a KSerializer for TextFieldState
    val userName = TextFieldState()
}
