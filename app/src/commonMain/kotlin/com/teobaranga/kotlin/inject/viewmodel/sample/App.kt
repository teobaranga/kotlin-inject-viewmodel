package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.LocalViewModelFactoryOwner
import com.teobaranga.kotlin.inject.viewmodel.sample.user.UserScreen
import com.teobaranga.kotlin.inject.viewmodel.sample.user.viewModelFactoryOwner

@Composable
fun App(
    navController: NavHostController = rememberNavController(),
) {
    CompositionLocalProvider(
        LocalViewModelFactoryOwner provides appComponent.viewModelFactoryOwner,
    ) {
        NavHost(
            navController = navController,
            startDestination = MainScreen,
        ) {
            composable<MainScreen> {
                MainScreen(
                    onUserLogin = { userName ->
                        createUserComponent(userName)
                        navController.navigate(UserScreen(userName))
                    }
                )
            }
            composable<UserScreen> { route ->
                CompositionLocalProvider(
                    LocalViewModelFactoryOwner provides userComponent!!.viewModelFactoryOwner,
                ) {
                    UserScreen()
                }
            }
        }
    }
}
