@file:Suppress("UnusedImport")

package com.teobaranga.kotlin.inject.viewmodel.runtime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.AssistedFactory
import software.amazon.lastmile.kotlin.inject.anvil.extend.ContributingAnnotation
import kotlin.reflect.KClass

/**
 * Identifies a [ViewModel] for constructor injection using kotlin-inject.
 *
 * The `ViewModel` annotated with [ContributesViewModel] will have its factory contributed to a map of factories
 * keyed by their type, which can then be used to create instances of the ViewModel through a
 * [ViewModelProvider.Factory].
 *
 * Assisted injection is supported by providing an [assistedFactory] for the ViewModel. This factory must
 * annotated with [AssistedFactory] and must have a function that takes the same parameters as the ones
 * annotated with [Assisted] in the ViewModel's constructor. For example:
 * ```kotlin
 * @Inject
 * @ContributesViewModel(
 *     scope = AppScope::class,
 *     assistedFactory = MyAssistedViewModel.Factory::class,
 * )
 * class MyAssistedViewModel(
 *     @Assisted savedStateHandle: SavedStateHandle,
 *     @Assisted val myDep: Dep,
 * ) : ViewModel() {
 *
 *     @AssistedFactory
 *     interface Factory {
 *         operator fun invoke(
 *             @Assisted savedStateHandle: SavedStateHandle,
 *             @Assisted myDep: Dep,
 *         ): MyAssistedViewModel
 *     }
 * }
 * ```
 *
 * The ViewModel can then be retrieved by using the factory. For example, in Compose this could be done through
 * `injectedViewModel()`:
 * ```kotlin
 * val viewModel = injectedViewModel<MyAssistedViewModel, MyAssistedViewModel.Factory>(
 *     creationCallback = { factory ->
 *         factory(createSavedStateHandle(), Dep())
 *     },
 * )
 * ```
 */
@Target(AnnotationTarget.CLASS)
@ContributingAnnotation
annotation class ContributesViewModel(
    /**
     * The scope in which to include this `ViewModel`.
     */
    val scope: KClass<*>,
    /**
     * The [AssistedFactory] interface holding a factory function. The function must take the same [Assisted]
     * parameters as the ViewModel's constructor and return the ViewModel type. See [ContributesViewModel] for an
     * example.
     */
    val assistedFactory: KClass<*> = Unit::class,
)
