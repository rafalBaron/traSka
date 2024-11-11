package com.TraSka.com.TraSka

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.Screen

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StartScreen(navController: NavController, viewModel: LocationViewModel) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.background_welcome_v2),
        contentDescription = null,
        contentScale = ContentScale.FillHeight,
        alpha = 1f
    )
    Column(modifier = Modifier
        .fillMaxSize()
        ){
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            alignment = Alignment.Center,
            painter = painterResource(R.drawable.traska),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(100.dp))
        Button(onClick = {
            navController.navigate(Screen.RoutePlanner.route)
        },
            modifier = Modifier
                .width(200.dp)
                .height(70.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors()
            ) {
            Text(
                text = "Let's go!",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }

}