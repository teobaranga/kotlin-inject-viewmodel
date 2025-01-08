package com.teobaranga.kotlininject.viewmodel.sample

import androidx.lifecycle.ViewModel
import com.teobaranga.kotlininject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesViewModel(AppScope::class)
class BasicDependencyViewModel(myDep: Dep): ViewModel() {

    init {
        println(myDep)
    }
}
