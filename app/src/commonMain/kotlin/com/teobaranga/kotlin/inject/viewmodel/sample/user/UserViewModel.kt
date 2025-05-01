package com.teobaranga.kotlin.inject.viewmodel.sample.user

import androidx.lifecycle.ViewModel
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.Inject

@Inject
@ContributesViewModel(UserScope::class)
class UserViewModel(
    val userName: String,
) : ViewModel()
