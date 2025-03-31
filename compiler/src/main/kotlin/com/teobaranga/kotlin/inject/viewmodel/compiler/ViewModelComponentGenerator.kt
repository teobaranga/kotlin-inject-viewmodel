package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.teobaranga.kotlin.inject.viewmodel.compiler.env.EnvironmentOwner
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.SavedStateHandleClassName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.ViewModelClassName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.getAssistedParametersTypes
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.simpleShortName
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import kotlin.reflect.KClass

internal class ViewModelComponentGenerator(
    override val env: SymbolProcessorEnvironment,
) : EnvironmentOwner {

    /**
     * Generate the file containing the kotlin-inject component that contributes the ViewModel factory
     * into the right map.
     */
    fun generate(annotatedClass: KSClassDeclaration) {
        val packageName = annotatedClass.qualifiedName?.getQualifier().orEmpty()
        val fileName = "${annotatedClass.simpleShortName}Component"
        val scope = getScope(annotatedClass)
        FileSpec.builder(packageName, fileName)
            .addType(generateComponentInterface(annotatedClass, scope))
            .build()
            .writeTo(codeGenerator = env.codeGenerator, aggregating = false)
    }

    /**
     * Generate the kotlin-inject component interface that uses anvil to contribute the ViewModel factory to the
     * right map.
     */
    private fun generateComponentInterface(annotatedClass: KSClassDeclaration, scope: TypeName): TypeSpec {
        val interfaceName = "${annotatedClass.simpleShortName}Component"
        return TypeSpec.interfaceBuilder(interfaceName)
            .addAnnotation(
                AnnotationSpec.builder(ContributesTo::class)
                    .addMember("%T::class", scope)
                    .build(),
            )
            .addFunction(generateProviderFunction(annotatedClass))
            .addOriginatingKSFile(annotatedClass.containingFile!!)
            .build()
    }

    /**
     * Generate the provide function that binds the ViewModel class to its factory.
     *
     * The factory can be either
     * - a simple lambda that returns the ViewModel in case there are no assisted parameters
     * - a lambda that takes in a `SavedStateHandle` and returns a ViewModel
     * - `Any` representing the factory of a more complex ViewModel that has at least one non-SavedStateHandle parameter
     */
    private fun generateProviderFunction(annotatedClass: KSClassDeclaration): FunSpec {
        val assistedParameters = annotatedClass.getAssistedParametersTypes()

        val viewModelClassName = annotatedClass.simpleShortName

        val packageName = annotatedClass.qualifiedName?.getQualifier().orEmpty()
        val parameter = ParameterSpec
            .builder(
                name = "factory",
                type = when {
                    assistedParameters.any { it != SavedStateHandleClassName } -> {
                        ClassName.bestGuess("$packageName.${viewModelClassName}Factory")
                    }

                    else -> LambdaTypeName.get(
                        parameters = assistedParameters,
                        returnType = annotatedClass.toClassName(),
                    )
                },
            )
            .build()

        // Pair<KClass<out ViewModel>, () -> ViewModel> or
        // Pair<KClass<out ViewModel>, (SavedStateHandle) -> ViewModel> or
        // Pair<KClass<out ViewModel>, Any>
        val returnType = Pair::class.asClassName().parameterizedBy(
            KClass::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(ViewModelClassName)),
            when {
                assistedParameters.any { it != SavedStateHandleClassName } -> {
                    Any::class.asTypeName()
                }

                else -> LambdaTypeName.get(
                    parameters = assistedParameters,
                    returnType = ViewModelClassName,
                )
            },
        )

        return FunSpec.builder("provide${viewModelClassName}")
            .addAnnotation(Provides::class)
            .addAnnotation(IntoMap::class)
            .addParameter(parameter)
            .returns(returnType)
            .addCode("return %T::class to %N", annotatedClass.toClassName(), parameter)
            .build()
    }

    private fun getScope(element: KSClassDeclaration): TypeName {
        // There is a simpler way to do this using the experimental getAnnotationsByType, however it doesn't
        // work when attempting to retrieve the type of the scope argument if it's not on this project's classpath.
        val annotation = element.annotations
            .first { annotation ->
                val clazz = ContributesViewModel::class
                annotation.shortName.asString() == clazz.simpleName
                    && annotation.annotationType.resolve().declaration.qualifiedName?.asString() == clazz.qualifiedName
            }
        val scopeArgument = annotation.arguments
            .first { argument ->
                argument.name?.asString() == ContributesViewModel::scope.name
            }
            .value as KSType
        return scopeArgument.toTypeName()
    }
}
