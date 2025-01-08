package com.teobaranga.kotlininject.viewmodel.compiler

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.teobaranga.kotlininject.viewmodel.compiler.util.SavedStateHandleClassName
import me.tatarka.inject.annotations.Inject

/**
 * For ViewModels that have non-SavedStateHandle assisted parameters, generate a public Factory class
 * that can be used to create the ViewModel given the required parameters. The Factory class wraps the
 * kotlin-inject generated provider function of the ViewModel and can implement `SavedStateViewModelFactory`
 * to let consumers provide a SavedStateHandle.
 */
fun generateAssistedViewModelFactory(
    annotatedClass: KSClassDeclaration,
): TypeSpec {
    val className = "${annotatedClass.simpleName.getShortName()}Factory"
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

    val parameters = annotatedClass.getAssistedParameters()
        .map { parameter ->
            ParameterSpec
                .builder(requireNotNull(parameter.name).getShortName(), parameter.type.toTypeName())
                .build()
        }

    return TypeSpec.classBuilder(className)
        .addAnnotation(Inject::class)
        .primaryConstructor(factoryParam)
        .apply {
            if (parameters.any { it.type == SavedStateHandleClassName }) {
                addSuperinterface(ClassName.bestGuess("com.teobaranga.kotlininject.viewmodel.SavedStateViewModelFactory"))
                addProperty(
                    PropertySpec.builder(
                        "savedStateHandle",
                        SavedStateHandleClassName,
                        KModifier.OVERRIDE,
                        KModifier.LATEINIT
                    )
                        .mutable(true)
                        .build(),
                )
            }
        }
        .addProperty(factoryProperty)
        .addFunction(
            FunSpec.builder("invoke")
                .addModifiers(KModifier.OPERATOR)
                .addParameters(
                    parameters
                        .filterNot {
                            // No need to pass in a SavedStateHandle at runtime, the `ViewModelProvider.Factory`
                            // should do it.
                            it.type == SavedStateHandleClassName
                        },
                )
                .returns(annotatedClass.toClassName())
                .addCode(
                    "return %N(${(0 until parameters.size).map { "%N" }.joinToString(", ")})",
                    factoryProperty,
                    *parameters.toTypedArray(),
                )
                .build(),
        )
        .build()
}
