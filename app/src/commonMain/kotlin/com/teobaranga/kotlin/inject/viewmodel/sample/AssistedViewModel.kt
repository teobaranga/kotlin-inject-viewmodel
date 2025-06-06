package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.lifecycle.ViewModel
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.AssistedFactory
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesViewModel(scope = AppScope::class, assistedFactory = AssistedViewModel.Factory::class)
class AssistedViewModel(
    @Assisted val luckyNumber: Int,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        operator fun invoke(luckyNumber: Int): AssistedViewModel
    }
}
