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
import com.google.firebase.database.FirebaseDatabase
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

    private var currentUser: User? = null
    private val placesClient by lazy { Places.createClient(applicationContext) }
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var geoCoder: Geocoder
    var currentSavedRoutes by mutableStateOf(listOf<Route>())
    var routePoints by mutableStateOf(listOf<Point>())
        private set
    var locationState by mutableStateOf<LocationState>(LocationState.NoPermission)
    val locationAutofill = mutableStateListOf<AutocompleteResult>()
    var currentLatLong by mutableStateOf(LatLng(51.9189046, 19.1343786))
    var currentPointId by mutableStateOf(String())
    var currentSavingRouteLen = 0f
    var isLogged by mutableStateOf(false)
    private var job: Job? = null

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

    fun sendRequestOpenMaps(context: Context, travelMode: String) {
        val origin = routePoints.first().address
        val destination = routePoints.last().address
        val waypointsList = routePoints.subList(1, routePoints.size - 1)
        val encodedOrigin = origin?.replace(" ", "%20")
        val encodedDestination = destination?.replace(" ", "%20")
        val encodedWaypoints =
            waypointsList.joinToString("|") { it.address?.replace(" ", "%20").toString() }

        val urlRequest =
            "https://maps.googleapis.com/maps/api/directions/json?" + "origin=$encodedOrigin&" + "destination=$encodedDestination&" + "waypoints=optimize:true|$encodedWaypoints&" + "travelmode=$travelMode&" + "key=" + BuildConfig.DIRECTIONS_API_KEY

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
                    "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&waypoints=$separatedWaypoints&travelmode=$travelMode"

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
            "https://maps.googleapis.com/maps/api/directions/json?" + "origin=$encodedOrigin&" + "destination=$encodedDestination&" + "waypoints=optimize:true|$encodedWaypoints&" + "travelmode=${route.travelMode}&" + "key=" + BuildConfig.DIRECTIONS_API_KEY

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

    fun convertKeyToDigits(key: String): String {
        val regex = Regex("[^0-9]")
        return regex.replace(key, "")
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
            "https://maps.googleapis.com/maps/api/directions/json?" + "origin=$encodedOrigin&" + "destination=$encodedDestination&" + "waypoints=optimize:true|$encodedWaypoints&" + "mode=$travelMode&" + "key=" + BuildConfig.DIRECTIONS_API_KEY

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

}