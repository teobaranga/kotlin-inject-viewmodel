package com.teobaranga.kotlin.inject.viewmodel.runtime.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import com.teobaranga.kotlin.inject.viewmodel.runtime.KotlinInjectViewModelFactory
import com.teobaranga.kotlin.inject.viewmodel.runtime.withCreationCallback

val LocalViewModelFactoryOwner = staticCompositionLocalOf<ViewModelFactoryOwner> {
    error("No ViewModelFactoryOwner was provided provided via LocalViewModelFactoryOwner")
}

/**
 * Returns or creates a [ViewModel] annotated with [ContributesViewModel], scoped to the local
 * [ViewModelStoreOwner]. This can be a navigation backstack entry, a fragment, or an activity.
 *
 * Requires a [LocalViewModelFactoryOwner] to be set in the composition tree, one that can provide a factory
 * such as [KotlinInjectViewModelFactory].
 *
 * Can only handle ViewModels without assisted dependencies.
 *
 * @see [viewModel]
 */
@Composable
inline fun <reified VM : ViewModel> injectedViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    factory: ViewModelProvider.Factory? = LocalViewModelFactoryOwner.current.viewModelFactory,
): VM {
    return viewModel<VM>(
        viewModelStoreOwner = viewModelStoreOwner,
        key = key,
        factory = factory,
    )
}

/**
 * Returns or creates a [ViewModel] annotated with [ContributesViewModel], scoped to the local
 * [ViewModelStoreOwner]. This can be a navigation backstack entry, a fragment, or an activity.
 *
 * This overload takes in a ViewModel factory type that can be used to create ViewModels with
 * assisted parameters, including those with `@Assisted SavedStateHandle` dependencies.
 *
 * Requires a [LocalViewModelFactoryOwner] to be set in the composition tree, one that can provide a factory
 * such as [KotlinInjectViewModelFactory].
 *
 * @see [viewModel]
 */
@Composable
inline fun <reified VM : ViewModel, reified VMF> injectedViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    factory: ViewModelProvider.Factory? = LocalViewModelFactoryOwner.current.viewModelFactory,
    noinline creationCallback: CreationExtras.(VMF) -> VM,
): VM {
    return viewModel<VM>(
        viewModelStoreOwner = viewModelStoreOwner,
        key = key,
        factory = factory,
        extras = viewModelStoreOwner.run {
            if (this is HasDefaultViewModelProviderFactory) {
                this.defaultViewModelCreationExtras.withCreationCallback(creationCallback)
            } else {
                CreationExtras.Empty.withCreationCallback(creationCallback)
            }
        },
    )
}
