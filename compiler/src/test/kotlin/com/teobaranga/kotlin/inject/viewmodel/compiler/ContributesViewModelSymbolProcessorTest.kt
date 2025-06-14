package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.teobaranga.kotlin.inject.viewmodel.compiler.util.VIEW_MODEL_FACTORY_FQ_NAME
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.appScopeViewModelFactoryComponentClass
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.compile
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.testViewModelClass
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.testViewModelComponentClass
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.testViewModelFactoryClass
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.userScopeViewModelFactoryComponentClass
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.viewModelClass
import com.teobaranga.kotlin.inject.viewmodel.runtime.KotlinInjectViewModelFactory
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.ForScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.typeOf

@OptIn(ExperimentalCompilerApi::class)
class ContributesViewModelSymbolProcessorTest {

    @Test
    fun `annotated class must extend ViewModel`() {
        compile(
            """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @ContributesViewModel(AppScope::class)
                class TestViewModel()
            """
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.INTERNAL_ERROR
            messages shouldContain
                "com.teobaranga.kotlin.inject.viewmodel.compiler.test.TestViewModel must extend ViewModel."
        }
    }

    @Test
    fun `annotated class must be injected`() {
        compile(
            """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @ContributesViewModel(AppScope::class)
                class TestViewModel(): ViewModel()
            """
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.INTERNAL_ERROR
            messages shouldContain
                "com.teobaranga.kotlin.inject.viewmodel.compiler.test.TestViewModel must be annotated with @Inject."
        }
    }

    @Test
    fun `assisted factory must be provided if there are assisted dependencies`() {
        compile(
            """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.Inject
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(@Assisted dependency: String): ViewModel()
            """
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
            messages shouldContain "ViewModels with @Assisted parameters must have an @AssistedFactory declared"
        }
    }

    @Test
    fun `valid assisted factory must be provided if there are assisted dependencies (annotated with @AssistedFactory)`() {
        compile(
            """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.Inject
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(scope = AppScope::class, assistedFactory = TestViewModel.Factory::class)
                class TestViewModel(@Assisted dependency: String): ViewModel() {
                    interface Factory {
                        operator fun invoke(dependency: String): TestViewModel
                    }
                }
            """
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
            messages shouldContain "ViewModel assisted factory must be an @AssistedFactory"
        }
    }

    @Test
    fun `valid assisted factory must be provided if there are assisted dependencies (factory of correct ViewModel)`() {
        compile(
            """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.AssistedFactory
                import me.tatarka.inject.annotations.Inject
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(scope = AppScope::class, assistedFactory = TestViewModel.Factory::class)
                class TestViewModel(@Assisted dependency: String): ViewModel() {
                    @AssistedFactory
                    interface Factory {
                        operator fun invoke(dependency: String): ViewModel
                    }
                }
            """
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
            messages shouldContain "ViewModel assisted factory must return this ViewModel: com.teobaranga.kotlin.inject.viewmodel.compiler.test.TestViewModel"
        }
    }

    @Test
    fun `ViewModel with base class extending ViewModel generates correct component`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(): BaseViewModel()
            """
            ),
            SourceFile.kotlin(
                "BaseViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel

                @Suppress("unused")
                abstract class BaseViewModel(): ViewModel()
            """
            )
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            testViewModelComponentClass.annotations.single() shouldBe ContributesTo(AppScope::class)

            with(testViewModelComponentClass.declaredFunctions.single()) {
                name shouldBe "provideTestViewModel"

                annotations shouldBe listOf(Provides(), IntoMap())

                // Provider function should take in the basic ViewModel factory function: () -> TestViewModel
                with(valueParameters.single()) {
                    type shouldBe Function0::class.createType(
                        listOf(KTypeProjection(KVariance.INVARIANT, testViewModelClass.createType()))
                    )
                }

                // Pair<KClass<out ViewModel>, () -> ViewModel>
                returnType shouldBe Pair::class.createType(
                    listOf(
                        KTypeProjection(
                            KVariance.INVARIANT, KClass::class.createType(
                                listOf(KTypeProjection(KVariance.OUT, viewModelClass.createType()))
                            )
                        ),
                        KTypeProjection(
                            KVariance.INVARIANT, Function0::class.createType(
                                listOf(KTypeProjection(KVariance.INVARIANT, viewModelClass.createType()))
                            )
                        )
                    ),
                )
            }
        }
    }

    @Test
    fun `ViewModel without dependencies generates correct component`() {
        compile(
            """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(): ViewModel()
            """
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            testViewModelComponentClass.annotations.single() shouldBe ContributesTo(AppScope::class)

            with(testViewModelComponentClass.declaredFunctions.single()) {
                name shouldBe "provideTestViewModel"

                annotations shouldBe listOf(Provides(), IntoMap())

                // Provider function should take in the basic ViewModel factory function: () -> TestViewModel
                with(valueParameters.single()) {
                    type shouldBe Function0::class.createType(
                        listOf(KTypeProjection(KVariance.INVARIANT, testViewModelClass.createType()))
                    )
                }

                // Pair<KClass<out ViewModel>, () -> ViewModel>
                returnType shouldBe Pair::class.createType(
                    listOf(
                        KTypeProjection(
                            KVariance.INVARIANT, KClass::class.createType(
                                listOf(KTypeProjection(KVariance.OUT, viewModelClass.createType()))
                            )
                        ),
                        KTypeProjection(
                            KVariance.INVARIANT, Function0::class.createType(
                                listOf(KTypeProjection(KVariance.INVARIANT, viewModelClass.createType()))
                            )
                        )
                    ),
                )
            }
        }
    }

    @Test
    fun `ViewModel with dependencies generates correct component`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(private val dependency: Dependency): ViewModel()
            """
            ),
            SourceFile.kotlin(
                "Dependency.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import me.tatarka.inject.annotations.Inject

                @Suppress("unused")
                @Inject
                class Dependency()
            """
            ),
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            testViewModelComponentClass.annotations.single() shouldBe ContributesTo(AppScope::class)

            with(testViewModelComponentClass.declaredFunctions.single()) {
                name shouldBe "provideTestViewModel"

                annotations shouldBe listOf(Provides(), IntoMap())

                // Provider function should take in the basic ViewModel factory function: () -> TestViewModel
                with(valueParameters.single()) {
                    type shouldBe Function0::class.createType(
                        listOf(KTypeProjection(KVariance.INVARIANT, testViewModelClass.createType()))
                    )
                }

                // Pair<KClass<out ViewModel>, () -> ViewModel>
                returnType shouldBe Pair::class.createType(
                    listOf(
                        KTypeProjection(
                            KVariance.INVARIANT, KClass::class.createType(
                                listOf(KTypeProjection(KVariance.OUT, viewModelClass.createType()))
                            )
                        ),
                        KTypeProjection(
                            KVariance.INVARIANT, Function0::class.createType(
                                listOf(KTypeProjection(KVariance.INVARIANT, viewModelClass.createType()))
                            )
                        )
                    ),
                )
            }
        }
    }

    @Test
    fun `ViewModel with single assisted dependency generates correct component`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.AssistedFactory
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(scope = AppScope::class, assistedFactory = TestViewModel.Factory::class)
                class TestViewModel(@Assisted private val dependency: Dependency): ViewModel() {
                    @AssistedFactory
                    interface Factory {
                        operator fun invoke(dependency: Dependency): TestViewModel                    
                    }
                }
            """
            ),
            SourceFile.kotlin(
                "Dependency.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import me.tatarka.inject.annotations.Inject

                @Suppress("unused")
                class Dependency()
            """
            ),
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            testViewModelComponentClass.annotations.single() shouldBe ContributesTo(AppScope::class)

            with(testViewModelComponentClass.declaredFunctions.single()) {
                name shouldBe "provideTestViewModel"

                annotations shouldBe listOf(Provides(), IntoMap())

                // Provider function should take in the assisted ViewModel factory: TestViewModel.Factory
                with(valueParameters.single()) {
                    type shouldBe testViewModelFactoryClass.createType()
                }

                // Pair<KClass<out ViewModel>, Any>
                returnType shouldBe Pair::class.createType(
                    listOf(
                        KTypeProjection(
                            KVariance.INVARIANT, KClass::class.createType(
                                listOf(KTypeProjection(KVariance.OUT, viewModelClass.createType()))
                            )
                        ),
                        KTypeProjection(KVariance.INVARIANT, typeOf<Any>())
                    ),
                )
            }
        }
    }

    @Test
    fun `ViewModel with multiple assisted dependencies generates correct component`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.SavedStateHandle
                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.AssistedFactory
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(scope = AppScope::class, assistedFactory = TestViewModel.Factory::class)
                class TestViewModel(
                    @Assisted
                    private val savedStateHandle: SavedStateHandle,
                    @Assisted
                    private val dependency: Dependency,
                ): ViewModel() {
                    @AssistedFactory
                    interface Factory {
                        operator fun invoke(savedStateHandle: SavedStateHandle, dependency: Dependency): TestViewModel                    
                    }
                }
            """
            ),
            SourceFile.kotlin(
                "Dependency.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import me.tatarka.inject.annotations.Inject

                @Suppress("unused")
                class Dependency()
            """
            ),
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            testViewModelComponentClass.annotations.single() shouldBe ContributesTo(AppScope::class)

            with(testViewModelComponentClass.declaredFunctions.single()) {
                name shouldBe "provideTestViewModel"

                annotations shouldBe listOf(Provides(), IntoMap())

                // Provider function should take in the assisted ViewModel factory: TestViewModel.Factory
                with(valueParameters.single()) {
                    type shouldBe testViewModelFactoryClass.createType()
                }

                // Pair<KClass<out ViewModel>, Any>
                returnType shouldBe Pair::class.createType(
                    listOf(
                        KTypeProjection(
                            KVariance.INVARIANT, KClass::class.createType(
                                listOf(KTypeProjection(KVariance.OUT, viewModelClass.createType()))
                            )
                        ),
                        KTypeProjection(KVariance.INVARIANT, typeOf<Any>())
                    ),
                )
            }
        }
    }

    @Test
    fun `Scoped ViewModel factory component generated correctly (top-level component)`() {
        compile(
            SourceFile.kotlin(
                "AppComponent.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import software.amazon.lastmile.kotlin.inject.anvil.AppScope
                import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
                import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

                @Suppress("unused")
                @MergeComponent(AppScope::class)
                @SingleIn(AppScope::class)
                abstract class AppComponent
            """
            ),
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            with(appScopeViewModelFactoryComponentClass) {

                annotations shouldBe listOf(ContributesTo(AppScope::class), SingleIn(AppScope::class))

                with(declaredFunctions.single()) {
                    annotations shouldBe listOf(Provides(), SingleIn(AppScope::class), ForScope(AppScope::class))
                    valueParameters.single().type shouldBe KotlinInjectViewModelFactory::class.createType()
                    returnType.toString() shouldBe VIEW_MODEL_FACTORY_FQ_NAME
                }
            }
        }
    }

    @Test
    fun `Scoped ViewModel factory component generated correctly (subcomponent)`() {
        compile(
            SourceFile.kotlin(
                "UserComponent.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import com.teobaranga.kotlin.inject.viewmodel.compiler.UserScope
                import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
                import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

                @Suppress("unused")
                @ContributesSubcomponent(UserScope::class)
                @SingleIn(UserScope::class)
                interface UserComponent
            """
            ),
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            with(userScopeViewModelFactoryComponentClass) {

                annotations shouldBe listOf(ContributesTo(UserScope::class), SingleIn(UserScope::class))

                with(declaredFunctions.single()) {
                    annotations shouldBe listOf(Provides(), SingleIn(UserScope::class), ForScope(UserScope::class))
                    valueParameters.single().type shouldBe KotlinInjectViewModelFactory::class.createType()
                    returnType.toString() shouldBe VIEW_MODEL_FACTORY_FQ_NAME
                }
            }
        }
    }
}
