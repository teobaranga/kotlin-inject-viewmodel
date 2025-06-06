package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.teobaranga.kotlin.inject.viewmodel.compiler.env.EnvironmentOwner
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.CommonClassNames
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.getArgumentByName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.getAssistedParametersTypes
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.qualifiedName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.simpleShortName
import com.teobaranga.kotlin.inject.viewmodel.runtime.ContributesViewModel
import me.tatarka.inject.annotations.AssistedFactory
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
        FileSpec.builder(packageName, fileName)
            .addType(generateComponentInterface(annotatedClass))
            .build()
            .writeTo(codeGenerator = env.codeGenerator, aggregating = false)
    }

    /**
     * Generate the kotlin-inject component interface that uses anvil to contribute the ViewModel factory to the
     * right map.
     */
    private fun generateComponentInterface(annotatedClass: KSClassDeclaration): TypeSpec {
        val interfaceName = "${annotatedClass.simpleShortName}Component"
        val scope = annotatedClass.requireViewModelScope()
        return TypeSpec.interfaceBuilder(interfaceName)
            .addAnnotation(
                AnnotationSpec.builder(ContributesTo::class)
                    .addMember("%T::class", scope.toTypeName())
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
     * - an @AssistedFactory type that returns the ViewModel in case there are assisted parameters
     */
    private fun generateProviderFunction(annotatedClass: KSClassDeclaration): FunSpec {
        val assistedParameters = annotatedClass.getAssistedParametersTypes()

        val viewModelClassName = annotatedClass.simpleShortName

        val parameter = ParameterSpec
            .builder(
                name = "factory",
                type = when {
                    assistedParameters.isNotEmpty() -> {
                        annotatedClass.requireViewModelAssistedFactory().toClassName()
                    }

                    else -> LambdaTypeName.get(
                        parameters = assistedParameters,
                        returnType = annotatedClass.toClassName(),
                    )
                },
            )
            .build()

        // Pair<KClass<out ViewModel>, () -> ViewModel> or
        // Pair<KClass<out ViewModel>, Any>
        val returnType = Pair::class.asClassName().parameterizedBy(
            KClass::class.asClassName().parameterizedBy(
                WildcardTypeName.producerOf(ClassName.bestGuess(CommonClassNames.VIEW_MODEL))
            ),
            when {
                assistedParameters.isNotEmpty() -> {
                    Any::class.asTypeName()
                }

                else -> LambdaTypeName.get(
                    parameters = assistedParameters,
                    returnType = ClassName.bestGuess(CommonClassNames.VIEW_MODEL),
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

    private fun KSClassDeclaration.requireViewModelScope(): KSType {
        // There is a simpler way to do this using the experimental getAnnotationsByType, however it doesn't
        // work when attempting to retrieve the type of the scope argument if it's not on this project's classpath.
        return contributesViewModelAnnotation.getArgumentByName(ContributesViewModel::scope.name)
            .value as KSType
    }

    private fun KSClassDeclaration.requireViewModelAssistedFactory(): KSClassDeclaration {
        // There is a simpler way to do this using the experimental getAnnotationsByType, however it doesn't
        // work when attempting to retrieve the type of the factory argument if it's not on this project's classpath.
        val scopeArgument = contributesViewModelAnnotation.getArgumentByName(ContributesViewModel::assistedFactory.name)
            .value as KSType
        val factoryType = scopeArgument.declaration.closestClassDeclaration()!!
        when {
            factoryType.qualifiedName!!.asString() == Unit::class.qualifiedName -> {
                env.logger.error(
                    message = "ViewModels with @Assisted parameters must have an @AssistedFactory declared: ${qualifiedName!!.asString()}",
                    symbol = this,
                )
            }

            factoryType.annotations.none { it.isOfType<AssistedFactory>() } -> {
                env.logger.error(
                    message = "ViewModel assisted factory must be an @AssistedFactory: ${qualifiedName!!.asString()}",
                    symbol = this,
                )
            }

            factoryType.getDeclaredFunctions().none { it.returnType?.qualifiedName == qualifiedName } -> {
                env.logger.error(
                    message = "ViewModel assisted factory must return this ViewModel: ${qualifiedName!!.asString()}",
                    symbol = factoryType,
                )
            }
        }
        return factoryType
    }

    private val KSClassDeclaration.contributesViewModelAnnotation: KSAnnotation
        get() = annotations
            .first { annotation ->
                annotation.isOfType<ContributesViewModel>()
            }

    private inline fun <reified T : Any> KSAnnotation.isOfType(): Boolean {
        return shortName.asString() == T::class.simpleName
            && annotationType.resolve().declaration.qualifiedName?.asString() == T::class.qualifiedName
    }
}
