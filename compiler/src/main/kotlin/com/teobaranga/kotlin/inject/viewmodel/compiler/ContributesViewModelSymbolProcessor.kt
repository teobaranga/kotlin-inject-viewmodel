package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.teobaranga.kotlin.inject.viewmodel.compiler.env.EnvironmentOwner
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.SavedStateHandleClassName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.ViewModelClassName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.getAssistedParametersTypes
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.Inject

/**
 * Processor that makes each `ViewModel` annotated with [ContributesViewModel] available for injection
 * through a `ViewModelProvider.Factory`. The factory for each ViewModel is put into one of three maps keyed by the
 * ViewModel's type.
 *
 * The first map includes factories for simple ViewModels that do not have any assisted parameters. Any dependencies
 * will be provided by the dependency graph.
 *
 * The second map includes factories for ViewModels with exactly one assisted parameter of type `SavedStateHandle`.
 * While this is an assisted parameter, it doesn't require manual injection as the implementation of
 * `ViewModelProvider.Factory` can provide it automatically.
 *
 * The third map includes factories for ViewModels with one or more non-SavedStateHandle parameters. These factories
 * require consumers to provide the assisted parameters at runtime. To make things easier, a `{ViewModel}Factory` type
 * is generated that can be used to create an instance of the ViewModel given the assisted parameters.
 */
internal class ContributesViewModelSymbolProcessor(
    override val env: SymbolProcessorEnvironment,
    private val viewModelComponentGenerator: ViewModelComponentGenerator,
    private val assistedViewModelFactoryGenerator: AssistedViewModelFactoryGenerator,
    private val viewModelFactoryComponentGenerator: ViewModelFactoryComponentGenerator,
) : SymbolProcessor, EnvironmentOwner {

    @AutoService(SymbolProcessorProvider::class)
    @Suppress("unused")
    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return ContributesViewModelSymbolProcessor(
                env = environment,
                viewModelComponentGenerator = ViewModelComponentGenerator(environment),
                assistedViewModelFactoryGenerator = AssistedViewModelFactoryGenerator(environment),
                viewModelFactoryComponentGenerator = ViewModelFactoryComponentGenerator(environment),
            )
        }
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val scopes = mutableSetOf<TypeName>()

        resolver
            .getSymbolsWithAnnotation(ContributesViewModel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .forEach { annotatedClass ->
                validate(annotatedClass)

                // Generate a factory class for ViewModels with non-SavedStateHandle assisted parameters
                val assistedParameters = annotatedClass.getAssistedParametersTypes()
                if (assistedParameters.any { it != SavedStateHandleClassName }) {
                    assistedViewModelFactoryGenerator.generate(annotatedClass)
                }

                val result = viewModelComponentGenerator.generate(annotatedClass)
                if (scopes.add(result.scope)) {
                    viewModelFactoryComponentGenerator.generate(resolver, result.scope)
                }

                // Associate this with the right ViewModelFactoryComponent
                env.codeGenerator.associate(
                    sources = listOf(annotatedClass.containingFile!!),
                    packageName = ViewModelFactoryComponentGenerator.getPackageName(result.scope),
                    fileName = ViewModelFactoryComponentGenerator.getViewModelFactoryComponentName(result.scope),
                )
            }

        return emptyList()
    }

    private fun validate(annotatedClass: KSClassDeclaration) {
        require(annotatedClass.isPublic()) {
            """${annotatedClass.toClassName()} must be public.
                        | kotlin-inject does not support components that are not public."""
                .trimMargin()
        }
        require(annotatedClass.extendsViewModel()) {
            """${annotatedClass.toClassName()} must extend ViewModel."""
        }
        require(annotatedClass.isInjected()) {
            """${annotatedClass.toClassName()} must be annotated with @Inject."""
        }
    }

    private fun KSClassDeclaration.extendsViewModel(): Boolean {
        return getAllSuperTypes().any { it.toTypeName() == ViewModelClassName }
    }

    @OptIn(KspExperimental::class)
    private fun KSAnnotated.isInjected(): Boolean {
        return isAnnotationPresent(Inject::class)
    }
}
