package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

internal typealias ViewModelMap = Map<KClass<out ViewModel>, () -> ViewModel>

internal typealias SavedStateViewModelMap = Map<KClass<out ViewModel>, (SavedStateHandle) -> ViewModel>

internal typealias ViewModelFactoryMap = Map<KClass<out ViewModel>, Any>
