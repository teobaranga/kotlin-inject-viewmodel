package com.teobaranga.kotlin.inject.viewmodel.compiler.util

import com.teobaranga.kotlin.inject.viewmodel.compiler.ContributesViewModelSymbolProcessor
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
fun compile(@Language("kotlin") contents: String, block: JvmCompilationResult.() -> Unit) {
    compile(SourceFile.kotlin("Source.kt", contents)) {
        block(this)
    }
}

@OptIn(ExperimentalCompilerApi::class)
fun compile(vararg sources: SourceFile, block: JvmCompilationResult.() -> Unit) {
    KotlinCompilation().run {
        inheritClassPath = true
        allWarningsAsErrors = true
        verbose = false
        messageOutputStream = System.out
        this.sources = sources.toList()
        configureKsp {
            languageVersion = "2.3"
            symbolProcessorProviders += ContributesViewModelSymbolProcessor.Provider()
            allWarningsAsErrors = true
        }
        compile()
    }.run {
        block(this)
    }
}
