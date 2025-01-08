@file:OptIn(ExperimentalCompilerApi::class)

package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

private const val TEST_PACKAGE = "com.teobaranga.kotlin.inject.viewmodel.compiler.test"
private const val ANVIL_PACKAGE = "software.amazon.lastmile.kotlin.inject.anvil"
const val VIEW_MODEL_FACTORY_FQ_NAME = "androidx.lifecycle.ViewModelProvider.Factory"

val JvmCompilationResult.viewModelClass
    get() = classLoader
        .loadClass("androidx.lifecycle.ViewModel")
        .kotlin

val JvmCompilationResult.savedStateHandleClass
    get() = classLoader
        .loadClass("androidx.lifecycle.SavedStateHandle")
        .kotlin

val JvmCompilationResult.testViewModelClass
    get() = classLoader
        .loadClass("$TEST_PACKAGE.TestViewModel")
        .kotlin

val JvmCompilationResult.testViewModelFactoryClass
    get() = classLoader
        .loadClass("$TEST_PACKAGE.TestViewModelFactory")
        .kotlin

val JvmCompilationResult.testViewModelComponentClass
    get() = classLoader
        .loadClass("$TEST_PACKAGE.TestViewModelComponent")
        .kotlin

val JvmCompilationResult.dependencyClass
    get() = classLoader
        .loadClass("$TEST_PACKAGE.Dependency")
        .kotlin

val JvmCompilationResult.appScopeViewModelFactoryComponentClass
    get() = classLoader
        .loadClass("$ANVIL_PACKAGE.AppScopeViewModelFactoryComponent")
        .kotlin
