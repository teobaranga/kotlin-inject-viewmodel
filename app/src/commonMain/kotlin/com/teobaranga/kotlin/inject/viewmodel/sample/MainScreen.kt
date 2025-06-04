package com.teobaranga.kotlin.inject.viewmodel.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import com.teobaranga.kotlin.inject.viewmodel.runtime.compose.injectedViewModel
import kotlinx.serialization.Serializable
import kotlin.random.Random

private const val LUCKY_NUMBER_RANGE = 100

@Serializable
data object MainScreen

@Composable
fun MainScreen(
    onUserLogin: (String) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val viewModel = injectedViewModel<SavedStateHandleViewModel, SavedStateHandleViewModel.Factory>(
                creationCallback = { factory ->
                    factory(createSavedStateHandle())
                },
            )
            val assistedViewModel = injectedViewModel<AssistedViewModel, AssistedViewModel.Factory>(
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
                    onUserLogin(viewModel.userName.text.toString())
                }
            ) {
                Text("Login")
            }
        }
    }
}
