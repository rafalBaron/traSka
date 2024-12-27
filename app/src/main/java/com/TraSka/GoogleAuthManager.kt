package com.TraSka.auth

import android.content.Context
import android.content.Intent
import com.TraSka.R
import com.TraSka.Route
import com.TraSka.User
import com.TraSka.UserData
import com.TraSka.Vehicle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GoogleAuthManager(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    lateinit var googleSignInClient: GoogleSignInClient
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
    private val userRef: DatabaseReference = database.reference.child("Users")

    companion object {
        const val RC_SIGN_IN = 9001
    }

    fun signInWithGoogleCredential(
        credential: AuthCredential,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    checkAndAddUserToDatabase(firebaseUser, onSuccess, onFailure)
                } else {
                    onFailure("Nie udało się pobrać użytkownika.")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Błąd logowania.")
            }
    }

    fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun handleSignInResult(requestCode: Int, data: Intent?, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account, onSuccess, onFailure)
            } catch (e: ApiException) {
                onFailure("Google sign-in failed: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    firebaseUser?.let {
                        checkAndAddUserToDatabase(it, onSuccess, onFailure)
                    }
                } else {
                    task.exception?.let { onFailure(it.message ?: "Błąd logowania.") }
                }
            }
    }

    private fun checkAndAddUserToDatabase(firebaseUser: FirebaseUser, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        val userId = firebaseUser.uid
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userData = snapshot.child("userData").getValue(UserData::class.java)
                    var routesList = listOf<Route>()
                    for (route in snapshot.child("savedRoutes").children) {
                        routesList = routesList + (route.getValue(Route::class.java))!!
                    }
                    var vehiclesList = listOf<Vehicle>()
                    for (vehicle in snapshot.child("savedVehicles").children) {
                        vehiclesList = vehiclesList + (vehicle.getValue(Vehicle::class.java))!!
                    }
                    val user = User(userData, routesList, vehiclesList)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure("Nie udało się pobrać danych użytkownika")
                    }
                } else {
                    val newUserData = UserData(
                        uid = firebaseUser.uid,
                        login = firebaseUser.email?.split('@')?.get(0),
                        email = firebaseUser.email
                    )
                    val newUser = User(userData = newUserData)
                    userRef.child(userId).setValue(newUser).addOnCompleteListener {
                        if (it.isSuccessful) {
                            onSuccess(newUser)
                        } else {
                            onFailure("Błąd zapisu użytkownika")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure("Błąd połączenia z bazą danych: ${error.message}")
            }
        })
    }
}
