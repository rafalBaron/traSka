package com.TraSka

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SavedRoutesScreen(
    navController: NavController,
    viewModel: LocationViewModel
) {
    val context = LocalContext.current
    var savedRoutes = viewModel.getUser()!!.savedRoutes
    val routes = remember { mutableStateOf(savedRoutes) }
    val openAlertDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row() {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(15.dp, 15.dp, 0.dp, 0.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(Color(0xFF248A12))
                        .size(40.dp, 50.dp)
                        .clickable {
                            navController.navigateUp()
                        },
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF272E38))
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            LazyColumn(modifier = Modifier.padding(15.dp)) {

                items(routes.value!!.size) { index ->
                    var route = routes.value!![index]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .height(225.dp)
                            .padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "\"${route.name}\"",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF272E38)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp, 30.dp)
                                )
                                Text(
                                    text = route.point!!.first().address!!,
                                    modifier = Modifier.width(70.dp),
                                    maxLines = 2
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    imageVector = Icons.Filled.Place,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp, 30.dp)
                                )
                                Text(
                                    text = route.point!!.last().address!!,
                                    modifier = Modifier.width(70.dp),
                                    maxLines = 2,
                                )
                            }
                            Text(
                                text = (route.len!! / 1000).toInt().toString() + " km",
                                modifier = Modifier
                                    .weight(2f)
                                    .clip(shape = RoundedCornerShape(5.dp))
                                    .background(Color(0xFF272E38))
                                    .padding(5.dp),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Image(
                                modifier = Modifier
                                    .size(30.dp, 30.dp)
                                    .weight(1f),
                                painter = if (route.travelMode == "driving") painterResource(R.drawable.car) else painterResource(
                                    R.drawable.person
                                ),
                                contentDescription = "driving mode"
                            )
                            Image(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp, 30.dp)
                                    .clickable {
                                        openAlertDialog.value = true
                                    }
                                    .weight(1f)
                            )
                            when {
                                openAlertDialog.value -> {
                                    DeleteRouteDialog(
                                        onDismissRequest = {
                                            openAlertDialog.value = false
                                        },
                                        onConfirmation = {
                                            openAlertDialog.value = false
                                            if (routes.value!!.size == 1) {
                                                viewModel.delRoute(route, context)
                                                routes.value = routes.value?.minus(route)
                                            } else {
                                                viewModel.delRoute(route, context)
                                                routes.value = routes.value?.minus(route)
                                            }
                                        },
                                        dialogTitle = "Delete this route?",
                                        icon = Icons.Default.Delete
                                    )
                                }
                            }
                        }
                        Row() {
                            Button(
                                onClick = { viewModel.sendRequestOpenMaps(context, route) },
                                modifier = Modifier,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF))
                            ) {
                                Text(text = "Open in Google Maps")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

//region Composables

@Composable
fun DeleteRouteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Image(
                imageVector = Icons.Default.Delete, contentDescription = "acc",
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
                    "Delete",
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

//endregion