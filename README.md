# Kotlin Inject ViewModel

![Maven Central Version](https://img.shields.io/maven-central/v/com.teobaranga.kotlin.inject.viewmodel/runtime)
![Push](https://github.com/teobaranga/kotlin-inject-viewmodel/actions/workflows/push.yaml/badge.svg?branch=main)

`kotlin-inject-viewmodel` is a custom annotation processor that builds on top of
[kotlin-inject-anvil](https://github.com/amzn/kotlin-inject-anvil) to provide a way to inject ViewModels similar to
Hilt and @HiltViewModel.

> [!IMPORTANT]  
> While the project has a KMP structure, only the Android target has been implemented and tested.  
> Desktop and iOS targets are planned and will be implemented next.

## Setup

The project requires [kotlin-inject](https://github.com/evant/kotlin-inject?tab=readme-ov-file#download)
and [kotlin-inject-anvil](https://github.com/amzn/kotlin-inject-anvil?tab=readme-ov-file#setup) so please refer
to their respective docs for setup.

For `kotlin-inject-viewmodel`:

```kotlin
dependencies {
    ksp("com.teobaranga.kotlin.inject.viewmodel:compiler:$version")
    implementation("com.teobaranga.kotlin.inject.viewmodel:runtime:$version")
    // For Compose
    implementation("com.teobaranga.kotlin.inject.viewmodel:runtime-compose:$version")
}
```

> [!NOTE]  
> This library depends on `androidx.lifecycle` 2.9.0-alpha13 because this is where support for KMP ViewModel
> was introduced. Make sure you're okay introducing an alpha version into your project before using this library.

## Features

- Plugs in nicely into existing projects using Hilt wanting to migrate to kotlin-inject without requiring a different
  injection strategy for ViewModels
- Compose and Activity/Fragment support
- Support all types of ViewModels, both with injected dependencies and with assisted dependencies
- Automatic handling of `@Assisted SavedStateHandle` dependencies

## Usage

Given any ViewModel, add the `@ContributesViewModel` annotation with a scope such as anvil's `AppScope`.
A ViewModel factory will be generated per scope and it will known how to create all the ViewModels contributed
to that scope.

```kotlin
@Inject
@ContributesViewModel(AppScope::class)
class MyViewModel(val foo: Foo) : ViewModel()
```

Add a way to extract the generated ViewModel factory from the component.

```kotlin
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class AppComponent {

    @ForScope(AppScope::class)
    abstract val vmFactory: ViewModelProvider.Factory
}
```

The factory can be used where needed to override the defaults and create ViewModels through injection.

For **Compose**, the standard `viewModel` Composable API can be used with the generated factory.

```kotlin
val viewModel = viewModel<MyViewModel>(
    factory = (application as App).appComponent.vmFactory
)
```

For convenience, the `runtime-compose` dependency provides a simpler way to access ViewModels without repeating
the factory parameter. Provide a `LocalViewModelFactoryOwner` once at the top of the Composable tree then use the
`injectedViewModel` function to get a ViewModel using that factory.

```kotlin
CompositionLocalProvider(
    // Provide a way to access the ViewModel factory to injectedViewModel
    // calls down the composable tree
    LocalViewModelFactoryOwner provides object : ViewModelFactoryOwner {
        override val viewModelFactory: ViewModelProvider.Factory
            get() = (application as App).appComponent.vmFactory
    }
) {
    // No explicit factory needed. Works with navigation as well.
    val viewModel = injectedViewModel<MyViewModel>()
}
```

For **Activities** or **Fragments**, you can use existing APIs from `androidx.activity.viewModels` or
`androidx.fragment.app.viewModels`, respectively.

```kotlin
class MyActivity : ComponentActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = (application as App).appComponent.vmFactory

    // Simple ViewModel
    val myViewModel by viewModels<MyViewModel>()
}
```

**Assisted injection** is supported, eg:

```kotlin
@Inject
@ContributesViewModel(AppScope::class)
class MyViewModel(@Assisted val foo: Foo) : ViewModel()

// ... Generates a MyViewModelFactory

// Activity:
class MyActivity : ComponentActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = (application as App).appComponent.vmFactory
  
    // Assisted ViewModel
    val myAssistedViewModel by viewModels<MyViewModel>(
        extras = defaultViewModelCreationExtras.withCreationCallback<MyViewModelFactory> { factory ->
            factory(Foo())
        },
    )
}

// Compose
val myAssistedViewModel = injectedViewModel<MyViewModel, MyViewModelFactory>(
    creationCallback = { factory ->
        factory(Foo())
    },
)
```

For more practical examples, see the sample app.

## License

**Kotlin Inject ViewModel** is distributed under the terms of the Apache License (Version 2.0). See the
[license](LICENSE) for more information.
