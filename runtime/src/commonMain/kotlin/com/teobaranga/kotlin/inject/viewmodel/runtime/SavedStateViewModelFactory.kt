package com.teobaranga.kotlin.inject.viewmodel.runtime

import androidx.lifecycle.SavedStateHandle
import me.tatarka.inject.annotations.Assisted

/**
 * Interface used to tag ViewModel factories that require a [SavedStateHandle] and at least one other
 * [Assisted] dependency. This allows a `ViewModelProvider.Factory` to know which factory requires a
 * [SavedStateHandle] and provide it automatically. Without this, users would have to manually provide one.
 */
interface SavedStateViewModelFactory {
    var savedStateHandle: SavedStateHandle
}
