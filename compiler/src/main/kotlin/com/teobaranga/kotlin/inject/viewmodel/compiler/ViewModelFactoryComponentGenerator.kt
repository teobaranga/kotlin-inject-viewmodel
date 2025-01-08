package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.teobaranga.kotlin.inject.viewmodel.compiler.env.EnvironmentOwner
import com.teobaranga.kotlin.inject.viewmodel.runtime.KotlinInjectViewModelFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.ForScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

internal class ViewModelFactoryComponentGenerator(
    override val env: SymbolProcessorEnvironment,
) : EnvironmentOwner {

    fun generate(scope: TypeName) {
        FileSpec.builder(getPackageName(scope), getViewModelFactoryName(scope))
            .addType(generateComponentInterface(scope))
            .build()
            .writeTo(codeGenerator = env.codeGenerator, aggregating = false)
    }

    private fun generateComponentInterface(scope: TypeName): TypeSpec {
        return TypeSpec.interfaceBuilder(getViewModelFactoryName(scope))
            .addAnnotation(
                AnnotationSpec.builder(ContributesTo::class)
                    .addMember("%T::class", scope)
                    .build(),
            )
            .addAnnotation(
                AnnotationSpec.builder(SingleIn::class)
                    .addMember("%T::class", scope)
                    .build(),
            )
            .addFunction(generateProviderFunction(scope))
            .build()
    }

    private fun generateProviderFunction(scope: TypeName): FunSpec {
        val parameter = ParameterSpec
            .builder(
                name = "viewModelFactory",
                type = KotlinInjectViewModelFactory::class.asTypeName()
            )
            .build()

        return FunSpec.builder("provideViewModelFactory")
            .addAnnotation(Provides::class)
            .addAnnotation(
                AnnotationSpec.builder(SingleIn::class)
                    .addMember("%T::class", scope)
                    .build(),
            )
            .addAnnotation(
                AnnotationSpec.builder(ForScope::class)
                    .addMember("%T::class", scope)
                    .build(),
            )
            .addParameter(parameter)
            .returns(ClassName.bestGuess("androidx.lifecycle.ViewModelProvider.Factory"))
            .addCode("return %N", parameter)
            .build()
    }

    companion object {

        fun getViewModelFactoryName(scope: TypeName): String {
            val simpleName = ClassName.bestGuess(scope.toString()).simpleName
            return "${simpleName}ViewModelFactoryComponent"
        }

        fun getPackageName(scope: TypeName): String {
            return ClassName.bestGuess(scope.toString()).packageName
        }
    }
}
