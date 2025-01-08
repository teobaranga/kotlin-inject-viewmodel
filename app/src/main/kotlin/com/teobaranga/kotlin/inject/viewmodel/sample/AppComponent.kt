package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.lifecycle.ViewModelProvider
import com.teobaranga.kotlin.inject.viewmodel.runtime.KotlinInjectViewModelFactory
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.ViewModelFactoryOwner
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.ForScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

// TODO generate this
@ContributesTo(AppScope::class)
@SingleIn(AppScope::class)
interface VmFactoryComponent {

    @Provides
    @SingleIn(AppScope::class)
    fun provide(
        vmFactory: KotlinInjectViewModelFactory
    ): @ForScope(AppScope::class) ViewModelProvider.Factory = vmFactory
}

@Component
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class AppComponent : AppComponentMerged {

    abstract val vmFactory: @ForScope(AppScope::class) ViewModelProvider.Factory
}

val AppComponent.viewModelFactoryOwner
    get() = object : ViewModelFactoryOwner {
        override val viewModelFactory: ViewModelProvider.Factory
            get() = vmFactory
    }
