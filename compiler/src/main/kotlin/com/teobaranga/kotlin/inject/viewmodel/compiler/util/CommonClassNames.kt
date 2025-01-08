package com.teobaranga.kotlin.inject.viewmodel.compiler.util

import com.squareup.kotlinpoet.ClassName

val SavedStateHandleClassName = ClassName.bestGuess("androidx.lifecycle.SavedStateHandle")
val ViewModelClassName = ClassName.bestGuess("androidx.lifecycle.ViewModel")
