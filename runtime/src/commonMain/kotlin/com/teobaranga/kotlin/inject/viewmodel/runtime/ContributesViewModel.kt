@file:Suppress("UnusedImport")

package com.teobaranga.kotlin.inject.viewmodel.runtime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.tatarka.inject.annotations.Assisted
import software.amazon.lastmile.kotlin.inject.anvil.extend.ContributingAnnotation
import kotlin.reflect.KClass

/**
 * Identifies a [ViewModel] for constructor injection using kotlin-inject.
 *
 * The `ViewModel` annotated with [ContributesViewModel] will have its factory contributed to a map of factories
 * keyed by their type, which can then be used to create instances of the ViewModel through a
 * [ViewModelProvider.Factory].
 *
 * Assisted injection is supported through the [Assisted] annotation. Any assisted [SavedStateHandle] parameter
 * is automatically injected by kotlin-inject. Other types must be manually provided by using the generated factory
 * named `{ViewModel}Factory`.
 */
@Target(AnnotationTarget.CLASS)
@ContributingAnnotation
annotation class ContributesViewModel(
    /**
     * The scope in which to include this `ViewModel`.
     */
    val scope: KClass<*>,
)
