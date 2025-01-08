package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.teobaranga.kotlin.inject.viewmodel.compiler.env.EnvironmentOwner
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.SavedStateHandleClassName
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.getAssistedParameters
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.getAssistedParametersTypes
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.simpleShortName
import com.teobaranga.kotlin.inject.viewmodel.runtime.SavedStateViewModelFactory
import me.tatarka.inject.annotations.Inject

internal class AssistedViewModelFactoryGenerator(
    override val env: SymbolProcessorEnvironment,
) : EnvironmentOwner {

    fun generate(annotatedClass: KSClassDeclaration) {
        val packageName = annotatedClass.qualifiedName?.getQualifier().orEmpty()
        val fileName = "${annotatedClass.simpleShortName}Factory"
        FileSpec.builder(packageName, fileName)
            .addType(generateAssistedViewModelFactory(annotatedClass))
            .build()
            .writeTo(codeGenerator = env.codeGenerator, aggregating = false)
    }

    /**
     * For ViewModels that have non-SavedStateHandle assisted parameters, generate a public factory class
     * that can be used to create the ViewModel given the required parameters. The Factory class wraps the
     * kotlin-inject generated provider function of the ViewModel and can implement [SavedStateViewModelFactory]
     * to let consumers provide a SavedStateHandle.
     */
    fun generateAssistedViewModelFactory(
        annotatedClass: KSClassDeclaration,
    ): TypeSpec {
        // `factory` is the kotlin-inject ViewModel provider function
        val factoryFunctionType = LambdaTypeName.get(
            parameters = annotatedClass.getAssistedParametersTypes(),
            returnType = annotatedClass.toClassName(),
        )
        val factoryParam = FunSpec.constructorBuilder()
            .addParameter("factory", factoryFunctionType)
            .build()
        val factoryProperty = PropertySpec.builder("factory", factoryFunctionType)
            .initializer("factory")
            .addModifiers(KModifier.PRIVATE)
            .build()

        val assistedParameters = annotatedClass.getAssistedParameters()
            .map { parameter ->
                ParameterSpec
                    .builder(requireNotNull(parameter.name).getShortName(), parameter.type.toTypeName())
                    .build()
            }

        return TypeSpec.classBuilder("${annotatedClass.simpleShortName}Factory")
            .addAnnotation(Inject::class)
            .primaryConstructor(factoryParam)
            .apply {
                if (assistedParameters.any { it.type == SavedStateHandleClassName }) {
                    addSavedStateViewModelFactorySuperinterface()
                }
            }
            .addProperty(factoryProperty)
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameters(
                        assistedParameters
                            .filterNot { parameter ->
                                // No need to pass in a SavedStateHandle at runtime, the `ViewModelProvider.Factory`
                                // should do it.
                                parameter.type == SavedStateHandleClassName
                            },
                    )
                    .returns(annotatedClass.toClassName())
                    .addCode(
                        "return %N(${(0 until assistedParameters.size).map { "%N" }.joinToString(", ")})",
                        factoryProperty,
                        *assistedParameters.toTypedArray(),
                    )
                    .build(),
            )
            .addOriginatingKSFile(annotatedClass.containingFile!!)
            .build()
    }

    private fun TypeSpec.Builder.addSavedStateViewModelFactorySuperinterface(): TypeSpec.Builder {
        addSuperinterface(SavedStateViewModelFactory::class.asTypeName())
        addProperty(
            PropertySpec.builder(
                name = "savedStateHandle",
                type = SavedStateHandleClassName,
                modifiers = arrayOf(KModifier.OVERRIDE, KModifier.LATEINIT),
            )
                .mutable(true)
                .build(),
        )
        return this
    }
}
