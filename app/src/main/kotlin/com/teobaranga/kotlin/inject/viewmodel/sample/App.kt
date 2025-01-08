package com.teobaranga.kotlin.inject.viewmodel.sample

import android.app.Application
import com.teobaranga.kotlin.inject.viewmodel.sample.user.UserComponent

class App : Application() {

    val appComponent = AppComponent::class.create()

    var userComponent: UserComponent? = null
        private set

    fun createUserComponent(userName: String) {
        userComponent = appComponent.createUserComponent(userName)
    }
}
