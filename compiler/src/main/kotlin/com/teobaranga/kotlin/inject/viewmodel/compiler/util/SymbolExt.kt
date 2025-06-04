package com.teobaranga.kotlin.inject.viewmodel.compiler.util

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeReference

val KSClassDeclaration.simpleShortName: String
    get() = simpleName.getShortName()

/**
 * Resolve the underlying qualified name.
 */
val KSTypeReference.qualifiedName: KSName?
    get() = resolve().declaration.qualifiedName
