package com.TraSka

import android.os.Build
import android.text.method.TextKeyListener.Capitalize
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StartScreen(navController: NavController, viewModel: LocationViewModel) {
    val scrollState = rememberScrollState()
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.background_welcome),
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            modifier = Modifier
                .align(Alignment.Center)
                .size(125.dp)
                .clip(CircleShape)
                .padding(10.dp),
            onClick = {
                navController.navigate(Screen.RoutePlanner.route)
            },
            colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.navi_arrow),
                contentDescription = "Mój obrazek",
                modifier = Modifier.size(50.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun CardContent() {
    Box(modifier = Modifier
        .fillMaxSize(),
        Alignment.Center
        ) {
        Image(
            painter = painterResource(R.drawable.maps_bck),
            contentDescription = "maps back",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .alpha(0.7f)
        )
        Column {
            Text(text = "Start Planning Immediately!",
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(Color.Black)
                    .padding(15.dp)
                    .width(200.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                )
        }
    }
}

@Composable
fun UserContent() {
    Box(modifier = Modifier
        .fillMaxSize(),
        Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.userback),
            contentDescription = "https://pngtree.com/freebackground/blue-business-technology-user_912519.html'>free background photos from pngtree.com",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .alpha(1f)
        )
        Column {
            Text(text = "Login Or Create Account",
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(Color.Black)
                    .padding(15.dp)
                    .width(200.dp),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}