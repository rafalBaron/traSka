package com.TraSka

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RoutePlanner(
    navController: NavController,
    viewModel: LocationViewModel,
) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf("driving") }
    val openAlertDialog = remember { mutableStateOf(false) }
    val notLoggedAlert = remember { mutableStateOf(false) }

    when {
        notLoggedAlert.value -> {
            AlertDialogExample(
                onDismissRequest = {
                    notLoggedAlert.value = false
                },
                onConfirmation = {
                    notLoggedAlert.value = false
                    navController.navigate(Screen.WelcomeScreen.route)
                },
                dialogTitle = "Saving only for logged users!",
                icon = Icons.Default.Lock
            )
        }
    }
    Scaffold(
        topBar = {
            Row (){
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(20.dp,20.dp,0.dp,0.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(Color(0xFF248A12))
                        .size(40.dp,50.dp)
                        .clickable {
                            navController.navigateUp()
                        },
                )
            }
        }
    ) {
        Column(modifier = Modifier.background(Color(0xFF222831))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(70.dp, 20.dp, 20.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { selectedOption = "driving" },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    border = if (selectedOption == "driving") BorderStroke(
                        2.dp,
                        Color(0xFF0D99FF)
                    ) else null,
                    colors = ButtonDefaults.buttonColors(
                        if (selectedOption == "driving") Color.White else Color(0xFF0D99FF)
                    )
                ) {
                    Image(
                        modifier = Modifier.size(25.dp, 25.dp),
                        painter = painterResource(R.drawable.car),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { selectedOption = "walking" },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    border = if (selectedOption == "walking") BorderStroke(
                        2.dp,
                        Color(0xFF0D99FF)
                    ) else null,
                    colors = ButtonDefaults.buttonColors(
                        if (selectedOption == "walking") Color.White else Color(0xFF0D99FF)
                    )
                ) {
                    Image(
                        modifier = Modifier.size(25.dp, 25.dp),
                        painter = painterResource(R.drawable.person),
                        contentDescription = null
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp,10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextFieldBackground(Color.White) {
                    OutlinedTextField(
                        value = viewModel.text, onValueChange = {
                            viewModel.text = it
                            viewModel.searchPlaces(it)
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF0D99FF),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(280.dp)
                            .height(55.dp),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        if (viewModel.text.isBlank()) {
                            Toast.makeText(context, "Input address!", Toast.LENGTH_SHORT).show()
                        } else if (viewModel.routePoints.size < 13) {
                            var point = Point()
                            point.latLng = listOf(
                                viewModel.currentLatLong.latitude,
                                viewModel.currentLatLong.longitude
                            )
                            point.address = viewModel.text
                            point.id = viewModel.currentPointId
                            viewModel.addPoint(point)
                            viewModel.text = ""
                            viewModel.locationAutofill.clear()
                        } else {
                            Toast.makeText(
                                context,
                                "Maximum number of points! (12)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .size(80.dp, 55.dp)
                        .weight(2f),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF))
                ) {
                    Image(
                        modifier = Modifier.size(20.dp, 20.dp),
                        painter = painterResource(R.drawable.plus),
                        contentDescription = null
                    )
                }
            }
            Box() {
                if (viewModel.locationAutofill.size > 0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp, 0.dp, 93.dp, 0.dp)
                            .zIndex(2f)
                            .offset(0.dp, (-27).dp),
                        color = Color.White,
                        shape = RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedVisibility(
                                viewModel.locationAutofill.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = if (viewModel.locationAutofill.size >= 5) Modifier.height(
                                        285.dp
                                    ) else Modifier.height((viewModel.locationAutofill.size * 57).dp)
                                ) {
                                    items(viewModel.locationAutofill) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(1.dp)
                                                .height(50.dp)
                                                .clip(shape = RoundedCornerShape(10.dp))
                                                .background(Color(0xfff5f5f5))
                                                .clickable {
                                                    viewModel.text = it.address
                                                    viewModel.locationAutofill.clear()
                                                    viewModel.getCoordinates(it)
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                Icons.Filled.LocationOn,
                                                contentDescription = "IconLocation"
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(it.address)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp, 0.dp, 20.dp, 0.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .height(if (viewModel.routePoints.size == 1 || viewModel.routePoints.size == 2) 135.dp else if (viewModel.routePoints.size == 0) 0.dp else 200.dp)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(if (viewModel.routePoints.isNotEmpty()) Color.Black else Color.Transparent)
                            .padding(10.dp)
                    ) {
                        itemsIndexed(viewModel.routePoints) { index, point ->
                            AnimatedVisibility(visible = true) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(55.dp)
                                        .padding(0.dp, 5.dp, 0.dp, 0.dp)
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .animateItemPlacement(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (index == 0) {
                                        Button(
                                            onClick = {},
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(10.dp))
                                                .size(40.dp, 30.dp)
                                                .padding(10.dp, 0.dp, 0.dp, 0.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(Color.White)
                                        )
                                        {
                                            Image(
                                                imageVector = Icons.Filled.Home,
                                                contentDescription = "Home"
                                            )
                                        }
                                    } else if (index == viewModel.routePoints.size - 1) {
                                        Button(
                                            onClick = {},
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(10.dp))
                                                .size(40.dp, 30.dp)
                                                .padding(10.dp, 0.dp, 0.dp, 0.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(Color.White)
                                        )
                                        {
                                            Image(
                                                imageVector = Icons.Filled.Place,
                                                contentDescription = "Place"
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = {},
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(10.dp))
                                                .size(40.dp, 30.dp)
                                                .padding(10.dp, 0.dp, 0.dp, 0.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(Color.White)
                                        )
                                        {
                                            Image(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = "Waypoint"
                                            )
                                        }
                                    }
                                    Text(
                                        point.address.toString(),
                                        modifier = Modifier
                                            .width(225.dp)
                                            .wrapContentHeight(align = Alignment.CenterVertically),
                                        maxLines = 1,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Image(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(0.dp, 0.dp, 13.dp, 0.dp)
                                            .size(20.dp, 20.dp)
                                            .clickable { viewModel.delPoint(point) }
                                    )
                                }
                            }
                        }
                    }
                    if (viewModel.routePoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(2f)
                            .padding(0.dp, 0.dp, 0.dp, 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                if (viewModel.isLogged) {
                                    if (viewModel.routePoints.size > 1) {
                                        openAlertDialog.value = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Choose at least 2 points!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    notLoggedAlert.value = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                Color.Black
                            )
                        ) {
                            Text(
                                text = "Save Route",
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (viewModel.routePoints.size > 1) {
                                    viewModel.sendRequestOpenMaps(context = context, selectedOption)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Choose at least 2 points!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                Color.Black
                            )
                        ) {
                            Text(
                                text = "Calculate and open in Google Maps",
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    var cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(viewModel.currentLatLong, 2f)
                    }
                    GoogleMap(
                        modifier = Modifier
                            .zIndex(1f)
                            .padding(0.dp, 0.dp, 0.dp, 20.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        cameraPositionState = cameraPositionState
                    ) {
                        for (point in viewModel.routePoints) {
                            AdvancedMarker(
                                state = MarkerState(
                                    position = LatLng(
                                        point.latLng!![0],
                                        point.latLng!![1]
                                    )
                                ),
                                title = point.address
                            )
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(
                                    LatLng(
                                        point.latLng!![0],
                                        point.latLng!![1]
                                    ), 10f
                                )
                        }
                    }
                }
            }
        }
    }
    when {
        openAlertDialog.value -> {
            ShowAlertDialog(openAlertDialog, onDismissRequest = {}, onSave = { name ->
                viewModel.sendRequestSaveRoute(context, selectedOption, name)
            })
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
            Image(
                painter = painterResource(R.drawable.acc), contentDescription = "acc",
                modifier = Modifier.size(80.dp, 80.dp)
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
                Text(
                    "Login",
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
                Text(
                    "Cancel",
                    fontSize = 18.sp
                )
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ShowAlertDialog(
    showDialog: MutableState<Boolean>,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit
) {
    val name = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = {
            Text(
                "Enter route name",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name.value, onValueChange = { name.value = it },
                label = { Text("Name") },
                textStyle = TextStyle.Default.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF0D99FF),
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .width(280.dp)
                    .height(60.dp),
                maxLines = 1
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name.value)
                    showDialog.value = false
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    Color.Black
                )
            ) {
                Text(
                    "Save",
                    fontSize = 18.sp
                )
            }
        },
        dismissButton = {
            Button(
                onClick = { showDialog.value = false },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    Color.Black
                )
            ) {
                Text(
                    "Cancel",
                    fontSize = 18.sp
                )
            }
        }
    )
}

@Composable
fun OutlinedTextFieldBackground(color: Color, content: @Composable () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color, shape = RoundedCornerShape(10.dp)
                )
        )
        content()
    }
}