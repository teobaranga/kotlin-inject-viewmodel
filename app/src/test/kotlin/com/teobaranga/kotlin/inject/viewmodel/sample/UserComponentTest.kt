package com.teobaranga.kotlin.inject.viewmodel.sample

import com.teobaranga.kotlin.inject.viewmodel.sample.user.UserDepViewModel
import com.teobaranga.kotlin.inject.viewmodel.sample.user.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserComponentTest {

    @Test
    fun `Basic map contains ViewModel with component dependency`() {
        val appComponent = AppComponent::class.create()
        val userComponent = appComponent.createUserComponent("John Doe")
        val factory = requireNotNull(userComponent.viewModelMap[UserViewModel::class]) {
            "Factory for UserViewModel without dependencies not found"
        }
        val viewModel = factory()
        assertNotNull(viewModel)
        viewModel as UserViewModel
        assertEquals("John Doe", viewModel.userName)
    }

    @Test
    fun `Basic map contains ViewModel with component & parent component dependency`() {
        val appComponent = AppComponent::class.create()
        val userComponent = appComponent.createUserComponent("John Doe")
        val factory = requireNotNull(userComponent.viewModelMap[UserDepViewModel::class]) {
            "Factory for UserViewModel without dependencies not found"
        }
        val viewModel = factory()
        assertNotNull(viewModel)
        viewModel as UserDepViewModel
        assertEquals("John Doe", viewModel.userName)
    }

    @Test
    fun `Basic map contains ViewModel from parent component`() {
        val appComponent = AppComponent::class.create()
        val userComponent = appComponent.createUserComponent("John Doe")
        val factory = requireNotNull(userComponent.viewModelMap[BasicViewModel::class]) {
            "Factory for ViewModel from parent component not found"
        }
        val viewModel = factory()
        assertNotNull(viewModel)
        viewModel as BasicViewModel
    }
}
