package com.TraSka.com.TraSka

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.Route
import com.TraSka.ScreenFlowHandler
import com.TraSka.User
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController, viewModel: LocationViewModel) {
    val currentUser: User? = viewModel.getUser()
    val context = LocalContext.current
    val drawableIdMap = mapOf(
        "driving" to R.drawable.small_car_dark,
        "walking" to R.drawable.walking,
        "bicycling" to R.drawable.bicycling
    )
    val openAlertDialog = remember { mutableStateOf(false) }

    if (currentUser != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF222831))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        "Recently saved routes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                if (viewModel.currentSavedRoutes.isNotEmpty()) {
                    val routesSub = if (viewModel.currentSavedRoutes.size >= 5) {
                        viewModel.currentSavedRoutes.takeLast(5).reversed()
                    } else {
                        viewModel.currentSavedRoutes.reversed()
                    }

                    val pagerState =
                        rememberPagerState(initialPage = 0, pageCount = { routesSub.size })
                    val coroutineScope = rememberCoroutineScope()

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) { index ->
                        Card(
                            elevation = CardDefaults.cardElevation(3.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2C333F)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Text(
                                    routesSub[index].name!!.uppercase(),
                                    color = Color(0xFF0D99FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                                            text = routesSub[index].point!!.first().address!!,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        Image(
                                            modifier = Modifier.size(50.dp),
                                            painter = painterResource(R.drawable.to_down_dark),
                                            contentDescription = "to_dark down",
                                        )
                                        Text(
                                            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                                            text = routesSub[index].point!!.last().address!!,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(Color.White)
                                                .padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                "Travel mode",
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Image(
                                                modifier = Modifier.size(35.dp),
                                                painter = painterResource(drawableIdMap[routesSub[index].travelMode]!!),
                                                contentDescription = "to_dark down",

                                                )
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(Color.White)
                                                .padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                "Distance",
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                String.format(
                                                    "%.1f",
                                                    (routesSub[index].len!! / 1000)
                                                ) + " km",
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(routesSub.size) { pageIndex ->
                            val isSelected = pageIndex == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White else Color.Gray
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(pageIndex)
                                        }
                                    }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "You don't have any saved routes yet.\nSave your first one!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF2C333F))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Saved routes",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Button(
                        modifier = Modifier
                            .size(40.dp, 40.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(5.dp),
                        onClick = {
                            navController.navigate(ScreenFlowHandler.RoutePlannerScreen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D99FF))
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "add route",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.currentSavedRoutes) { route ->
                        RouteItem(navController, viewModel, context, route)
                    }
                }
            }
        }
    }
}


//region Composables

@Composable
fun RouteItem(
    navController: NavController,
    viewModel: LocationViewModel,
    context: Context,
    route: Route
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(5.dp))
            .background(Color.White)
            .padding(10.dp)
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                route.name?.let {
                    Text(
                        modifier = Modifier.padding(start = 10.dp, end = 20.dp),
                        text = it.uppercase(Locale.getDefault()),
                        color = Color(0xFF0D99FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    modifier = Modifier.padding(start = 10.dp, end = 20.dp),
                    text = "Distance: " + String.format("%.1f", (route.len!! / 1000)) + " km",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.delRoute(route, context)
                    },
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                        .weight(0.5f),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFFEBF5FC))
                ) {
                    Image(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        viewModel.routePoints = route.point!!
                        route.travelMode?.let { viewModel.updateSelectedOption(it) }
                        if (route.travelMode == "bicycling" || route.travelMode == "walking") {
                            viewModel.updateSelectedCar("")
                        }
                        if (route.vehicle != null) {
                            route.vehicle!!.name?.let { viewModel.updateSelectedCar(it) }
                        }
                        navController.navigate(ScreenFlowHandler.RoutePlannerScreen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(10.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFFEBF5FC))
                ) {
                    Text(
                        "Open in Planner",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        viewModel.openGoogleMaps(route, context)
                    },
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(10.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFFEBF5FC))
                ) {
                    Text(
                        "Google Maps",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

//endregion