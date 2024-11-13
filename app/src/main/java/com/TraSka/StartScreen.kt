package com.TraSka

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.ScreenFlowHandler

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StartScreen(navController: NavController, viewModel: LocationViewModel) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.background_welcome_v2),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        alpha = 1f
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            alignment = Alignment.Center,
            painter = painterResource(R.drawable.traska),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                navController.navigate(ScreenFlowHandler.RoutePlannerScreen.route)
            },
            modifier = Modifier
                .width(150.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(Color(0xFF222831)),
            shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp)
        ) {
            Text(
                text = "Guest",
                fontSize = 16.sp,
                letterSpacing = (5).sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                navController.navigate(ScreenFlowHandler.LoginScreen.route)
            },
            modifier = Modifier
                .width(150.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(Color(0xFF139aff)),
            shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp)
        ) {
            Text(
                text = "Account",
                fontSize = 15.sp,
                letterSpacing = (2).sp,
                color = Color.White,
                fontWeight = FontWeight(500)
            )
        }
    }

}