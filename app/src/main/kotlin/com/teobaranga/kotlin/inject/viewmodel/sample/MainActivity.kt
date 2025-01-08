package com.teobaranga.kotlin.inject.viewmodel.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.LocalViewModelFactoryOwner
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.injectedViewModel
import com.teobaranga.kotlin.inject.viewmodel.sample.user.UserActivity
import kotlin.random.Random

private const val LUCKY_NUMBER_RANGE = 100

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(
                LocalViewModelFactoryOwner provides (application as App).appComponent.viewModelFactoryOwner
            ) {
                Scaffold { contentPadding ->
                    Column(
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val viewModel = injectedViewModel<SavedStateHandleViewModel>()
                        val assistedViewModel = injectedViewModel<AssistedViewModel, AssistedViewModelFactory>(
                            creationCallback = { factory ->
                                val luckyNumber = Random.nextInt(LUCKY_NUMBER_RANGE)
                                factory(luckyNumber)
                            },
                        )

                        Text(
                            modifier = Modifier
                                .padding(bottom = 48.dp),
                            text = "Hello world!",
                        )

                        Text(
                            modifier = Modifier
                                .padding(bottom = 48.dp),
                            text = "Your lucky number is ${assistedViewModel.luckyNumber}!",
                        )

                        OutlinedTextField(
                            state = viewModel.userName,
                            placeholder = {
                                Text("Username")
                            },
                        )

                        Button(
                            modifier = Modifier
                                .padding(top = 12.dp),
                            onClick = {
                                val intent = UserActivity.createIntent(
                                    context = this@MainActivity,
                                    username = viewModel.userName.text.toString(),
                                )
                                startActivity(intent)
                            }
                        ) {
                            Text("Login")
                        }
                    }
                }
            }
        }
    }
}
