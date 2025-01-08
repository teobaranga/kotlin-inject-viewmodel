package com.teobaranga.kotlin.inject.viewmodel.sample.user

import androidx.lifecycle.ViewModelProvider
import com.teobaranga.kotlin.inject.viewmodel.runtime.KotlinInjectViewModelFactory
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.ViewModelFactoryOwner
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.ForScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

// TODO generate this
@ContributesTo(UserScope::class)
@SingleIn(UserScope::class)
interface VmFactoryComponent {

    @Provides
    @SingleIn(UserScope::class)
    fun provide(vmFactory: KotlinInjectViewModelFactory): @ForScope(UserScope::class) ViewModelProvider.Factory =
        vmFactory
}

@ContributesSubcomponent(UserScope::class)
@SingleIn(UserScope::class)
interface UserComponent : UserComponentFinalAppComponentMerged {

    val vmFactory: @ForScope(UserScope::class) ViewModelProvider.Factory

    @ContributesSubcomponent.Factory(AppScope::class)
    interface Factory {
        fun createUserComponent(userName: String): UserComponent
    }
}

val UserComponent.viewModelFactoryOwner
    get() = object : ViewModelFactoryOwner {
        override val viewModelFactory: ViewModelProvider.Factory
            get() = vmFactory
    }
