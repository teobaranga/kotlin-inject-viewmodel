package com.teobaranga.kotlin.inject.viewmodel.compiler.util

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument

val KSClassDeclaration.simpleShortName: String
    get() = simpleName.getShortName()

/**
 * Resolve the underlying qualified name.
 */
val KSTypeReference.qualifiedName: KSName?
    get() = resolve().declaration.qualifiedName

fun KSAnnotation.getArgumentByName(name: String): KSValueArgument {
    return arguments.first { it.name?.asString() == name }
}
