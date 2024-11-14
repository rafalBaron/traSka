package com.TraSka.com.TraSka

import android.content.Context
import com.TraSka.User
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.TraSka.FirebaseCallback
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.ScreenFlowHandler
import com.TraSka.UserData
import com.TraSka.myCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
private val mDatabase: FirebaseDatabase =
    FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
private var currentUser: User? = null
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
            colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF)),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(0.6f)
                .offset(y = (-20).dp),
            contentPadding = PaddingValues(bottom = 40.dp, top = 10.dp),
            onClick = {}) {
            Text(
                text = "Create Account",
                style = TextStyle(color = Color.White),
                fontSize = 24.sp,
            )
        }
        Card(
            elevation = CardDefaults.cardElevation(5.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222831)),
            modifier = Modifier
                .padding(25.dp, 0.dp, 25.dp, 25.dp)
                .offset(y = (-55).dp)
        ) {
            Spacer(modifier = Modifier.height(25.dp))
            RegisterSection(navController, viewModel)
            Spacer(modifier = Modifier.height(25.dp))
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

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextFieldBackground(Color.White) {
            OutlinedTextField(
                modifier = Modifier.height(50.dp)
                    .fillMaxWidth(0.65f),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Person,
                        "person icon username",
                        modifier = Modifier.size(22.dp, 22.dp),
                        tint = Color(0xFF0D99FF)
                    )
                },
                value = login,
                onValueChange = { login = it },
                placeholder = {
                    Text(
                        text = "Username",
                        style = TextStyle(color = Color.Gray),
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight(500)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = if (!error) Color.Transparent else Color.Red,
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
                modifier = Modifier.height(50.dp)
                    .fillMaxWidth(0.65f),
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
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = if (!error) Color.Transparent else Color.Red,
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
                modifier = Modifier.height(50.dp)
                    .fillMaxWidth(0.65f),
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
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = if (!error) Color.Transparent else Color.Red,
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
                modifier = Modifier.height(50.dp)
                    .fillMaxWidth(0.65f),
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
                        signUp(
                            navController,
                            viewModel,
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
                    focusedBorderColor = Color(0xFF0D99FF),
                    unfocusedBorderColor = if (!error) Color.Transparent else Color.Red,
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
                focusManager.clearFocus()
                error =
                    signUp(navController, viewModel, context, email, login, password, rePassword)
            },
            colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF)),
            shape = RoundedCornerShape(10),
            modifier = Modifier.size(width = 150.dp, height = 40.dp),
        ) {
            Text(
                text = "Sign up",
                color = Color.White,
            )
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
                    shape = RoundedCornerShape(4.dp)
                )
        )
        content()
    }
}

//endregion

// region Functions

fun signUp(
    navController: NavController,
    viewModel: LocationViewModel,
    context: Context,
    email: String,
    login: String,
    password: String,
    rePassword: String
): Boolean {
    if ((email.isNotBlank() && password.isNotBlank() && login.isNotBlank() && rePassword.isNotBlank()) && (password == rePassword)) {
        error = false
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = mAuth.currentUser
                val userId = user?.uid ?: return@addOnSuccessListener
                val userData = UserData(login, email, userId)
                val userModel =
                    User(userData)

                mDatabase.getReference("Users").child(userId).setValue(userModel)
                    .addOnSuccessListener() {
                        navController.navigate(ScreenFlowHandler.RegisterSuccessfulScreen.route)
                        readUserData(object : myCallback() {
                            override fun onResponse(user: User?) {
                                currentUser = user
                                currentUser?.let { it1 -> viewModel.setUser(it1) }
                            }
                        }, userId)
                    }
                    .addOnFailureListener() {
                        navController.navigate(ScreenFlowHandler.RegisterErrorScreen.route)
                    }
            }
            .addOnFailureListener { _ ->
                navController.navigate(ScreenFlowHandler.RegisterErrorScreen.route)
            }
        return false
    } else {
        Toast.makeText(
            context,
            "Fill all fields / password and re-password must be the same!!",
            Toast.LENGTH_SHORT
        ).show()
        return true
    }
}

fun readUserData(callback: FirebaseCallback, uid: String) {
    val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbRef = mDatabase.reference
    dbRef.child("Users").child(uid)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    Log.d("TAG", user.userData!!.email.toString())
                    Log.d("TAG", user.userData!!.login.toString())
                }
                callback.onResponse(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("ERROR", "Error while reading data from db")
            }
        })
}

//endregion

