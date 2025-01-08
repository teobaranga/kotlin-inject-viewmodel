package com.teobaranga.kotlin.inject.viewmodel.compiler.env

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

interface EnvironmentOwner {

    val env: SymbolProcessorEnvironment
}
