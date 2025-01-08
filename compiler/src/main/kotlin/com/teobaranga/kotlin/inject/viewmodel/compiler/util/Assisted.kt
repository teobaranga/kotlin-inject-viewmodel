package com.teobaranga.kotlin.inject.viewmodel.compiler.util

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import me.tatarka.inject.annotations.Assisted

@OptIn(KspExperimental::class)
fun KSClassDeclaration.getAssistedParameters(): List<KSValueParameter> {
    return (primaryConstructor?.parameters ?: emptyList())
        .filter { parameter ->
            parameter.getAnnotationsByType(Assisted::class).count() > 0
        }
}

fun KSClassDeclaration.getAssistedParametersTypes(): Array<TypeName> {
    return getAssistedParameters()
        .map { parameter ->
            parameter.type.toTypeName()
        }
        .toTypedArray()
}
