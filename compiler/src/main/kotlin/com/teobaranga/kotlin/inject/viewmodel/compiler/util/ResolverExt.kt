package com.teobaranga.kotlin.inject.viewmodel.compiler.util

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import kotlin.reflect.KClass

/**
 * There is a bug in KSP where [Resolver.getSymbolsWithAnnotation] returns the same symbol
 * on a second pass. This method is a workaround for this issue and is used to avoid exceptions around
 * generated files already existing.
 *
 * **See**: [ksp#1993](https://github.com/google/ksp/issues/1993)
 */
fun Resolver.getNewSymbolsWithAnnotation(annotation: KClass<*>): Sequence<KSAnnotated> {
    val newFiles = getNewFiles().toSet()
    return getSymbolsWithAnnotation(annotation.qualifiedName!!)
        .filter { it.containingFile in newFiles }
}
