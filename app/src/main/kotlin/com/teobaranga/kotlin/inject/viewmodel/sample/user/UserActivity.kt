package com.teobaranga.kotlin.inject.viewmodel.sample.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.LocalViewModelFactoryOwner
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.injectedViewModel
import com.teobaranga.kotlin.inject.viewmodel.sample.App

const val EXTRA_USERNAME = "username"

class UserActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra(EXTRA_USERNAME) ?: "guest"

        (application as App).createUserComponent(username)
        val userComponent = requireNotNull((application as App).userComponent)

        setContent {
            CompositionLocalProvider(
                LocalViewModelFactoryOwner provides userComponent.viewModelFactoryOwner,
            ) {
                Scaffold { contentPadding ->
                    Column(
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val vm = injectedViewModel<UserViewModel>()
                        Text(
                            text = "Hello ${vm.userName}!",
                        )
                    }
                }
            }
        }
    }

    companion object {

        fun createIntent(context: Context, username: String): Intent {
            return Intent(context, UserActivity::class.java)
                .putExtra(EXTRA_USERNAME, username.takeIf { it.isNotBlank() })
        }
    }
}
