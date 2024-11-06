import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.TraSka.CardContent
import com.TraSka.FirebaseCallback
import com.TraSka.LocationViewModel
import com.TraSka.R
import com.TraSka.Route
import com.TraSka.Screen
import com.TraSka.User
import com.TraSka.UserData
import com.TraSka.myCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
private val aDatabase: FirebaseDatabase =
    FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
private var mDatabase: DatabaseReference = aDatabase.reference
private var db: DatabaseReference = mDatabase.child("Users")
private var currentUser: User? = null
private var error: Boolean = false

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WelcomeScreen(navController: NavController, viewModel: LocationViewModel) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.background_welcome),
        contentDescription = null,
        contentScale = ContentScale.FillWidth
    )
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(state = scrollState)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(250.dp))
        Card(
            elevation = CardDefaults.cardElevation(5.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222831)),
            modifier = Modifier
                .padding(25.dp,0.dp,25.dp,25.dp),

        ) {
            Spacer(modifier = Modifier.height(25.dp))
            LoginSection(navController, viewModel)
            Spacer(modifier = Modifier.height(25.dp))
            RegisterSection(navController)
            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSection(navController: NavController, viewModel: LocationViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextFieldBackground(Color(0xFF0D99FF)) {
            OutlinedTextField(modifier = Modifier.size(240.dp, 50.dp), leadingIcon = {
                Icon(
                    Icons.Filled.Person,
                    "person icon",
                    modifier = Modifier.size(20.dp, 20.dp)
                )
            }, value = email, onValueChange = { email = it; }, placeholder = {
                Text(
                    text = "E-mail",
                    style = TextStyle(color = Color(0xFF222831)),
                    fontSize = 17.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight(600)
                )
            }, colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = if (!error) Color.Transparent else Color.Red
            ), singleLine = true, textStyle = TextStyle(
                fontSize = 17.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF222831),
                textDecoration = TextDecoration.None
            )
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextFieldBackground(Color(0xFF0D99FF)) {
            OutlinedTextField(modifier = Modifier.size(240.dp, 50.dp), leadingIcon = {
                Icon(
                    Icons.Filled.Lock,
                    "password icon",
                    modifier = Modifier.size(20.dp, 20.dp)
                )
            }, value = password, onValueChange = { password = it }, placeholder = {
                Text(
                    text = "Password",
                    style = TextStyle(color = Color(0xFF222831)),
                    fontSize = 17.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight(600)
                )
            }, colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = if (!error) Color.Transparent else Color.Red,
            ), singleLine = true, textStyle = TextStyle(
                fontSize = 17.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF222831),
                textDecoration = TextDecoration.None
            ), visualTransformation = PasswordVisualTransformation()
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            error = false
                            val uid = it.user?.uid
                            if (uid != null) {
                                readUserData(object : myCallback() {
                                    override fun onResponse(user: User?) {
                                        currentUser = user
                                        currentUser?.let { it1 -> viewModel.setUser(it1) }
                                        navController.navigate(Screen.HomeScreenLogged.route)
                                    }
                                }, uid)
                            }
                        }
                        .addOnFailureListener { exception ->
                            error = true
                            Toast.makeText(
                                context,
                                exception.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    error = true
                    Toast.makeText(context, "Fill login and password!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = RoundedCornerShape(10),
            modifier = Modifier.size(width = 150.dp, height = 50.dp),
        ) {
            Text(
                text = "Login",
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                color = Color(0xFF222831),
                fontWeight = FontWeight(600)
            )
        }
    }
}

fun readUserData(callback: FirebaseCallback, uid: String) {
    val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbRef = mDatabase.reference
    dbRef.child("Users").child(uid)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.child("userData").getValue(UserData::class.java)
                var routesList = listOf<Route>()
                for (route in dataSnapshot.child("savedRoutes").children) {
                    routesList = routesList + (route.getValue(Route::class.java))!!
                }

                val user = User(userData,routesList)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSection(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Do not have an account?", fontSize = 18.sp, color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate(Screen.RegisterScreen.route) },
            colors = ButtonDefaults.buttonColors(Color.Black),
            shape = RoundedCornerShape(10),
            modifier = Modifier.size(width = 220.dp, height = 60.dp),
        ) {
            Text(
                text = "Create Account",
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                color = Color.White,
                fontWeight = FontWeight(600)
            )
        }
    }
}

@Composable
fun OutlinedTextFieldBackground(color: Color, content: @Composable () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color, shape = RoundedCornerShape(4.dp)
                )
        )
        content()
    }
}


