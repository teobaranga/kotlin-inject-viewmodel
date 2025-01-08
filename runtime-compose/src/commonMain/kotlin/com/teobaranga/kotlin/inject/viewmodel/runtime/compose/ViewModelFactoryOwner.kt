package com.teobaranga.kotlin.inject.viewmodel.runtime.compose

import androidx.lifecycle.ViewModelProvider

interface ViewModelFactoryOwner {

    val viewModelFactory: ViewModelProvider.Factory
}
