package com.teobaranga.kotlin.inject.viewmodel.compiler

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.teobaranga.kotlin.inject.viewmodel.compiler.env.EnvironmentOwner
import com.teobaranga.kotlin.inject.viewmodel.compiler.util.getNewSymbolsWithAnnotation
import com.teobaranga.kotlin.inject.viewmodel.runtime.KotlinInjectViewModelFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.ForScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass

/**
 * Generates a kotlin-inject component per scope that binds a ViewModelFactory to an implementation
 * of [KotlinInjectViewModelFactory] that knows how to provide all ViewModels for that scope.
 *
 * Only one such component is generated for that scope in the same package and module where the component
 * associated with that scope is defined.
 */
internal class ViewModelFactoryComponentGenerator(
    override val env: SymbolProcessorEnvironment,
) : EnvironmentOwner {

    fun generate(resolver: Resolver) {
        MergeComponentVisitor.visit(resolver) { scope, componentClass ->
            env.logger.info(
                """
                |Found component class: ${componentClass.qualifiedName?.asString()}
                |with scope: $scope
                """.trimMargin()
            )

            generateComponentFile(scope, componentClass)
        }

        ContributesSubcomponentVisitor.visit(resolver) { scope, componentClass ->
            env.logger.info(
                """
                |Found subcomponent class: ${componentClass.qualifiedName?.asString()}
                |with scope: $scope
                """.trimMargin()
            )

            generateComponentFile(scope, componentClass)
        }
    }

    private fun generateComponentFile(scope: TypeName, componentClass: KSClassDeclaration) {
        val simpleName = getViewModelFactoryComponentName(scope)
        FileSpec.builder(componentClass.qualifiedName?.getQualifier().orEmpty(), simpleName)
            .addType(generateComponentInterface(scope, componentClass))
            .build()
            .writeTo(codeGenerator = env.codeGenerator, aggregating = false)
    }

    private fun generateComponentInterface(scope: TypeName, componentClass: KSClassDeclaration): TypeSpec {
        return TypeSpec.interfaceBuilder(getViewModelFactoryComponentName(scope))
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
            .addOriginatingKSFile(componentClass.containingFile!!)
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

    private fun getViewModelFactoryComponentName(scope: TypeName): String {
        val simpleName = ClassName.bestGuess(scope.toString()).simpleName
        return "${simpleName}ViewModelFactoryComponent"
    }
}

private abstract class ComponentVisitor {

    abstract val annotationClass: KClass<*>

    abstract val scopeName: String

    fun visit(
        resolver: Resolver,
        generateComponentFile: (scope: TypeName, componentClass: KSClassDeclaration) -> Unit
    ) {
        resolver.getNewSymbolsWithAnnotation(annotationClass)
            .filterIsInstance<KSClassDeclaration>()
            .filterNotGenerated()
            .forEach { componentClass ->
                componentClass.annotations
                    .filter { annotation ->
                        annotation.isValid()
                    }
                    .forEach { annotation ->
                        generateComponentFile(annotation.scope, componentClass)
                    }
            }
    }

    private fun Sequence<KSClassDeclaration>.filterNotGenerated() = filter {
        it.containingFile?.filePath?.contains("generated") != true
    }

    private fun KSAnnotation.isValid(): Boolean {
        return shortName.asString() == annotationClass.simpleName
            && annotationType.resolve().declaration.qualifiedName?.asString() == annotationClass.qualifiedName
    }

    private val KSAnnotation.scope: TypeName
        get() {
            val mergeComponent = arguments
                .first { argument ->
                    argument.name?.asString() == scopeName
                }
            return (mergeComponent.value as KSType).toTypeName()
        }
}

private object MergeComponentVisitor : ComponentVisitor() {

    override val annotationClass = MergeComponent::class

    override val scopeName = MergeComponent::scope.name

}

private object ContributesSubcomponentVisitor : ComponentVisitor() {

    override val annotationClass = ContributesSubcomponent::class

    override val scopeName = ContributesSubcomponent::scope.name
}
