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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.TraSka.auth.GoogleAuthManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    lateinit var geoCoder: Geocoder

    init {
        Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        geoCoder = Geocoder(applicationContext)
    }

    //region Values and variables

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val mDatabase: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
    private val googleAuthManager = GoogleAuthManager(applicationContext, firebaseAuth)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val placesClient by lazy { Places.createClient(applicationContext) }

    var currentSavedRoutes by mutableStateOf(listOf<Route>())
    var currentSavedVehicles by mutableStateOf(listOf<Vehicle>())
    var routePoints by mutableStateOf(listOf<Point>())
    var locationState by mutableStateOf<LocationState>(LocationState.NoPermission)
    val locationAutofill = mutableStateListOf<AutocompleteResult>()
    var currentLatLong by mutableStateOf<LatLng?>(null)
    var userLatLong by mutableStateOf<LatLng?>(null)
    var userLocationText by mutableStateOf("")
    var userPlaceId by mutableStateOf("")
    var selectedCar = mutableStateOf("")
        private set
    var selectedOption = mutableStateOf("driving")
        private set
    var currentPointId by mutableStateOf(String())
    var currentSavingRouteLen = 0f
    var currentNotOptimizedRoute by mutableStateOf<Route?>(null)
    var currentOptimizedRoute by mutableStateOf<Route?>(null)
    var isLogged by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    private val _isLoggingLoading = MutableLiveData(true)
    val isLoggingLoading: LiveData<Boolean> = _isLoggingLoading
    private var job: Job? = null

    val emissionsMap = mapOf(
        "diesel" to 2.64f, // w kilogramach CO₂ na litr
        "petrol" to 2.392f,
        "lpg" to 1.65f
    )

    //endregion

    //region User functions

    fun clearViewModel() {
        firebaseAuth.signOut()
        clearUser(null)
        routePoints = emptyList()
        currentLatLong = null
        currentSavingRouteLen = 0f
        currentPointId = ""
        isLoading = false
    }

    fun setLoadingScreen() {
        _isLoggingLoading.value = true
    }

    fun setLoadingScreenFalse() {
        _isLoggingLoading.value = false
    }

    fun updateSelectedCar(newVehicle: String) {
        selectedCar.value = newVehicle
    }

    fun updateSelectedOption(newOption: String) {
        selectedOption.value = newOption
    }

    fun addPoint(pointClass: Point) {
        var id = ""
        while (!(routePoints.any { it.lazyColumnId == pointClass.lazyColumnId })) {
            id = generatePointId()
            pointClass.lazyColumnId = id
            routePoints = routePoints + pointClass
        }
    }

    fun delPoint(pointClass: Point) {
        routePoints = routePoints - pointClass
        userLatLong = userLatLong
        if (routePoints.isEmpty()) {
            currentLatLong = userLatLong
        }
    }

    fun delRoute(route: Route, context: Context) {
        val mDatabase: FirebaseDatabase =
            FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
        val userRouteRef = route.id?.let {
            mDatabase.getReference("Users").child(currentUser.value!!.userData!!.uid!!)
                .child("savedRoutes").child(
                    it
                )
        }

        userRouteRef!!.removeValue().addOnSuccessListener {
            currentUser.value!!.savedRoutes = currentUser.value!!.savedRoutes?.minus(route)
            currentSavedRoutes = currentSavedRoutes - route
            Toast.makeText(context, "Route deleted successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Error while deleting route!", Toast.LENGTH_SHORT).show()
        }
    }

    fun setUser(user: User?) {
        _currentUser.value = user
        isLogged = user != null
        currentSavedRoutes = user!!.savedRoutes!!
        currentSavedVehicles = user.savedVehicles!!
    }

    fun clearUser(user: User?) {
        _currentUser.value = user
        isLogged = user != null
        currentSavedRoutes = emptyList()
        currentSavedVehicles = emptyList()
        selectedCar.value = ""
        selectedOption.value = "driving"
    }

    fun getUser(): User? {
        return currentUser.value
    }

    fun saveVehicle(uid: String?, vehicle: Vehicle, context: Context) {
        val mDatabase: FirebaseDatabase =
            FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
        val userVehiclesRef = mDatabase.getReference("Users").child(uid!!).child("savedVehicles")
        var newVehicleKey = userVehiclesRef.push().key ?: return
        vehicle.id = newVehicleKey
        userVehiclesRef.child(newVehicleKey).setValue(vehicle).addOnSuccessListener {
            Log.d("Firebase", "Pojazd dodany do listy")
            currentUser.value!!.savedVehicles = currentUser.value!!.savedVehicles!!.plus(vehicle)
            currentSavedVehicles = currentSavedVehicles + vehicle
            Log.d("TRASKA", "Pojazd dodany do listy")
            Toast.makeText(context, "Vehicle saved successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error dodawania pojazdu: $exception")
            Toast.makeText(context, "Error while saving vehicle!", Toast.LENGTH_SHORT).show()
        }

    }

    fun delVehicle(vehicle: Vehicle, context: Context) {
        val mDatabase: FirebaseDatabase =
            FirebaseDatabase.getInstance("https://traska-f9851-default-rtdb.europe-west1.firebasedatabase.app/")
        val userVehicleRef = vehicle.id?.let {
            mDatabase.getReference("Users").child(currentUser.value!!.userData!!.uid!!)
                .child("savedVehicles").child(
                    it
                )
        }

        userVehicleRef!!.removeValue().addOnSuccessListener {
            currentUser.value!!.savedVehicles = currentUser.value!!.savedVehicles!!.minus(vehicle)
            currentSavedVehicles = currentSavedVehicles - vehicle
            Toast.makeText(context, "Vehicle deleted successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Error while deleting Vehicle!", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkSetCurrentLoggedUser() {
        _isLoggingLoading.value = true

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            readUserData(object : myCallback() {
                override fun onResponse(user: User?) {
                    setUser(user)
                    isLogged = user != null
                    _isLoggingLoading.value = false
                }
            }, uid)
        } else {
            isLogged = false
            _isLoggingLoading.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocationId() {
        userLatLong?.let { latLng ->
            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)
            val request = FindCurrentPlaceRequest.builder(placeFields).build()

            placesClient.findCurrentPlace(request)
                .addOnSuccessListener { response ->
                    val currentPlace = response.placeLikelihoods.firstOrNull()?.place
                    if (currentPlace != null) {
                        userLocationText = currentPlace.address ?: "Unknown Location"
                        userPlaceId = currentPlace.id ?: "Unknown Place ID"
                    }
                    Log.i("Location", userLocationText)
                    Log.i("ID", userPlaceId)
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    println(it.cause)
                    println(it.message)
                    Log.i("Location", "Failed to fetch user location > " + it.message + it.cause)
                }
        }
    }

    fun updateUsernameInFirebase(userId: String, newUsername: String, context: Context, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("userData")

        val updates = mapOf<String, Any>(
            "login" to newUsername
        )

        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                onSuccess()
                val updatedUser = _currentUser.value?.copy(userData = _currentUser.value?.userData?.copy(login = newUsername))
                _currentUser.value = updatedUser
                Toast.makeText(context, "Username change successful!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
                Toast.makeText(context, "There was an error updating the username.", Toast.LENGTH_SHORT).show()
            }
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
                var vehiclesList = listOf<Vehicle>()
                for (vehicle in dataSnapshot.child("savedVehicles").children) {
                    vehiclesList = vehiclesList + (vehicle.getValue(Vehicle::class.java))!!
                }

                val user = User(userData, routesList, vehiclesList)

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
                            _currentUser.value = user
                            setUser(currentUser.value)
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
                            //navController.navigate(ScreenFlowHandler.RegisterSuccessfulScreen.route)
                            readUserData(object : myCallback() {
                                override fun onResponse(user: User?) {
                                    _currentUser.value = user
                                    setUser(
                                        user
                                    )
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
            _isLoggingLoading.value = false
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

    fun generatePointId(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    var text by mutableStateOf("")

    fun getAddress(latLng: LatLng) : String {
        var address = ""
        viewModelScope.launch {
            address =
                currentLatLong?.let { geoCoder.getFromLocation(it.latitude, it.longitude, 1)?.get(0)
                    ?.getAddressLine(0) }
                    .toString()
        }
        return address
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
                if (routes.length() == 0) {
                    Toast.makeText(context, "Error while getting directions. Impossible to reach (seas).", Toast.LENGTH_SHORT).show()
                    return@Listener
                }
                val waypointsOrder = routes.getJSONObject(0).getJSONArray("waypoint_order")

                for (i in 0 until waypointsOrder.length()) {
                    val index = waypointsOrder.getInt(i)
                    waypointsList[index].address?.let { waypoints.add(it) }
                }
                var separatedWaypoints = waypoints.joinToString("|")

                openGoogleMaps(origin!!, separatedWaypoints, destination!!, avoid, travelMode, context)
            }, Response.ErrorListener { _ ->
                Toast.makeText(context, "Error while getting directions.", Toast.LENGTH_SHORT).show()
            }) {}

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }

    fun sendRequestNotOptimized(context: Context, travelMode: String, avoid: String, vehicle: Vehicle? = null, callback: (Route?) -> Unit) {
        val origin = routePoints.first().address
        val destination = routePoints.last().address
        val waypointsList = routePoints.subList(1, routePoints.size - 1)
        val encodedOrigin = origin?.replace(" ", "%20")
        val encodedDestination = destination?.replace(" ", "%20")
        val encodedWaypoints =
            waypointsList.joinToString("|") { it.address?.replace(" ", "%20").toString() }

        val urlRequest =
            "https://maps.googleapis.com/maps/api/directions/json?avoid=$avoid&origin=$encodedOrigin&destination=$encodedDestination&waypoints=optimize:false|$encodedWaypoints&mode=$travelMode&key=" + BuildConfig.DIRECTIONS_API_KEY

        Log.i("URL not optimized", urlRequest)

        val directionsRequest = object :
            StringRequest(Request.Method.GET, urlRequest, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() == 0) {
                    Toast.makeText(context, "Error while getting directions. Impossible to reach (seas).", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
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
                }
                if (vehicle != null) {
                    val burnedFuel =
                        currentSavingRouteLen / 1000 * (vehicle.avgFuelConsumption!! / 100)
                    val co2 = burnedFuel * emissionsMap[vehicle.fuelType]!!
                    currentNotOptimizedRoute = Route(
                        travelMode = travelMode,
                        len = currentSavingRouteLen,
                        point = routePoints,
                        vehicle = vehicle,
                        co2 = co2,
                        avoid = avoid
                    )
                } else {
                    currentNotOptimizedRoute = Route(
                        travelMode = travelMode,
                        len = currentSavingRouteLen,
                        point = routePoints,
                        vehicle = null,
                        co2 = null,
                        avoid = avoid
                    )
                }
                callback(currentNotOptimizedRoute)
            }, Response.ErrorListener { _ ->
                callback(null)
            }) {}

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }

    fun sendRequestOptimized(context: Context, travelMode: String, avoid: String, vehicle: Vehicle? = null, callback: (Route?) -> Unit) {
        val origin = routePoints.first().address
        val destination = routePoints.last().address
        val waypointsList = routePoints.subList(1, routePoints.size - 1)
        val encodedOrigin = origin?.replace(" ", "%20")
        val encodedDestination = destination?.replace(" ", "%20")
        val encodedWaypoints =
            waypointsList.joinToString("|") { it.address?.replace(" ", "%20").toString() }

        val urlRequest =
            "https://maps.googleapis.com/maps/api/directions/json?avoid=$avoid&origin=$encodedOrigin&destination=$encodedDestination&waypoints=optimize:true|$encodedWaypoints&mode=$travelMode&key=" + BuildConfig.DIRECTIONS_API_KEY

        Log.i("URL optimized", urlRequest)

        var waypoints: MutableList<Point> = mutableListOf<Point>()
        val directionsRequest = object :
            StringRequest(Request.Method.GET, urlRequest, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() == 0) {
                    Toast.makeText(context, "Error while getting directions. Impossible to reach (seas).", Toast.LENGTH_SHORT).show()
                    return@Listener
                }
                val waypointsOrder = routes.getJSONObject(0).getJSONArray("waypoint_order")

                for (i in 0 until waypointsOrder.length()) {
                    val index = waypointsOrder.getInt(i)
                    waypointsList[index].let { waypoints.add(it) }
                }
                waypoints.add(0, routePoints.first())
                waypoints.add(waypoints.size, routePoints.last())
                //var separatedWaypoints = waypoints.joinToString("|")

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

                }
                if (vehicle != null) {
                    val burnedFuel =
                        currentSavingRouteLen / 1000 * (vehicle.avgFuelConsumption!! / 100)
                    val co2 = burnedFuel * emissionsMap[vehicle.fuelType]!!

                    currentOptimizedRoute = Route(
                        travelMode = travelMode,
                        len = currentSavingRouteLen,
                        point = waypoints,
                        vehicle = vehicle,
                        co2 = co2,
                        avoid = avoid
                    )
                } else {
                    currentOptimizedRoute = Route(
                        travelMode = travelMode,
                        len = currentSavingRouteLen,
                        point = waypoints,
                        vehicle = null,
                        co2 = null,
                        avoid = avoid
                    )
                }
                callback(currentOptimizedRoute)
            }, Response.ErrorListener { _ ->
                callback(null)
            }) {}

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)
    }

    fun openGoogleMaps(origin: String, separatedWaypoints: String, destination: String, avoid: String, travelMode: String, context: Context)  {
        val url =
            "https://www.google.com/maps/dir/?api=1&avoid=$avoid&origin=$origin&destination=$destination&waypoints=$separatedWaypoints&travelmode=$travelMode"

        Log.println(Log.INFO, "ZAPYTANIE2", url)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, intent, null)
    }

    fun openGoogleMaps(route: Route, context: Context)  {
        val origin = route.point!!.first().address
        val destination = route.point.last().address
        val avoid = if (route.avoid == null) "" else route.avoid

        val addresses = mutableListOf<String>()
        for (point in route.point.subList(1, route.point.size - 1)) {
            val address = point.address
            addresses.add(address!!)
        }

        val waypointsList = addresses
        var separatedWaypoints = waypointsList.joinToString("|")

        val url =
            "https://www.google.com/maps/dir/?api=1&avoid=${avoid}&origin=$origin&destination=$destination&waypoints=$separatedWaypoints&travelmode=${route.travelMode}"

        Log.println(Log.INFO, "ZAPYTANIE2", url)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, intent, null)
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
            currentUser.value!!.savedRoutes = currentUser.value!!.savedRoutes?.plus(route)
            currentSavedRoutes = currentSavedRoutes + route
            Log.d("TRASKA", "Trasa dodana do listy lokalnie")
            Toast.makeText(context, "Route saved successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error dodawania trasy: $exception")
            Toast.makeText(context, "Error while saving route!", Toast.LENGTH_SHORT).show()
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
                        "Łączna odległość: $totalDistance metrów. Zapisana current Dlugosc: $currentSavingRouteLen"
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