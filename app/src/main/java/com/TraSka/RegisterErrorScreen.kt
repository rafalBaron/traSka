package com.TraSka

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RegisterErrorScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.error),
                contentDescription = null,
                modifier = Modifier.size(100.dp, 100.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Something went wrong",
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = { navController.navigate(ScreenFlowHandler.LoginScreen.route) },
                colors = ButtonDefaults.buttonColors(Color.Black),
                shape = RoundedCornerShape(10),
                modifier = Modifier.size(width = 150.dp, height = 40.dp),
            )
            {
                Text(
                    text = "Go back",
                    fontSize = 16.sp,
                    letterSpacing = 2.sp,
                    color = Color.White
                )
            }
        }
    }
}
