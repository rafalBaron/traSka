import android.app.Activity
import android.content.Context
import android.content.res.Resources.Theme
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
import com.TraSka.Route
import com.TraSka.ScreenFlowHandler
import com.TraSka.User
import com.TraSka.UserData
import com.TraSka.auth.GoogleAuthManager
import com.TraSka.myCallback
import com.TraSka.ui.theme.AppFont
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
private var currentUser: User? = null

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginScreen(navController: NavController, viewModel: LocationViewModel) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.background_welcome_v2),
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            alignment = Alignment.Center,
            painter = painterResource(R.drawable.traska),
            contentDescription = null,
        )
        Button(
            modifier = Modifier
                .fillMaxWidth(0.25f)
                .offset(y = (-50).dp),
            onClick = {
                navController.navigate(ScreenFlowHandler.StartScreen.route)
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
                .padding(25.dp, 0.dp, 25.dp, 25.dp)
                .offset(y = (-50).dp)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
                ) {
                LoginSection(navController, viewModel)
                Spacer(modifier = Modifier.height(15.dp))
                GoogleSection(navController, viewModel)
                Spacer(modifier = Modifier.height(15.dp))
                RegisterSection(navController)
            }
        }
    }
}

// region Composables
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginSection(navController: NavController, viewModel: LocationViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextFieldBackground(Color.White) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .padding(PaddingValues(0.dp)),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Person,
                        "person icon",
                        modifier = Modifier.size(22.dp, 22.dp),
                        tint = Color(0xFF0D99FF)
                    )
                },
                value = email,
                onValueChange = { email = it; },
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
                    focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
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
                        modifier = Modifier.size(22.dp, 22.dp),
                        tint = Color(0xFF0D99FF)
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.isLoading = true;
                    viewModel.loginWithEmailAndPassword(navController, context, email, password)
                    focusManager.clearFocus()
                }),
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
                    focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF222831),
                    textDecoration = TextDecoration.None
                ),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.isLoading = true;
                viewModel.loginWithEmailAndPassword(navController, context, email, password)
                focusManager.clearFocus()
            },
            colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF)),
            shape = RoundedCornerShape(10),
            modifier = Modifier.size(width = 150.dp, height = 40.dp),
        ) {
            if (!viewModel.isLoading) {
                Text(
                    text = "Sign in",
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = Color.White,
                    fontWeight = FontWeight(500)
                )
            }else {
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
fun GoogleSection(navController: NavController, viewModel: LocationViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authManager = remember { GoogleAuthManager(context, mAuth) }

    LaunchedEffect(Unit) {
        authManager.initializeGoogleSignIn()
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                viewModel.setLoadingScreen()
                viewModel.signInWithGoogle(credential)

                navController.navigate(ScreenFlowHandler.HomeScreen.route)
            } catch (e: ApiException) {
                Log.e("GoogleSection", "Google sign-in failed", e)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
    ) {
        Or()
        Spacer(modifier = Modifier.height(15.dp))
        Image(
            modifier = Modifier.width(200.dp).clickable(onClick = {
                activity?.let {
                    val signInIntent = authManager.googleSignInClient.signInIntent
                    activityResultLauncher.launch(signInIntent)
                }
            }),
            alignment = Alignment.Center,
            painter = painterResource(R.drawable.android_dark_sq_ctn),
            contentDescription = null,
        )
    }
}

@Composable
fun RegisterSection(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate(ScreenFlowHandler.RegisterScreen.route) },
            colors = ButtonDefaults.buttonColors(Color(0xFF131314)),
            shape = RoundedCornerShape(10),
            modifier = Modifier.size(width = 189.dp, height = 40.dp),
            contentPadding = PaddingValues(start = 9.dp),
            border = BorderStroke((0.5).dp, Color(0xFF8E918F))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    Icons.Filled.Email,
                    "email icon",
                    modifier = Modifier
                        .size(20.dp, 20.dp)
                        .offset(x = 2.dp),
                    tint = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Continue with Email", fontSize = 13.sp, color = Color(0xFFE3E3E3)
                    )
                }
            }
        }
    }
}

@Composable
fun Or() {
    Row(
        modifier = Modifier.width(100.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.Gray)
        )
        Text(
            text = "or",
            style = TextStyle(fontSize = 15.sp, color = Color.Gray),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.Gray)
        )
    }
}

@Composable
fun OutlinedTextFieldBackground(color: Color, content: @Composable () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color, shape = RoundedCornerShape(50.dp)
                )
        )
        content()
    }
}




