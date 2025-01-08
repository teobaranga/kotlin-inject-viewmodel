package com.teobaranga.kotlininject.viewmodel.runtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import me.tatarka.inject.annotations.Assisted

/**
 * Returns a new [CreationExtras] with the original entries plus the passed in creation
 * callback. The callback is used by kotlin-inject to create ViewModels annotated with
 * [ContributesViewModel] and non-SavedStateHandle [Assisted] parameters.
 *
 * @param callback A creation callback that takes an assisted factory and returns a [ViewModel].
 */
fun <VMF> CreationExtras.withCreationCallback(callback: (VMF) -> ViewModel): CreationExtras =
    MutableCreationExtras(this).addCreationCallback(callback)

/**
 * Returns the [MutableCreationExtras] with the passed in creation callback added. The callback is used by
 * kotlin-inject to create ViewModels annotated with [ContributesViewModel] and non-SavedStateHandle
 * [Assisted] parameters.
 *
 * @param callback A creation callback that takes an assisted factory and returns a [ViewModel].
 */
@Suppress("UNCHECKED_CAST")
fun <VMF> MutableCreationExtras.addCreationCallback(callback: (VMF) -> ViewModel): CreationExtras =
    this.apply {
        this[CREATION_CALLBACK_KEY] = { factory -> callback(factory as VMF) }
    }
