package com.TraSka

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.TraSka.auth.GoogleAuthManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

sealed class LocationState {
    object NoPermission : LocationState()
    object LocationDisabled : LocationState()
    object LocationLoading : LocationState()
    data class LocationAvailable(val cameraLatLang: LatLng) : LocationState()
    object Error : LocationState()
}

data class AutocompleteResult(
    val address: String,
    val placeId: String,
)

@HiltViewModel
class LocationViewModel @Inject constructor(@ApplicationContext applicationContext: Context) :
    ViewModel() {

    init {
        Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
    }

    //region Values and variables

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val mDatabase: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
    private val googleAuthManager = GoogleAuthManager(applicationContext, firebaseAuth)

    private var currentUser: User? = null
    private val placesClient by lazy { Places.createClient(applicationContext) }
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var geoCoder: Geocoder
    var currentSavedRoutes by mutableStateOf(listOf<Route>())
    var routePoints by mutableStateOf(listOf<Point>())
    var locationState by mutableStateOf<LocationState>(LocationState.NoPermission)
    val locationAutofill = mutableStateListOf<AutocompleteResult>()
    var currentLatLong by mutableStateOf(LatLng(51.9189046, 19.1343786))
    var currentPointId by mutableStateOf(String())
    var currentSavingRouteLen = 0f
    var isLogged by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    private var job: Job? = null

    //endregion

    //region User functions

    fun clearViewModel() {
        clearUser(null)
        routePoints = emptyList()
        currentSavingRouteLen = 0f
    }

    fun addPoint(pointClass: Point) {
        routePoints = routePoints + pointClass
    }

    fun delPoint(pointClass: Point) {
        routePoints = routePoints - pointClass
    }

    fun delRoute(route: Route, context: Context) {
        val mDatabase: FirebaseDatabase =
            FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
        val userRouteRef = route.id?.let {
            mDatabase.getReference("Users").child(currentUser!!.userData!!.uid!!)
                .child("savedRoutes").child(
                    it
                )
        }

        userRouteRef!!.removeValue().addOnSuccessListener {
            currentUser!!.savedRoutes = currentUser!!.savedRoutes?.minus(route)
            currentSavedRoutes = currentSavedRoutes.minus(route)
            Toast.makeText(context, "Route deleted successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Error while deleting route!", Toast.LENGTH_SHORT).show()
        }
    }

    fun setUser(user: User?) {
        currentUser = user
        isLogged = user != null
        currentSavedRoutes = user!!.savedRoutes!!
    }

    fun clearUser(user: User?) {
        currentUser = user
        isLogged = user != null
        currentSavedRoutes = emptyList()
    }

    fun getUser(): User? {
        return currentUser
    }

    fun readUserData(callback: FirebaseCallback, uid: String) {
        val dbRef = databaseRef
        dbRef.child("Users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.child("userData").getValue(UserData::class.java)
                var routesList = listOf<Route>()
                for (route in dataSnapshot.child("savedRoutes").children) {
                    routesList = routesList + (route.getValue(Route::class.java))!!
                }

                val user = User(userData, routesList)

                callback.onResponse(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("ERROR", "Error while reading data from db")
            }
        })
    }

    fun loginWithEmailAndPassword(
        navController: NavController,
        context: Context,
        email: String,
        password: String
    ): Boolean {
        if (email.isNotBlank() && password.isNotBlank()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                val uid = it.user?.uid
                if (uid != null) {
                    readUserData(object : myCallback() {
                        override fun onResponse(user: User?) {
                            currentUser = user
                            currentUser?.let { it1 -> setUser(it1) }
                            navController.navigate(ScreenFlowHandler.HomeScreen.route)
                        }
                    }, uid)
                }
                isLoading = false;
            }.addOnFailureListener { _ ->
                isLoading = false;
                Toast.makeText(
                    context,
                    "Invalid e-mail or password",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            return true
        } else {
            isLoading = false;
            Toast.makeText(context, "E-mail and password can't be empty!", Toast.LENGTH_SHORT)
                .show()
            return false
        }
    }

    fun signUpWithEmailAndPassword(
        navController: NavController,
        context: Context,
        email: String,
        login: String,
        password: String,
        rePassword: String
    ): Boolean {
        if ((email.isNotBlank() && password.isNotBlank() && login.isNotBlank() && rePassword.isNotBlank()) && (password == rePassword)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = firebaseAuth.currentUser
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
                                    currentUser?.let { it1 ->
                                        setUser(
                                            it1
                                        )
                                    }
                                }
                            }, userId)
                            isLoading = false;
                        }
                        .addOnFailureListener() {
                            isLoading = false;
                            navController.navigate(ScreenFlowHandler.RegisterErrorScreen.route)
                        }
                }
                .addOnFailureListener { _ ->
                    isLoading = false;
                    navController.navigate(ScreenFlowHandler.RegisterErrorScreen.route)
                }
            return false
        } else {
            isLoading = false;
            Toast.makeText(
                context,
                "Fill all fields / password and re-password must be the same!!",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        googleAuthManager.signInWithGoogleCredential(credential, { user ->
            setUser(user)
            isLogged = true
        }, { error ->
            // error
        })
    }

    //endregion

    //region Google Apis stuff

    fun searchPlaces(query: String) {
        job?.cancel()
        locationAutofill.clear()
        job = viewModelScope.launch {
            val request = FindAutocompletePredictionsRequest.builder().setQuery(query).build()
            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                locationAutofill += response.autocompletePredictions.map {
                    AutocompleteResult(
                        it.getFullText(null).toString(), it.placeId
                    )
                }
            }.addOnFailureListener {
                it.printStackTrace()
                println(it.cause)
                println(it.message)
            }
        }
    }

    fun getCoordinates(result: AutocompleteResult) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)
        placesClient.fetchPlace(request).addOnSuccessListener {
            if (it != null) {
                currentLatLong = it.place.latLng!!
                currentPointId = result.placeId
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        locationState = LocationState.LocationLoading
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                locationState =
                    if (location == null && locationState !is LocationState.LocationAvailable) {
                        LocationState.Error
                    } else {
                        currentLatLong = LatLng(location.latitude, location.longitude)
                        LocationState.LocationAvailable(
                            LatLng(
                                location.latitude, location.longitude
                            )
                        )
                    }
            }
    }

    var text by mutableStateOf("")

    fun getAddress(latLng: LatLng) {
        viewModelScope.launch {
            val address = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            text = address?.get(0)?.getAddressLine(0).toString()
        }
    }

    fun sendRequestOpenMaps(context: Context, travelMode: String, avoid: String) {
        val origin = routePoints.first().address
        val destination = routePoints.last().address
        val waypointsList = routePoints.subList(1, routePoints.size - 1)
        val encodedOrigin = origin?.replace(" ", "%20")
        val encodedDestination = destination?.replace(" ", "%20")
        val encodedWaypoints =
            waypointsList.joinToString("|") { it.address?.replace(" ", "%20").toString() }

        val urlRequest =
            "https://maps.googleapis.com/maps/api/directions/json?avoid=$avoid&origin=$encodedOrigin&destination=$encodedDestination&waypoints=optimize:true|$encodedWaypoints&travelmode=$travelMode&key=" + BuildConfig.DIRECTIONS_API_KEY

        Log.println(Log.INFO, "ZAPYTANIE1", urlRequest)

        var waypoints: MutableList<String> = mutableListOf<String>()
        val directionsRequest = object :
            StringRequest(Request.Method.GET, urlRequest, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                Log.println(Log.INFO, "JSON JSON JSON", response.toString())
                val routes = jsonResponse.getJSONArray("routes")
                val waypointsOrder = routes.getJSONObject(0).getJSONArray("waypoint_order")

                for (i in 0 until waypointsOrder.length()) {
                    val index = waypointsOrder.getInt(i)
                    waypointsList[index].address?.let { waypoints.add(it) }
                }
                var separatedWaypoints = waypoints.joinToString("|")

                val url =
                    "https://www.google.com/maps/dir/?api=1&avoid=$avoid&origin=$origin&destination=$destination&waypoints=$separatedWaypoints&travelmode=$travelMode"

                Log.println(Log.INFO, "ZAPYTANIE2", url)

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ContextCompat.startActivity(context, intent, null)
            }, Response.ErrorListener { _ ->
            }) {}

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }

    fun sendRequestOpenMaps(context: Context, route: Route) {
        val origin = route.point!!.first().address
        val destination = route.point.last().address

        val addresses = mutableListOf<String>()
        for (point in route.point.subList(1, route.point.size - 1)) {
            val address = point.address
            addresses.add(address!!)
        }

        val waypointsList = addresses
        val encodedOrigin = origin?.replace(" ", "%20")
        val encodedDestination = destination?.replace(" ", "%20")
        val encodedWaypoints = waypointsList.joinToString("|")

        val urlRequest =
            "https://maps.googleapis.com/maps/api/directions/json?origin=$encodedOrigin&destination=$encodedDestination&waypoints=optimize:true|$encodedWaypoints&travelmode=${route.travelMode}&key=" + BuildConfig.DIRECTIONS_API_KEY

        Log.println(Log.INFO, "ZAPYTANIE1", urlRequest)

        var waypoints: MutableList<String> = mutableListOf<String>()
        val directionsRequest = object :
            StringRequest(Request.Method.GET, urlRequest, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.getJSONArray("routes")
                val waypointsOrder = routes.getJSONObject(0).getJSONArray("waypoint_order")

                for (i in 0 until waypointsOrder.length()) {
                    val index = waypointsOrder.getInt(i)
                    waypointsList[index].let { waypoints.add(it) }
                }
                var separatedWaypoints = waypoints.joinToString("|")

                val url =
                    "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&waypoints=$separatedWaypoints&travelmode=${route.travelMode}"

                Log.println(Log.INFO, "ZAPYTANIE2", url)

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ContextCompat.startActivity(context, intent, null)
            }, Response.ErrorListener { _ ->
            }) {}

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }

    fun saveRoute(uid: String?, route: Route, context: Context) {
        val mDatabase: FirebaseDatabase =
            FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
        val userRoutesRef = mDatabase.getReference("Users").child(uid!!).child("savedRoutes")
        var newRouteKey = userRoutesRef.push().key ?: return
        route.id = newRouteKey
        userRoutesRef.child(newRouteKey).setValue(route).addOnSuccessListener {
            Log.d("Firebase", "Trasa dodana do listy")
            currentUser!!.savedRoutes = currentUser!!.savedRoutes?.plus(route)
            currentSavedRoutes = currentSavedRoutes.plus(route)
            Log.d("TRASKA", "Trasa dodana do listy lokalnie")
            Toast.makeText(context, "Route saved successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error dodawania trasy: $exception")
            Toast.makeText(context, "Erroe while saving route!", Toast.LENGTH_SHORT).show()
        }

    }

    fun sendRequestSaveRoute(context: Context, travelMode: String, name: String) {
        val origin = routePoints.first().address
        val destination = routePoints.last().address
        val waypointsList = routePoints.subList(1, routePoints.size - 1)
        val encodedOrigin = origin?.replace(" ", "%20")
        val encodedDestination = destination?.replace(" ", "%20")
        val encodedWaypoints =
            waypointsList.joinToString("|") { it.address?.replace(" ", "%20").toString() }

        val urlRequest =
            "https://maps.googleapis.com/maps/api/directions/json?origin=$encodedOrigin&destination=$encodedDestination&waypoints=optimize:true|$encodedWaypoints&mode=$travelMode&key=" + BuildConfig.DIRECTIONS_API_KEY

        Log.println(Log.INFO, "ZAPYTANIE", urlRequest)

        var waypoints: MutableList<String> = mutableListOf<String>()
        val directionsRequest = object :
            StringRequest(Request.Method.GET, urlRequest, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.getJSONArray("routes")
                val waypointsOrder = routes.getJSONObject(0).getJSONArray("waypoint_order")

                for (i in 0 until waypointsOrder.length()) {
                    val index = waypointsOrder.getInt(i)
                    waypointsList[index].address?.let { waypoints.add(it) }
                }
                var separatedWaypoints = waypoints.joinToString("|")

                val url =
                    "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&waypoints=$separatedWaypoints&travelmode=$travelMode"

                for (i in 0 until routes.length()) {
                    val route = routes.getJSONObject(i)
                    val legs = route.getJSONArray("legs")

                    var totalDistance = 0

                    for (j in 0 until legs.length()) {
                        val leg = legs.getJSONObject(j)
                        val distanceObject = leg.getJSONObject("distance")
                        val distanceValue = distanceObject.getInt("value")

                        totalDistance += distanceValue
                    }
                    currentSavingRouteLen = totalDistance.toFloat()
                    Log.println(
                        Log.INFO,
                        "Trasa",
                        "Łączna odległość: $totalDistance metrów. Zapisana currentDlugosc: $currentSavingRouteLen"
                    )
                    val savingRoute = Route(
                        name, travelMode, currentSavingRouteLen, routePoints, shareUrl = url
                    )
                    saveRoute(getUser()?.userData?.uid, savingRoute, context)
                }

            }, Response.ErrorListener { _ ->
            }) {}

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)
    }

    //endregion

}