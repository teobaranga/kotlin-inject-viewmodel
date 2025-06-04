package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

internal typealias ViewModelMap = Map<KClass<out ViewModel>, () -> ViewModel>

internal typealias ViewModelFactoryMap = Map<KClass<out ViewModel>, Any>
