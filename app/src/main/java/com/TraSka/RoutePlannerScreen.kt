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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("StateFlowValueCalledInComposition", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RoutePlannerScreen(
    navController: NavController,
    viewModel: LocationViewModel,
) {
    val context = LocalContext.current
    val openAlertDialog = remember { mutableStateOf(false) }
    val notLoggedAlert = remember { mutableStateOf(false) }
    var travelOptionExpanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("driving") }
    val carSelect = listOf("Golf", "Honda", "Toyota")
    var selectedCar by remember { mutableStateOf(carSelect[0]) }
    val travelOptions = listOf("driving", "bicycling", "walking")
    var carSelectExpanded by remember { mutableStateOf(false) }
    val drawableIdMap = mapOf(
        "driving" to R.drawable.driving,
        "walking" to R.drawable.walking,
        "bicycling" to R.drawable.bicycling
    )
    var tollsChecked by remember { mutableStateOf(false) }
    var highwaysChecked by remember { mutableStateOf(false) }
    var travelOptionParentSize by remember { mutableStateOf(IntSize.Zero) }
    var carSelectParentSize by remember { mutableStateOf(IntSize.Zero) }
    var avoid by remember { mutableStateOf("") }

    when {
        notLoggedAlert.value -> {
            AlertDialogExample(onDismissRequest = {
                notLoggedAlert.value = false
            }, onConfirmation = {
                notLoggedAlert.value = false
                navController.navigate(ScreenFlowHandler.LoginScreen.route)
            }, dialogTitle = "Saving only for logged users!", icon = Icons.Default.Lock
            )
        }
    }

    Column(
        modifier = Modifier
            .background(Color(0xFF222831))
            .padding(10.dp, 10.dp, 10.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        travelOptionParentSize = coordinates.size
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Travel mode",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                )
                Button(
                    onClick = { travelOptionExpanded = true },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        Color(0xFF0D99FF)
                    )
                ) {
                    Row (
                        horizontalArrangement = Arrangement.SpaceAround,
                    ){
                        Image(
                            modifier = Modifier.size(25.dp, 25.dp),
                            painter = painterResource(drawableIdMap[selectedOption]!!),
                            contentDescription = "$selectedOption option"
                        )
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Arrow dropdown"
                        )
                    }
                }
                DropdownMenu(modifier = Modifier.width(with(LocalDensity.current) { travelOptionParentSize.width.toDp() }),
                    expanded = travelOptionExpanded,
                    onDismissRequest = { travelOptionExpanded = false }) {
                    travelOptions.forEach { option ->
                        DropdownMenuItem(modifier = Modifier, text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                drawableIdMap[option]?.let { painterResource(it) }?.let {
                                    Image(
                                        modifier = Modifier.size(20.dp, 20.dp),
                                        painter = it,
                                        contentDescription = "$option option"
                                    )
                                    Text(option)
                                }
                            }
                        }, onClick = {
                            selectedOption = option
                            travelOptionExpanded = false
                        })
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Avoid tolls",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                )
                Checkbox(
                    modifier = Modifier.graphicsLayer(
                        scaleX = 1.5f, scaleY = 1.5f
                    ),
                    checked = tollsChecked,
                    onCheckedChange = { tollsChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF0D99FF),
                        uncheckedColor = Color(0xFF0D99FF),
                        checkmarkColor = Color.White,
                        disabledCheckedColor = Color.Gray,
                        disabledUncheckedColor = Color.Gray
                    ),
                    enabled = !(selectedOption == "walking" || selectedOption == "bicycling")
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Avoid highways",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                )
                Checkbox(
                    modifier = Modifier.graphicsLayer(
                        scaleX = 1.5f, scaleY = 1.5f
                    ),
                    checked = highwaysChecked,
                    onCheckedChange = { highwaysChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF0D99FF),
                        uncheckedColor = Color(0xFF0D99FF),
                        checkmarkColor = Color.White,
                        disabledCheckedColor = Color.Gray,
                        disabledUncheckedColor = Color.Gray
                    ),
                    enabled = !(selectedOption == "walking" || selectedOption == "bicycling")
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        carSelectParentSize = coordinates.size
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Select car",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.sp
                )
                Button(
                    onClick = { carSelectExpanded = true },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D99FF),
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White,

                        ),
                    enabled = !(selectedOption == "walking" || selectedOption == "bicycling")
                ) {
                    Row (
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Text(selectedCar)
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Arrow dropdown"
                        )
                    }
                }
                DropdownMenu(modifier = Modifier.width(with(LocalDensity.current) { carSelectParentSize.width.toDp() }),
                    expanded = carSelectExpanded,
                    onDismissRequest = { carSelectExpanded = false }) {
                    carSelect.forEach { car ->
                        DropdownMenuItem(modifier = Modifier, text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(car)
                            }
                        }, onClick = {
                            selectedCar = car
                            carSelectExpanded = false
                        })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextFieldBackground(Color.White) {
                OutlinedTextField(
                    value = viewModel.text,
                    onValueChange = {
                        viewModel.text = it
                        viewModel.searchPlaces(it)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(55.dp)
                        .fillMaxWidth(0.85f),
                    maxLines = 1,
                    placeholder = { Text("Search for address", color = Color.LightGray) }
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
                            viewModel.currentLatLong.latitude, viewModel.currentLatLong.longitude
                        )
                        point.address = viewModel.text
                        point.id = viewModel.currentPointId
                        viewModel.addPoint(point)
                        viewModel.text = ""
                        viewModel.locationAutofill.clear()
                    } else {
                        Toast.makeText(
                            context, "Maximum number of points! (12)", Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(55.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF))
            ) {
                Image(
                    modifier = Modifier.size(30.dp, 30.dp),
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null,
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (viewModel.locationAutofill.size > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                        .zIndex(2f)
                        .offset(0.dp, (-7).dp),
                    color = Color.White,
                    shape = RoundedCornerShape(0.dp, 0.dp, 5.dp, 5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(
                            viewModel.locationAutofill.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(7.dp),
                            ) {
                                items(viewModel.locationAutofill) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(shape = RoundedCornerShape(5.dp))
                                            .background(Color(0xFFA7D8FC))
                                            .clickable {
                                                viewModel.text = it.address
                                                viewModel.locationAutofill.clear()
                                                viewModel.getCoordinates(it)
                                            }, verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            Icons.Filled.Place,
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
                    .padding(0.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(5.dp))
                        .heightIn(min = 0.dp, max = 175.dp)
                        .background(Color(0xFF455163))
                        .padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    itemsIndexed(viewModel.routePoints) { index, point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .clip(shape = RoundedCornerShape(5.dp))
                                .background(Color.White),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (index == 0) {
                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .size(40.dp, 30.dp)
                                        .padding(10.dp, 0.dp, 10.dp, 0.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(Color.White)
                                ) {
                                    Image(
                                        imageVector = Icons.Filled.Home, contentDescription = "Home"
                                    )
                                }
                            } else if (index == viewModel.routePoints.size - 1) {
                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .size(40.dp, 30.dp)
                                        .padding(10.dp, 0.dp, 10.dp, 0.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(Color.White)
                                ) {
                                    Image(
                                        imageVector = Icons.Filled.Place,
                                        contentDescription = "Place"
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(5.dp))
                                        .size(40.dp, 30.dp)
                                        .padding(10.dp, 0.dp, 10.dp, 0.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(Color.White)
                                ) {}
                            }
                            Text(
                                point.address.toString(),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                maxLines = 1
                            )
                            Button(
                                onClick = {
                                    viewModel.delPoint(point)
                                },
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(5.dp))
                                    .size(40.dp, 40.dp)
                                    .padding(0.dp, 0.dp, 0.dp, 0.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(Color.White)
                            ) {
                                Image(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Delete",
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))/*Row(
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
                                        "Choose at least 2 pointes!",
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
                            text = "Save Route", textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (viewModel.routePoints.size > 1) {
                                viewModel.sendRequestOpenMaps(context = context, selectedOption)
                            } else {
                                Toast.makeText(
                                    context, "Choose at least 2 pointes!", Toast.LENGTH_SHORT
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
                }*/

                var cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(viewModel.currentLatLong, 2f)
                }
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(4f)
                        .clip(shape = RoundedCornerShape(5.dp)),
                    cameraPositionState = cameraPositionState
                ) {
                    for (point in viewModel.routePoints) {
                        AdvancedMarker(
                            state = MarkerState(
                                position = LatLng(
                                    point.latLng!![0], point.latLng!![1]
                                )
                            ), title = point.address
                        )
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(
                                point.latLng!![0], point.latLng!![1]
                            ), 10f
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        avoid = if (highwaysChecked && tollsChecked) "highways|tolls" else if (highwaysChecked) "highways" else if (tollsChecked) "tolls" else ""
                        if (viewModel.routePoints.size > 1) {
                            viewModel.sendRequestOpenMaps(context = context, selectedOption, avoid)
                        } else {
                            Toast.makeText(
                                context, "Choose at least 2 points!", Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF))
                ) {
                    Text("Optimize")
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

//region Composables

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    icon: ImageVector,
) {
    AlertDialog(icon = {
        Image(
            painter = painterResource(R.drawable.acc),
            contentDescription = "acc",
            modifier = Modifier.size(80.dp, 80.dp)
        )

    }, title = {
        Text(text = dialogTitle)
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {
        TextButton(
            onClick = {
                onConfirmation()
            }, colors = ButtonDefaults.buttonColors(Color.Black), shape = RoundedCornerShape(5.dp)
        ) {
            Text(
                "Login", fontSize = 18.sp

            )
        }
    }, dismissButton = {
        TextButton(
            onClick = {
                onDismissRequest()
            }, colors = ButtonDefaults.buttonColors(Color.Black), shape = RoundedCornerShape(5.dp)
        ) {
            Text(
                "Cancel", fontSize = 18.sp
            )
        }
    })
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ShowAlertDialog(
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

@Composable
fun OutlinedTextFieldBackground(color: Color, content: @Composable () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color, shape = RoundedCornerShape(5.dp)
                )
        )
        content()
    }
}

//endregion