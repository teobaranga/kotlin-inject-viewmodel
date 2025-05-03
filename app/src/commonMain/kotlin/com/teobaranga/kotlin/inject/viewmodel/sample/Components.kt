package com.teobaranga.kotlin.inject.viewmodel.sample

import com.teobaranga.kotlin.inject.viewmodel.sample.user.UserComponent

val appComponent = AppComponent::class.create()

var userComponent: UserComponent? = null
    private set

fun createUserComponent(userName: String) {
    userComponent = appComponent.createUserComponent(userName)
}
