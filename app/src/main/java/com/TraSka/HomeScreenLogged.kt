package com.TraSka.com.TraSka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.Route
import com.TraSka.Screen
import com.TraSka.User
import com.TraSka.ui.theme.Purple40
import com.TraSka.ui.theme.Purple80
import com.TraSka.ui.theme.PurpleGrey40
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale
import kotlin.math.roundToLong

private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenLogged(navController: NavController, viewModel: LocationViewModel) {
    val currentUser: User? = viewModel.getUser()
    val routes = remember { mutableStateOf(viewModel.currentSavedRoutes) }
    val openAlertDialog = remember { mutableStateOf(false) }

    if (viewModel.isLogged) {
        when {
            openAlertDialog.value -> {
               AlertDialogExample(
                    onDismissRequest = {
                        openAlertDialog.value = false
                    },
                    onConfirmation = {
                        openAlertDialog.value = false
                        viewModel
                            .clearViewModel()
                            .also {
                                navController.navigate(Screen.StartScreen.route)
                            }
                        println("Logout")
                    },
                    dialogTitle = "Do you want to log out?",
                    icon = Icons.Default.AccountCircle
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(0.dp, 0.dp, 50.dp, 50.dp))
                        .background(Color(0xFF222831)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .size(80.dp, 80.dp)
                            .clickable {
                                openAlertDialog.value = true
                            },
                        painter = painterResource(R.drawable.acc),
                        contentDescription = null,
                    )
                    Text(
                        text = currentUser?.userData?.login!!,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D99FF),
                    )
                }

                Row(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier.size(60.dp, 60.dp),
                        painter = painterResource(R.drawable.route),
                        contentDescription = null
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Routes",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = currentUser?.savedRoutes!!.size.toString(),
                            color = Color(0xFF0D99FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kilometers",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        var km = 0f
                        for (route in currentUser!!.savedRoutes!!) {
                            km += route.len!!
                        }
                        Text(
                            text = (km / 1000).toInt().toString(),
                            color = Color(0xFF0D99FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(50.dp, 50.dp, 0.dp, 0.dp))
                    .background(Color(0xFF222831)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(5f)
                ) {
                    if (routes.value.isNotEmpty()) {
                        var routesSub = emptyList<Route>()

                        if (routes.value.size >= 5) {
                            routesSub =
                                routes.value.subList(
                                    routes.value.size - 5,
                                    routes.value.lastIndex + 1
                                )
                            routesSub = routesSub.reversed()
                        } else {
                            routesSub = routes.value
                            routesSub = routesSub.reversed()
                        }

                        val pagerState =
                            rememberPagerState(initialPage = 0, 0f) { routesSub.size }
                        Text(
                            text = "Last saved routes",
                            modifier = Modifier
                                .padding(15.dp, 5.dp, 15.dp, 0.dp)
                                .clip(shape = RoundedCornerShape(40.dp, 40.dp, 10.dp, 10.dp))
                                .background(Color.Black)
                                .height(50.dp)
                                .fillMaxWidth()
                                .wrapContentHeight(align = Alignment.CenterVertically),
                            color = Color(0xFFFFFFFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center,
                        )
                        HorizontalPager(
                            state = pagerState
                        ) { index ->
                            Card(
                                elevation = CardDefaults.cardElevation(3.dp),
                                modifier = Modifier.padding(15.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF272E38))
                                ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .height(400.dp)
                                        .padding(0.dp)
                                        //.border(BorderStroke(1.dp,Color(0xFF0D99FF)),shape = RoundedCornerShape(10.dp))
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        //.background(Color(0xFF272E38))
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    routesSub[index].name?.uppercase(Locale.getDefault())?.let {
                                        Text(
                                            text = it,
                                            modifier = Modifier.padding(15.dp),
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0D99FF)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(25.dp, 0.dp, 0.dp, 0.dp)
                                                .fillMaxSize(),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    0.dp,
                                                    0.dp,
                                                    0.dp,
                                                    0.dp
                                                )
                                            ) {
                                                Image(
                                                    imageVector = Icons.Filled.Home,
                                                    contentDescription = "Home",
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                routesSub[index].points?.first().let {
                                                    it!!.address?.let { it1 ->
                                                        Text(
                                                            text = it1,
                                                            fontSize = 16.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(25.dp))
                                            Row(
                                                modifier = Modifier.padding(0.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "Place",
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                if (routesSub[index].points!!.size > 2) {
                                                    Text(
                                                        text = "(" + (routesSub[index].points!!.size - 2).toString() + ")",
                                                        color = Color.White,
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(25.dp))
                                            Row(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                            ) {
                                                Image(
                                                    imageVector = Icons.Filled.Place,
                                                    contentDescription = "Place",
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                routesSub[index].points?.last().let {
                                                    it!!.address?.let { it1 ->
                                                        Text(
                                                            text = it1,
                                                            fontSize = 16.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(250.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            routesSub[index].len?.let {
                                                Text(
                                                    text = (it / 1000).toInt().toString() + " km",
                                                    color = Color.White,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(25.dp))
                                            routesSub[index].travelMode?.let {
                                                if (it == "driving") {
                                                    Image(
                                                        modifier = Modifier
                                                            .size(80.dp, 80.dp)
                                                            .padding(10.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(Color.White),
                                                        painter = painterResource(R.drawable.car),
                                                        contentDescription = "driving mode"
                                                    )
                                                } else {
                                                    Image(
                                                        modifier = Modifier
                                                            .size(80.dp, 80.dp)
                                                            .padding(10.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(Color.White),
                                                        painter = painterResource(R.drawable.person),
                                                        contentDescription = "walking mode"
                                                    )
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize(),

                            ) {
                            Text(
                                text = "Plan your first route!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { navController.navigate(Screen.RoutePlanner.route) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .size(50.dp, 50.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF))
                            ) {
                                Image(
                                    modifier = Modifier.size(25.dp, 25.dp),
                                    painter = painterResource(R.drawable.plus),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                if (routes.value.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = { navController.navigate(Screen.SavedRoutesScreen.route) },
                            modifier = Modifier
                                .height(110.dp)
                                .fillMaxWidth()
                                .padding(15.dp, 0.dp, 0.dp, 15.dp)
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.Black),
                            shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp)
                        ) {
                            Text(
                                text = "Saved routes",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Button(
                            onClick = {
                                navController.navigate(Screen.RoutePlanner.route)
                            },
                            modifier = Modifier
                                .height(110.dp)
                                .fillMaxWidth()
                                .padding(0.dp, 0.dp, 15.dp, 15.dp)
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF)),
                            shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp)
                        ) {
                            Text(
                                text = "Plan another route",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Image(painter = painterResource(R.drawable.acc), contentDescription = "acc",
                modifier = Modifier.size(80.dp,80.dp)
                )

        },
        title = {
            Text(text = dialogTitle)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Log out",
                    fontSize = 18.sp

                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Go back",
                fontSize = 18.sp)
            }
        }
    )
}

/*

@Preview(name = "HomeScreenLogged")
@Composable
private fun PreviewHomeScreen() {
    HomeScreenLogged()
}*/
