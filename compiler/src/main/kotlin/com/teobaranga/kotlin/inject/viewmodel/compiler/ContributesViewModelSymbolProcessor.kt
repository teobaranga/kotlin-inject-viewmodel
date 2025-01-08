package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.SavedStateHandleClassName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.simpleShortName
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

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
    private val env: SymbolProcessorEnvironment,
) : SymbolProcessor {

    @AutoService(SymbolProcessorProvider::class)
    @Suppress("unused")
    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return ContributesViewModelSymbolProcessor(environment)
        }
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(ContributesViewModel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .forEach { annotatedClass ->
                // TODO validation
                val fileSpec = generateComponentFile(annotatedClass)
                fileSpec
                    .writeTo(
                        codeGenerator = env.codeGenerator,
                        aggregating = false,
                        originatingKSFiles = listOf(annotatedClass.containingFile!!),
                    )
            }

        return emptyList()
    }

    /**
     * Generate the file containing the kotlin-inject component that contributes the ViewModel factory
     * into the right map.
     */
    private fun generateComponentFile(annotatedClass: KSClassDeclaration): FileSpec {
        val packageName = annotatedClass.qualifiedName?.getQualifier().orEmpty()
        val fileName = "${annotatedClass.simpleShortName}Component"
        return FileSpec.builder(packageName, fileName)
            .apply {
                val assistedParameters = annotatedClass.getAssistedParametersTypes()
                if (assistedParameters.any { it != SavedStateHandleClassName }) {
                    addType(generateAssistedViewModelFactory(annotatedClass))
                }
            }
            .addType(generateComponentInterface(annotatedClass))
            .build()
    }

    /**
     * Generate the kotlin-inject component interface that uses anvil to contribute the ViewModel factory to the
     * right map.
     */
    fun generateComponentInterface(annotatedClass: KSClassDeclaration): TypeSpec {
        val interfaceName = "${annotatedClass.simpleShortName}Component"
        return TypeSpec.interfaceBuilder(interfaceName)
            .addAnnotation(
                AnnotationSpec.builder(ContributesTo::class)
                    .addMember("%T::class", getScope(annotatedClass))
                    .build(),
            )
            .addFunction(generateProviderFunction(annotatedClass))
            .build()
    }

    fun getScope(element: KSClassDeclaration): TypeName {
        // TODO explain why the experimental way didn't work
        val annotation = element.annotations
            .first {
                it.shortName.asString() == ContributesViewModel::class.simpleName
                    && it.annotationType.resolve().declaration.qualifiedName?.asString() == ContributesViewModel::class.qualifiedName
            }
        val scopeArgument = annotation.arguments
            .first {
                it.name?.asString() == ContributesViewModel::scope.name
            }
            .value as KSType
        return scopeArgument.toTypeName()
    }
}
