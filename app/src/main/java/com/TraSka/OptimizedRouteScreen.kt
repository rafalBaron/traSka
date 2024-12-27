package com.TraSka.com.TraSka

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.Route
import com.TraSka.ScreenFlowHandler
import com.TraSka.ShowAlertDialog
import com.TraSka.User
import com.TraSka.Vehicle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OptimizedRouteScreen(navController: NavController, viewModel: LocationViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val openSaveAlertDialog = remember { mutableStateOf(false) }

    when {
        openSaveAlertDialog.value -> {
            SaveRouteDialog(openSaveAlertDialog, onDismissRequest = {}, onSave = { name ->
                viewModel.currentOptimizedRoute!!.name = name
                viewModel.currentOptimizedRoute?.let {
                    viewModel.saveRoute(
                        currentUser!!.userData!!.uid,
                        it,
                        context
                    )
                }
            })
        }
    }
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
                .weight(2f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Origin route",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
            viewModel.currentNotOptimizedRoute?.let {
                RouteDetails(
                    modifier = Modifier.weight(5f),
                    it
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Optimized by TraSka",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
            RouteDetails(modifier = Modifier.weight(5f), route = viewModel.currentOptimizedRoute!!)
        }
        Spacer(modifier = Modifier.height(10.dp))
        ButtonsSection(
            modifier = Modifier.weight(1f),
            navController = navController,
            viewModel = viewModel,
            context = context,
            openSaveAlertDialog
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun RouteDetails(modifier: Modifier, route: Route) {
    val drawableIdMap = mapOf(
        "driving" to R.drawable.small_car_dark,
        "walking" to R.drawable.walking,
        "bicycling" to R.drawable.bicycling
    )

    val drawableIdMap2 = mapOf(
        "small_car" to R.drawable.small_car_dark,
        "big_car" to R.drawable.big_car_dark,
        "motorbike" to R.drawable.motorbike_dark,
        "truck" to R.drawable.truck_dark
    )

    val waypoints = route.point?.drop(1)!!.dropLast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                text = route.point.first().address!!,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Image(
                modifier = Modifier
                    .size(50.dp)
                    .fillMaxWidth()
                    .weight(0.3f),
                painter = painterResource(R.drawable.to),
                contentDescription = "to"
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = route.point.last().address!!,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    "Travel mode",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D99FF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Image(
                    modifier = Modifier
                        .size(35.dp)
                        .fillMaxWidth(),
                    painter = painterResource(drawableIdMap[route.travelMode]!!),
                    contentDescription = "to"
                )
            }
            if (route.vehicle != null) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        "Vehicle",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D99FF),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .size(35.dp)
                                .fillMaxWidth(),
                            painter = painterResource(drawableIdMap2[route.vehicle?.type]!!),
                            contentDescription = "vehicle"
                        )
                        Text(
                            route.vehicle!!.name!!,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    "Distance",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D99FF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    String.format("%.1f", (route.len!! / 1000)) + " km",
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
            if (route.co2 != null) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "CO2",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D99FF),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        String.format("%.1f", route.co2).replace(",", ".") + " kg",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun ButtonsSection(
    modifier: Modifier,
    navController: NavController,
    viewModel: LocationViewModel,
    context: Context,
    openSaveAlertDialog: MutableState<Boolean>
) {
    Column(
        modifier
            .fillMaxSize()
    ) {
        Button(
            onClick = {
                viewModel.currentOptimizedRoute?.let {
                    viewModel.openGoogleMaps(
                        context = context,
                        route = it
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f)
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF))
        ) {
            Text(
                "Open in Google Maps",
                color = Color.White,
                fontSize = 17.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(end = 5.dp)
                    .border(1.dp, Color(0xFF0D99FF), RoundedCornerShape(5.dp)),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Text("Edit route")
            }
            Button(
                onClick = {
                    openSaveAlertDialog.value = true
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(start = 5.dp)
                    .border(1.dp, Color(0xFF0D99FF), RoundedCornerShape(5.dp)),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Text("Save route")
            }
        }
    }
}

@Composable
fun SaveRouteDialog(
    showDialog: MutableState<Boolean>, onDismissRequest: () -> Unit, onSave: (String) -> Unit
) {
    val name = remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = { showDialog.value = false }, title = {
        Text(
            "Enter route name", fontWeight = FontWeight.Bold
        )
    }, text = {
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Name") },
            textStyle = TextStyle.Default.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0D99FF),
                unfocusedBorderColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .width(280.dp)
                .height(60.dp),
            maxLines = 1
        )
    }, confirmButton = {
        Button(
            onClick = {
                onSave(name.value)
                showDialog.value = false
            }, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                Color.Black
            )
        ) {
            Text(
                "Save", fontSize = 18.sp
            )
        }
    }, dismissButton = {
        Button(
            onClick = { showDialog.value = false },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                Color.Black
            )
        ) {
            Text(
                "Cancel", fontSize = 18.sp
            )
        }
    })
}

