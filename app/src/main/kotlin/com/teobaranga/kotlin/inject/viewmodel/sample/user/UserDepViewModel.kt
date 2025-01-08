package com.teobaranga.kotlin.inject.viewmodel.sample.user

import androidx.lifecycle.ViewModel
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import com.teobaranga.kotlin.inject.viewmodel.sample.Dep
import me.tatarka.inject.annotations.Inject

@Inject
@ContributesViewModel(UserScope::class)
class UserDepViewModel(
    val userName: String,
    myDep: Dep,
) : ViewModel() {

    init {
        println(myDep)
    }
}
