package com.TraSka.com.TraSka

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.ScreenFlowHandler
import com.TraSka.User
import com.TraSka.Vehicle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AccountScreen(navController: NavController, viewModel: LocationViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var keepMeLoggedIn by remember { mutableStateOf(false) }
    val logoutAlert = remember { mutableStateOf(false) }
    val usernameChangeAlert = remember { mutableStateOf(false) }
    val vehicleAddAlert = remember { mutableStateOf(false) }
    val context = LocalContext.current

    when {
        logoutAlert.value -> {
            LogoutDialog(
                onDismissRequest = {
                    logoutAlert.value = false
                },
                onConfirmation = {
                    logoutAlert.value = false
                    viewModel.clearViewModel()
                    navController.navigate(ScreenFlowHandler.StartScreen.route)
                    println("Sign out")
                },
                dialogTitle = "Do you want to sign out?",
            )
        }
    }
    when {
        usernameChangeAlert.value -> {
            ChangeUsernameDialog(usernameChangeAlert, onDismissRequest = {}, onSave = { name ->
                currentUser!!.userData!!.uid?.let {
                    viewModel.updateUsernameInFirebase(
                        userId = it,
                        newUsername = name,
                        context,
                        onSuccess = {
                            usernameChangeAlert.value = false
                            println("Username successfully updated!")
                        },
                        onFailure = { exception ->
                            println("Failed to update username: ${exception.message}")
                        }
                    )
                }
            })
        }
    }
    when {
        vehicleAddAlert.value -> {
            AddVehicleDialog(
                vehicleAddAlert,
                onDismissRequest = {},
                context = context,
                onSave = { name, avgFuelConsumption, type, fuelType ->
                    currentUser!!.userData!!.uid?.let {
                        try {
                            if (name.isEmpty() || avgFuelConsumption.isEmpty() || type.isEmpty() || fuelType.isEmpty()) {
                                throw Exception("FieldsEmpty")
                            }
                            if (name.length > 16) {
                                throw Exception("NameTooLong")
                            }
                            val avgFuelConsumptionFloat =
                                avgFuelConsumption.replace(",", ".").toFloat()
                            viewModel.saveVehicle(
                                currentUser!!.userData!!.uid,
                                Vehicle(name, avgFuelConsumptionFloat, type, fuelType),
                                context
                            )
                        } catch (e: Exception) {
                            if (e is NumberFormatException) {
                                Toast.makeText(
                                    context,
                                    "Invalid format of fuel consumption!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else if (e.message == "FieldsEmpty") {
                                Toast.makeText(
                                    context,
                                    "Please fill all fields!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else if (e.message == "NameTooLong") {
                                Toast.makeText(
                                    context,
                                    "Name is too long! Max 16 characters!",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
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
        UserDataSection(modifier = Modifier.weight(2f), currentUser, usernameChangeAlert)
        Spacer(modifier = Modifier.height(10.dp))
        SignOptionSection(
            modifier = Modifier.weight(0.25f),
            keepMeLoggedIn = keepMeLoggedIn,
            onCheckedChange = { keepMeLoggedIn = it },
            logoutAlert
        )
        Spacer(modifier = Modifier.height(10.dp))
        CarsSection(
            viewModel, modifier = Modifier.weight(2f), context, vehicleAddAlert
        )


    }

}

@Composable
fun UserDataSection(
    modifier: Modifier,
    currentUser: User? = null,
    usernameChangeAlert: MutableState<Boolean>
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(5.dp))
            .background(Color.Transparent)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                "Username",
                fontWeight = FontWeight.Normal,
                color = Color(0xFF0D99FF),
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${currentUser?.userData?.login}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Button(
                    modifier = Modifier
                        .size(40.dp, 40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(5.dp),
                    onClick = {
                        usernameChangeAlert.value = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D99FF))
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit username",
                        tint = Color.White
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                "Password",
                fontWeight = FontWeight.Normal,
                color = Color(0xFF0D99FF),
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "*********",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Button(
                    modifier = Modifier
                        .size(40.dp, 40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(5.dp),
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    /*Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit password",
                        tint = Color.White
                    )*/
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                "E-mail",
                fontWeight = FontWeight.Normal,
                color = Color(0xFF0D99FF),
                fontSize = 13.sp
            )

            Text(
                "${currentUser?.userData?.email}",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 17.sp
            )
        }
    }
}


@Composable
fun SignOptionSection(
    modifier: Modifier = Modifier,
    keepMeLoggedIn: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    logoutAlert: MutableState<Boolean>
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(5.dp))
            .padding(0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {/*
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Keep me logged in",
                fontWeight = FontWeight.Normal,
                color = Color.White,
                fontSize = 17.sp
            )
            Checkbox(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = 1.5f, scaleY = 1.5f
                    )
                    .offset { IntOffset(9, 0) },
                checked = keepMeLoggedIn,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF0D99FF),
                    uncheckedColor = Color(0xFF0D99FF),
                    checkmarkColor = Color.White,
                    disabledCheckedColor = Color.Gray,
                    disabledUncheckedColor = Color.Gray
                )
            )
        }*/
        Button(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(5.dp),
            onClick = { logoutAlert.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D99FF))
        ) {
            Text(
                "Sign out",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}


@Composable
fun CarsSection(
    viewModel: LocationViewModel,
    modifier: Modifier,
    context: Context,
    vehicleAddAlert: MutableState<Boolean>
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(5.dp))
            .background(Color(0xFF2C333F))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Your vehicles",
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
                    vehicleAddAlert.value = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D99FF))
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "add car",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (viewModel.currentSavedVehicles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "You don't have any vehicles yet.\nAdd your first one to start\nestimating CO2 emissions!",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.currentSavedVehicles) { vehicle ->
                    VehicleItem(viewModel, context, vehicle)
                }
            }
        }
    }
}

@Composable
fun VehicleItem(viewModel: LocationViewModel, context: Context, vehicle: Vehicle) {
    val drawableIdMap = mapOf(
        "small_car" to R.drawable.small_car_dark,
        "big_car" to R.drawable.big_car_dark,
        "motorbike" to R.drawable.motorbike_dark,
        "truck" to R.drawable.truck_dark
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(5.dp))
            .background(Color.White)
            .padding(10.dp, 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            drawableIdMap[vehicle.type]?.let { painterResource(it) }?.let {
                Image(
                    modifier = Modifier.size(20.dp, 20.dp),
                    painter = it,
                    contentDescription = vehicle.type
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            vehicle.name?.let { Text(text = it,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
                ) }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(20.dp, 20.dp),
                painter = painterResource(R.drawable.fuel_dark),
                contentDescription = "fuel icon"
            )
            Spacer(modifier = Modifier.width(10.dp))
            vehicle.avgFuelConsumption?.let { Text(text = it.toString() + " L/100km", fontSize = 13.sp) }
        }
        Row(
            modifier = Modifier.weight(0.5f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    viewModel.delVehicle(vehicle, context)
                },
                modifier = Modifier
                    .size(40.dp, 40.dp)
                    .padding(0.dp, 0.dp, 0.dp, 0.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFEBF5FC))
            ) {
                Image(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                )
            }
        }
    }
}

@Composable
fun LogoutDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = {
            Text(text = dialogTitle, color = Color.White)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                },
                colors = ButtonDefaults.textButtonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text(
                    "Sign out",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF0D99FF)),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text(
                    "Cancel",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        containerColor = Color(0xFF2C333F),
        tonalElevation = 5.dp,
        shape = RoundedCornerShape(5.dp)
    )
}

@Composable
fun ChangeUsernameDialog(
    showDialog: MutableState<Boolean>, onDismissRequest: () -> Unit, onSave: (String) -> Unit
) {
    val name = remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = { showDialog.value = false }, title = {
        Text(
            "Change your username", fontWeight = FontWeight.Bold, color = Color.White
        )
    }, text = {
        OutlinedTextFieldBackground(color = Color.White) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(PaddingValues(0.dp)),
                value = name.value,
                onValueChange = { name.value = it; },
                placeholder = {
                    Text(
                        text = "New username",
                        style = TextStyle(color = Color.Gray),
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight(500)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF222831),
                    textDecoration = TextDecoration.None,
                )
            )
        }
    }, confirmButton = {
        Button(
            onClick = {
                onSave(name.value)
                showDialog.value = false
            }, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                Color(0xFF0D99FF)
            )
        ) {
            Text(
                "Change", fontSize = 14.sp
            )
        }
    }, dismissButton = {
        Button(
            onClick = { showDialog.value = false },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                Color(0xFF0D99FF)
            )
        ) {
            Text(
                "Cancel", fontSize = 14.sp
            )
        }
    },
        containerColor = Color(0xFF2C333F),
        tonalElevation = 5.dp,
        shape = RoundedCornerShape(5.dp)
    )
}

@Composable
fun AddVehicleDialog(
    showDialog: MutableState<Boolean>,
    onDismissRequest: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    context: Context,
) {
    val name = remember { mutableStateOf("") }
    val avgFuelConsumption = remember { mutableStateOf("") }
    val carType = remember { mutableStateOf("") }
    val fuelType = remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = { showDialog.value = false }, title = {
        Text(
            "Add vehicle", fontWeight = FontWeight.Bold, color = Color.White
        )
    }, text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextFieldBackground(color = Color.White) {
                OutlinedTextField(
                    modifier = Modifier
                        .height(50.dp)
                        .padding(PaddingValues(0.dp)),
                    value = name.value,
                    onValueChange = { name.value = it; },
                    placeholder = {
                        Text(
                            text = "Name",
                            style = TextStyle(color = Color.Gray),
                            fontSize = 15.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight(500)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF222831),
                        textDecoration = TextDecoration.None,
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextFieldBackground(
                color = Color.White
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .height(50.dp)
                        .padding(PaddingValues(0.dp)),
                    value = avgFuelConsumption.value,
                    onValueChange = { avgFuelConsumption.value = it },
                    placeholder = {
                        Text(
                            text = "Avg fuel cons./100km (eg. 4.5)",
                            style = TextStyle(color = Color.Gray),
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight(500)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF222831),
                        textDecoration = TextDecoration.None,
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, end = 0.dp),
            ) {
                VehicleTypeDropdown(
                    onTypeSelected = { selectedType ->
                        carType.value = selectedType
                    }
                )
                Spacer(modifier = Modifier.width(10.dp))
                VehicleFuelTypeDropdown(
                    onTypeSelected = { selectedType ->
                        fuelType.value = selectedType
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.auto-data.net/en/"))
                    context.startActivity(intent)
                },
                text = "Check your car fuel consumption",
                fontWeight = FontWeight.Normal,
                color = Color(0xFF0D99FF),
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
            )
        }
    }, confirmButton = {
        Button(
            onClick = {
                onSave(name.value, avgFuelConsumption.value, carType.value, fuelType.value)
                showDialog.value = false
            }, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                Color(0xFF0D99FF)
            )
        ) {
            Text(
                "Add", fontSize = 14.sp
            )
        }
    }, dismissButton = {
        Button(
            onClick = { showDialog.value = false },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                Color(0xFF0D99FF)
            )
        ) {
            Text(
                "Cancel", fontSize = 14.sp
            )
        }
    },
        containerColor = Color(0xFF2C333F),
        tonalElevation = 5.dp,
        shape = RoundedCornerShape(5.dp)
    )
}

@Composable
fun VehicleTypeDropdown(onTypeSelected: (String) -> Unit) {
    val options = listOf("Small car", "Big car", "Truck", "Motorbike")
    val optionsMap = mapOf(
        "Small car" to "small_car",
        "Big car" to "big_car",
        "Truck" to "truck",
        "Motorbike" to "motorbike"
    )

    val expanded = remember { mutableStateOf(false) }
    val text = remember { mutableStateOf("Vehicle type") }

    var vehicleTypeParentSize by remember { mutableStateOf(IntSize.Zero) }

    Button(
        onClick = { expanded.value = true },
        shape = RoundedCornerShape(5.dp),
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(end = 0.dp)
            .height(50.dp)
            .onGloballyPositioned { coordinates ->
                vehicleTypeParentSize = coordinates.size
            },
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFF0D99FF)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text.value)
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = "Arrow dropdown"
            )
        }
    }
    DropdownMenu(
        modifier = Modifier.width(with(LocalDensity.current) { vehicleTypeParentSize.width.toDp() }),
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }) {
        options.forEach { option ->
            DropdownMenuItem(modifier = Modifier, text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    option.let {
                        Text(it)
                    }
                }
            }, onClick = {
                optionsMap[option]?.let { onTypeSelected(it) }
                text.value = option
                expanded.value = false
            })
        }
    }
}

@Composable
fun VehicleFuelTypeDropdown(onTypeSelected: (String) -> Unit) {
    val options = listOf("Diesel", "Petrol", "LPG")
    val optionsMap = mapOf(
        "Diesel" to "diesel",
        "Petrol" to "petrol",
        "LPG" to "lpg",
    )

    val expanded = remember { mutableStateOf(false) }
    val text = remember { mutableStateOf("Fuel type") }

    var fuelTypeParentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Button(
            onClick = { expanded.value = true },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp)
                .height(50.dp)
                .onGloballyPositioned { coordinates ->
                    fuelTypeParentSize = coordinates.size
                },
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                Color(0xFF0D99FF)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text.value)
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "Arrow dropdown"
                )
            }
        }
        DropdownMenu(
            modifier = Modifier.width(with(LocalDensity.current) { fuelTypeParentSize.width.toDp() }),
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }) {
            options.forEach { option ->
                DropdownMenuItem(modifier = Modifier, text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        option.let {
                            Text(it)
                        }
                    }
                }, onClick = {
                    optionsMap[option]?.let { onTypeSelected(it) }
                    text.value = option
                    expanded.value = false
                })
            }
        }
    }
}
