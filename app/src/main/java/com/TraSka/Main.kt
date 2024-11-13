package com.TraSka

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import com.TraSka.ui.theme.TraSkaTheme
import com.google.firebase.FirebaseApp

class Main() : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(
        savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TraSkaTheme {
                FirebaseApp.initializeApp(this)
                val viewModel = LocationViewModel(LocalContext.current)
                Navigation(viewModel)
            }
        }
    }
}

