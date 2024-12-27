package com.TraSka.com.TraSka

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.ScreenFlowHandler

private var error: Boolean = false

@Composable
fun RegisterScreen(navController: NavController, viewModel: LocationViewModel) {

    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.background_welcome_v2),
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )
    Column(
        modifier = Modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            alignment = Alignment.Center,
            painter = painterResource(R.drawable.traska),
            contentDescription = null
        )
        Button(
            modifier = Modifier
                .fillMaxWidth(0.25f)
                .offset(y = (-50).dp),
            onClick = {
                navController.navigate(ScreenFlowHandler.LoginScreen.route)
            },
            contentPadding = PaddingValues(10.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF222831))

        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "go back icon",
                    modifier = Modifier.size(24.dp, 24.dp),
                    tint = Color.White
                )
                Text(
                    "Back",
                    style = TextStyle(
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = Color.White,
                        fontWeight = FontWeight(500)
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Card(
            elevation = CardDefaults.cardElevation(5.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222831)),
            modifier = Modifier
                .padding(25.dp, 0.dp, 25.dp, 0.dp)
                .offset(y = (-50).dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                RegisterSection(navController, viewModel)
            }
        }
    }
}

//region Composables

@Composable
fun RegisterSection(navController: NavController, viewModel: LocationViewModel) {
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var error by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isLengthValid = isLengthValid(password)
    val hasUpperCase = hasUpperCase(password)
    val hasLowerCase = hasLowerCase(password)
    val hasDigit = hasDigit(password)

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.75f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Password restrictions:",
                    style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight(600),
                        fontSize = 16.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• min. 8 characters", style = TextStyle(color = Color.White))
                if (isLengthValid) {
                    Image(
                        modifier = Modifier.size(15.dp, 15.dp),
                        painter = painterResource(R.drawable.success),
                        contentDescription = "success icon password",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• min. 1 uppercase character", style = TextStyle(color = Color.White))
                if (hasUpperCase) {
                    Image(
                        modifier = Modifier.size(15.dp, 15.dp),
                        painter = painterResource(R.drawable.success),
                        contentDescription = "success icon password",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• min. 1 lowercase character", style = TextStyle(color = Color.White))
                if (hasLowerCase) {
                    Image(
                        modifier = Modifier.size(15.dp, 15.dp),
                        painter = painterResource(R.drawable.success),
                        contentDescription = "success icon password",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• min. 1 number character", style = TextStyle(color = Color.White))
                if (hasDigit) {
                    Image(
                        modifier = Modifier.size(15.dp, 15.dp),
                        painter = painterResource(R.drawable.success),
                        contentDescription = "success icon password",
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextFieldBackground(Color.White) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .padding(PaddingValues(0.dp)),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Email,
                        "email icon",
                        modifier = Modifier.size(20.dp, 20.dp),
                        tint = Color(0xFF0D99FF)
                    )
                },
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "E-mail",
                        style = TextStyle(color = Color.Gray),
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight(500)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF222831),
                    textDecoration = TextDecoration.None,
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextFieldBackground(Color.White) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .padding(PaddingValues(0.dp)),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        "password icon",
                        modifier = Modifier.size(20.dp, 20.dp),
                        tint = Color(0xFF0D99FF)
                    )
                },
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "Password",
                        style = TextStyle(color = Color.Gray),
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight(500)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF222831),
                    textDecoration = TextDecoration.None,
                ),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextFieldBackground(Color.White) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .padding(PaddingValues(0.dp)),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        "password icon",
                        modifier = Modifier.size(20.dp, 20.dp),
                        tint = Color(0xFF0D99FF)
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.signUpWithEmailAndPassword(
                            navController,
                            context,
                            email,
                            login,
                            password,
                            rePassword
                        )
                        focusManager.clearFocus()
                    }
                ),
                value = rePassword,
                onValueChange = { rePassword = it },
                placeholder = {
                    Text(
                        text = "Confirm password",
                        style = TextStyle(color = Color.Gray),
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight(500)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF222831),
                    textDecoration = TextDecoration.None,
                ),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.isLoading = true
                login = email.split('@')[0]
                viewModel.signUpWithEmailAndPassword(
                    navController,
                    context,
                    email,
                    login,
                    password,
                    rePassword
                )
                focusManager.clearFocus()
            },
            colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF)),
            shape = RoundedCornerShape(10),
            modifier = Modifier.size(width = 150.dp, height = 40.dp),
        ) {
            if (!viewModel.isLoading) {
                Text(
                    text = "Sign up",
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = Color.White,
                    fontWeight = FontWeight(500)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
fun OutlinedTextFieldBackground(
    color: Color,
    content: @Composable () -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color,
                    shape = RoundedCornerShape(50.dp)
                )
        )
        content()
    }
}

//endregion

// region Functions

fun isLengthValid(password: String) = password.length >= 8
fun hasUpperCase(password: String) = password.any { it.isUpperCase() }
fun hasLowerCase(password: String) = password.any { it.isLowerCase() }
fun hasDigit(password: String) = password.any { it.isDigit() }

//endregion

