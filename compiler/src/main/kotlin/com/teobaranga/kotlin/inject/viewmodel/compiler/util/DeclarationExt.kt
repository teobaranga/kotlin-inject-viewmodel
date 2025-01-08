package com.teobaranga.kotlin.inject.viewmodel.compiler.util

import com.google.devtools.ksp.symbol.KSClassDeclaration

val KSClassDeclaration.simpleShortName: String
    get() = simpleName.getShortName()
