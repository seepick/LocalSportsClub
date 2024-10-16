package com.github.christophpickl.localsportsclub

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LocalSportsClub",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        Buttons()
    }
}


@Composable
fun Buttons() {
    MaterialTheme {
        Row {
            Button(onClick = {
                println("clicked")
            }) {
                Text("Haha")
            }
        }
    }
}