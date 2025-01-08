package com.teobaranga.kotlininject.viewmodel.sample

import android.app.Application
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppComponentTest {

    @Test
    fun `Given AppScope, When AppComponent is created, Then ViewModel map contains BasicViewModel`() {
        val component = component<AppComponent>()
        val viewModel = component.viewModelMap[BasicViewModel::class]?.invoke()
        assertNotNull(viewModel)
    }

    @Test
    fun `Given AppScope, When AppComponent is created, Then ViewModel map contains BasicDependencyViewModel`() {
        val component = component<AppComponent>()
        val viewModel = component.viewModelMap[BasicDependencyViewModel::class]?.invoke()
        assertNotNull(viewModel)
    }

    private fun <T> component(): T {
        @Suppress("UNCHECKED_CAST")
        return AppComponent::class.create(application()) as T
    }

    private fun application(): Application = object : Application() {}
}
