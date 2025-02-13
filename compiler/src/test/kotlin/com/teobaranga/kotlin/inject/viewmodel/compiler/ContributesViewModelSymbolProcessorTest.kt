package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.teobaranga.kotlin.inject.viewmodel.runtime.KotlinInjectViewModelFactory
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides
import org.intellij.lang.annotations.Language
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
import kotlin.reflect.full.primaryConstructor
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
    fun `ViewModel with base class extending ViewModel generates correct component`() {
        compile(
            SourceFile.kotlin("TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(): BaseViewModel()
            """),
            SourceFile.kotlin("BaseViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel

                @Suppress("unused")
                abstract class BaseViewModel(): ViewModel()
            """)
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
    fun `ViewModel with assisted dependency generates correct component`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(@Assisted private val dependency: Dependency): ViewModel()
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

                // Provider function should take in the assisted ViewModel factory: TestViewModelFactory
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
    fun `ViewModel with assisted dependency generates correct factory`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(@Assisted private val dependency: Dependency): ViewModel()
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

            with(testViewModelFactoryClass) {
                annotations.single() shouldBe Inject()

                primaryConstructor!!.valueParameters.single().type shouldBe Function1::class.createType(
                    listOf(
                        KTypeProjection(KVariance.INVARIANT, dependencyClass.createType()),
                        KTypeProjection(KVariance.INVARIANT, testViewModelClass.createType()),
                    )
                )

                with(declaredFunctions.single()) {
                    valueParameters.single().type shouldBe dependencyClass.createType()
                    returnType shouldBe testViewModelClass.createType()
                }
            }
        }
    }

    @Test
    fun `ViewModel with SavedStateHandle dependency generates correct component`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.SavedStateHandle
                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(@Assisted private val savedStateHandle: SavedStateHandle): ViewModel()
            """
            ),
        ) {
            exitCode shouldBe KotlinCompilation.ExitCode.OK

            testViewModelComponentClass.annotations.single() shouldBe ContributesTo(AppScope::class)

            with(testViewModelComponentClass.declaredFunctions.single()) {
                name shouldBe "provideTestViewModel"

                annotations shouldBe listOf(Provides(), IntoMap())

                // Provider function should take in a factory with assisted SavedStateHandle:
                // (SavedStateHandle) -> TestViewModel
                with(valueParameters.single()) {
                    type shouldBe Function1::class.createType(
                        listOf(
                            KTypeProjection(KVariance.INVARIANT, savedStateHandleClass.createType()),
                            KTypeProjection(KVariance.INVARIANT, testViewModelClass.createType()),
                        )
                    )
                }

                // Pair<KClass<out ViewModel>, (SavedStateHandle) -> ViewModel>
                returnType shouldBe Pair::class.createType(
                    listOf(
                        KTypeProjection(
                            KVariance.INVARIANT, KClass::class.createType(
                                listOf(KTypeProjection(KVariance.OUT, viewModelClass.createType()))
                            )
                        ),
                        KTypeProjection(
                            KVariance.INVARIANT, Function1::class.createType(
                                listOf(
                                    KTypeProjection(KVariance.INVARIANT, savedStateHandleClass.createType()),
                                    KTypeProjection(KVariance.INVARIANT, viewModelClass.createType())
                                )
                            )
                        )
                    ),
                )
            }
        }
    }

    @Test
    fun `Scoped ViewModel factory component generated correctly`() {
        compile(
            SourceFile.kotlin(
                "TestViewModel.kt", """
                package com.teobaranga.kotlin.inject.viewmodel.compiler.test

                import androidx.lifecycle.ViewModel
                import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
                import me.tatarka.inject.annotations.Assisted
                import me.tatarka.inject.annotations.Inject
                import software.amazon.lastmile.kotlin.inject.anvil.AppScope

                @Suppress("unused")
                @Inject
                @ContributesViewModel(AppScope::class)
                class TestViewModel(): ViewModel()
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

    private fun compile(@Language("kotlin") contents: String, block: JvmCompilationResult.() -> Unit) {
        compile(SourceFile.kotlin("Source.kt", contents)) {
            block(this)
        }
    }

    private fun compile(vararg sources: SourceFile, block: JvmCompilationResult.() -> Unit) {
        KotlinCompilation().run {
            inheritClassPath = true
            allWarningsAsErrors = true
            verbose = false
            messageOutputStream = System.out
            this.sources = sources.toList()
            configureKsp(useKsp2 = true) {
                languageVersion = "2.0"
                symbolProcessorProviders += ContributesViewModelSymbolProcessor.Provider()
                allWarningsAsErrors = true
            }
            compile()
        }.run {
            block(this)
        }
    }
}
